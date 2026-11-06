package lu.coffee.events.service;

import lu.coffee.events.dto.Dto;
import lu.coffee.events.entity.*;
import lu.coffee.events.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class ReservationService {
    private static final List<Enums.ReservationStatus> ACTIVE =
            List.of(Enums.ReservationStatus.PENDING, Enums.ReservationStatus.CONFIRMED);

    private final ReservationRepository res;
    private final BlockedDateRepository blocked;
    private final RoomPackageRepository packages;
    private final RoomExtraRepository extras;
    private final ProductRepository products;
    private final SettingsService settings;
    private final MailService mail;

    public ReservationService(ReservationRepository res, BlockedDateRepository blocked, RoomPackageRepository packages,
                              RoomExtraRepository extras, ProductRepository products, SettingsService settings, MailService mail) {
        this.res = res;
        this.blocked = blocked;
        this.packages = packages;
        this.extras = extras;
        this.products = products;
        this.settings = settings;
        this.mail = mail;
    }

    private static int minutes(LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    /** Motif de fermeture d'un jour (null si ouvert). */
    private String closedReason(LocalDate d, Map<LocalDate, String> closures) {
        if (closures.containsKey(d)) {
            String r = closures.get(d);
            return r == null || r.isBlank() ? "Salle indisponible" : r;
        }
        String weekdays = settings.str("room.closedWeekdays");
        for (String part : weekdays.split(",")) {
            String p = part.trim();
            if (!p.isEmpty() && p.equals(String.valueOf(d.getDayOfWeek().getValue()))) return "Salle fermée ce jour de la semaine";
        }
        return null;
    }

    public List<Dto.DayInfo> month(YearMonth ym) {
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        Map<LocalDate, String> closures = new HashMap<>();
        for (BlockedDate b : blocked.findByDateBetween(from, to)) closures.put(b.date, b.reason == null ? "" : b.reason);
        Map<LocalDate, List<String>> booked = new HashMap<>();
        for (Reservation r : res.findByDateBetweenAndStatusIn(from, to, ACTIVE)) {
            booked.computeIfAbsent(r.date, k -> new ArrayList<>()).add(r.startTime + "-" + r.endTime);
        }
        List<Dto.DayInfo> out = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            String reason = closedReason(d, closures);
            out.add(new Dto.DayInfo(d, reason != null, reason, booked.getOrDefault(d, List.of())));
        }
        return out;
    }

    /** Vérifie les règles, calcule le prix et enregistre. Synchronisé pour éviter les doubles réservations. */
    public synchronized Reservation create(Dto.ReservationRequest r, User user, boolean admin) {
        if (!admin && !settings.bool("room.enabled")) throw Errors.bad("Les réservations de la salle sont fermées pour le moment.");
        LocalDate today = LocalDate.now();
        int notice = settings.integer("room.minNoticeDays");
        if (r.date().isBefore(today)) throw Errors.bad("Cette date est passée.");
        if (!admin && r.date().isBefore(today.plusDays(notice)))
            throw Errors.bad("Réservation possible " + notice + " jour(s) à l'avance minimum.");
        Map<LocalDate, String> closures = new HashMap<>();
        for (BlockedDate b : blocked.findByDateBetween(r.date(), r.date())) closures.put(b.date, b.reason);
        String closed = closedReason(r.date(), closures);
        if (closed != null && !admin) throw Errors.bad("La salle n'est pas disponible ce jour-là (" + closed + ").");

        LocalTime open = LocalTime.parse(settings.str("room.openFrom"));
        LocalTime close = LocalTime.parse(settings.str("room.openTo"));
        int start = minutes(r.startTime());
        int end = minutes(r.endTime());
        if (end <= start) throw Errors.bad("L'heure de fin doit être après l'heure de début.");
        if (!admin && (start < minutes(open) || end > minutes(close)))
            throw Errors.bad("La salle est réservable de " + open + " à " + close + ".");
        int duration = end - start;
        if (!admin && duration < settings.integer("room.minHours") * 60)
            throw Errors.bad("Durée minimum : " + settings.integer("room.minHours") + " h.");
        if (!admin && duration > settings.integer("room.maxHours") * 60)
            throw Errors.bad("Durée maximum : " + settings.integer("room.maxHours") + " h.");
        if (r.guests() > settings.integer("room.maxGuests"))
            throw Errors.bad("Capacité maximum : " + settings.integer("room.maxGuests") + " personnes.");

        int buffer = settings.integer("room.bufferMinutes");
        for (Reservation e : res.findByDateAndStatusIn(r.date(), ACTIVE)) {
            if (start < minutes(e.endTime) + buffer && minutes(e.startTime) - buffer < end)
                throw Errors.conflict("Ce créneau n'est plus disponible. Choisissez un autre horaire ou une autre date.");
        }

        BigDecimal total = settings.dec("room.hourlyRate")
                .multiply(BigDecimal.valueOf(duration)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        Reservation x = new Reservation();
        if (r.packageId() != null) {
            RoomPackage pk = packages.findById(r.packageId()).orElseThrow(() -> Errors.bad("Formule introuvable."));
            if (!pk.active) throw Errors.bad("Cette formule n'est plus proposée.");
            if (r.guests() < pk.minGuests) throw Errors.bad("Cette formule demande au moins " + pk.minGuests + " personnes.");
            total = total.add(pk.pricePerPerson.multiply(BigDecimal.valueOf(r.guests())));
            x.packageName = pk.name;
        }
        List<String> extraNames = new ArrayList<>();
        if (r.extraIds() != null) {
            for (Long id : r.extraIds()) {
                RoomExtra ex = extras.findById(id).orElseThrow(() -> Errors.bad("Option introuvable."));
                if (!ex.active) throw Errors.bad("L'option « " + ex.name + " » n'est plus proposée.");
                total = total.add(ex.perPerson ? ex.price.multiply(BigDecimal.valueOf(r.guests())) : ex.price);
                extraNames.add(ex.name);
            }
        }
        List<String> mealLines = new ArrayList<>();
        if (r.meals() != null && !r.meals().isEmpty()) {
            if (!admin && !settings.bool("room.meals.enabled")) throw Errors.bad("La commande de repas n'est pas proposée pour la salle.");
            for (Dto.MealReq m : r.meals()) {
                int qty = m.perGuest() ? r.guests() : m.quantity();
                if (qty <= 0) continue;
                Product p = products.findById(m.productId()).orElseThrow(() -> Errors.bad("Un produit choisi n'existe plus."));
                if (!p.active || !p.available) throw Errors.bad(p.name + " n'est plus disponible.");
                total = total.add(p.price.multiply(BigDecimal.valueOf(qty)));
                mealLines.add(qty + " × " + p.name);
            }
        }
        x.mealsText = mealLines.isEmpty() ? null : String.join(", ", mealLines);
        total = total.setScale(2, RoundingMode.HALF_UP);
        x.extrasText = extraNames.isEmpty() ? null : String.join(", ", extraNames);
        x.total = total;
        x.deposit = total.multiply(BigDecimal.valueOf(settings.integer("room.depositPercent")))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        x.customerName = r.customerName().trim();
        x.email = r.email();
        x.phone = r.phone();
        x.eventType = r.eventType();
        x.date = r.date();
        x.startTime = r.startTime();
        x.endTime = r.endTime();
        x.guests = r.guests();
        x.note = r.note();
        x.user = user;
        x.status = admin || settings.bool("room.autoConfirm") ? Enums.ReservationStatus.CONFIRMED : Enums.ReservationStatus.PENDING;
        String code;
        do {
            code = CodeGenerator.next("R-");
        } while (res.existsByCode(code));
        x.code = code;
        Reservation saved = res.save(x);

        String recap = describe(saved);
        mail.send(saved.email, "Demande de réservation " + saved.code,
                "Bonjour " + saved.customerName + ",\n\nNous avons bien reçu votre demande :\n" + recap
                        + "\nStatut : " + (saved.status == Enums.ReservationStatus.CONFIRMED ? "confirmée" : "en attente de confirmation")
                        + "\n\n" + settings.str("cafe.name"));
        if (!admin) mail.send(settings.str("cafe.email"), "Nouvelle demande de salle " + saved.code, recap);
        return saved;
    }

    private String describe(Reservation r) {
        return "Code : " + r.code + "\nDate : " + r.date + " de " + r.startTime + " à " + r.endTime
                + "\nInvités : " + r.guests + (r.packageName != null ? "\nFormule : " + r.packageName : "")
                + (r.extrasText != null ? "\nOptions : " + r.extrasText : "")
                + (r.mealsText != null ? "\nRepas : " + r.mealsText : "")
                + "\nTotal : " + r.total + " EUR (acompte " + r.deposit + " EUR)\n";
    }

    public Reservation setStatus(Long id, Enums.ReservationStatus status, String adminNote) {
        Reservation r = res.findById(id).orElseThrow(() -> Errors.notFound("Réservation introuvable."));
        if (r.status == Enums.ReservationStatus.REFUSED || r.status == Enums.ReservationStatus.CANCELLED
                || r.status == Enums.ReservationStatus.COMPLETED)
            throw Errors.bad("Cette réservation est déjà clôturée.");
        r.status = status;
        if (adminNote != null) r.adminNote = adminNote;
        Reservation saved = res.save(r);
        String subject = null;
        if (status == Enums.ReservationStatus.CONFIRMED) subject = "Réservation " + saved.code + " confirmée";
        else if (status == Enums.ReservationStatus.REFUSED) subject = "Réservation " + saved.code + " refusée";
        else if (status == Enums.ReservationStatus.CANCELLED) subject = "Réservation " + saved.code + " annulée";
        if (subject != null) {
            mail.send(saved.email, subject, "Bonjour " + saved.customerName + ",\n\n" + describe(saved)
                    + (saved.adminNote != null && !saved.adminNote.isBlank() ? "\nMessage du café : " + saved.adminNote + "\n" : "")
                    + "\n" + settings.str("cafe.name"));
        }
        return saved;
    }

    public Reservation cancelByCode(String code) {
        Reservation r = res.findByCode(code).orElseThrow(() -> Errors.notFound("Réservation introuvable."));
        if (r.status != Enums.ReservationStatus.PENDING && r.status != Enums.ReservationStatus.CONFIRMED)
            throw Errors.bad("Cette réservation ne peut plus être annulée.");
        if (!r.date.isAfter(LocalDate.now())) throw Errors.bad("Contactez directement le café pour une annulation le jour même.");
        return setStatus(r.id, Enums.ReservationStatus.CANCELLED, null);
    }

}
