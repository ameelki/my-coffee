package lu.coffee.events.controller;

import lu.coffee.events.dto.Dto;
import lu.coffee.events.entity.*;
import lu.coffee.events.repository.*;
import lu.coffee.events.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Réglages, comptes, commandes, réservations et statistiques. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final List<Enums.ReservationStatus> OPEN =
            List.of(Enums.ReservationStatus.PENDING, Enums.ReservationStatus.CONFIRMED);

    private final SettingsService settings;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final CafeOrderRepository orders;
    private final ReservationRepository reservations;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final CurrentUser current;

    public AdminController(SettingsService settings, UserRepository users, PasswordEncoder encoder,
                           CafeOrderRepository orders, ReservationRepository reservations, OrderService orderService,
                           ReservationService reservationService, CurrentUser current) {
        this.settings = settings;
        this.users = users;
        this.encoder = encoder;
        this.orders = orders;
        this.reservations = reservations;
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.current = current;
    }

    // ---------- Réglages ----------
    @GetMapping("/settings")
    public Map<String, String> getSettings() {
        return settings.all();
    }

    @PutMapping("/settings")
    public Map<String, String> putSettings(@RequestBody Map<String, String> body) {
        return settings.update(body);
    }

    // ---------- Comptes ----------
    @GetMapping("/users")
    public List<User> users() {
        return users.findAll();
    }

    @PostMapping("/users")
    public User createUser(@RequestBody Dto.StaffRequest r) {
        if (r.email() == null || r.email().isBlank() || r.password() == null || r.password().length() < 8)
            throw Errors.bad("Email et mot de passe (8 caractères minimum) obligatoires.");
        if (users.existsByEmailIgnoreCase(r.email())) throw Errors.conflict("Un compte existe déjà avec cet email.");
        User u = new User();
        u.name = r.name() == null || r.name().isBlank() ? r.email() : r.name().trim();
        u.email = r.email().trim().toLowerCase();
        u.phone = r.phone();
        u.passwordHash = encoder.encode(r.password());
        u.role = r.role() == null ? Enums.Role.KITCHEN : r.role();
        return users.save(u);
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody Dto.StaffRequest r, Authentication auth) {
        User u = users.findById(id).orElseThrow(() -> Errors.notFound("Compte introuvable."));
        User me = current.get(auth);
        boolean self = me != null && me.id.equals(u.id);
        if (r.name() != null && !r.name().isBlank()) u.name = r.name().trim();
        if (r.phone() != null) u.phone = r.phone();
        if (r.password() != null && !r.password().isBlank()) {
            if (r.password().length() < 8) throw Errors.bad("Mot de passe : 8 caractères minimum.");
            u.passwordHash = encoder.encode(r.password());
        }
        if (r.role() != null) {
            if (self && r.role() != Enums.Role.ADMIN) throw Errors.bad("Vous ne pouvez pas retirer votre propre rôle admin.");
            u.role = r.role();
        }
        if (r.active() != null) {
            if (self && !r.active()) throw Errors.bad("Vous ne pouvez pas désactiver votre propre compte.");
            u.active = r.active();
        }
        return users.save(u);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id, Authentication auth) {
        User me = current.get(auth);
        if (me != null && me.id.equals(id)) throw Errors.bad("Vous ne pouvez pas supprimer votre propre compte.");
        users.deleteById(id);
    }

    // ---------- Commandes ----------
    @GetMapping("/orders")
    public List<CafeOrder> orders() {
        return orders.findTop200ByOrderByCreatedAtDesc();
    }

    @PutMapping("/orders/{id}/status")
    public CafeOrder orderStatus(@PathVariable Long id, @RequestBody Dto.StatusRequest r) {
        try {
            return orderService.setStatus(id, Enums.OrderStatus.valueOf(r.status()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw Errors.bad("Statut invalide.");
        }
    }

    @PutMapping("/orders/{id}/paid")
    public CafeOrder orderPaid(@PathVariable Long id, @RequestBody Dto.PaidRequest r) {
        CafeOrder o = orders.findById(id).orElseThrow(() -> Errors.notFound("Commande introuvable."));
        o.paid = r.paid();
        return orders.save(o);
    }

    // ---------- Réservations ----------
    @GetMapping("/reservations")
    public List<Reservation> reservations() {
        return reservations.findAllByOrderByDateDescStartTimeDesc();
    }

    /** Réservation saisie par le personnel (téléphone, comptoir) : confirmée d'office. */
    @PostMapping("/reservations")
    public Reservation createReservation(@jakarta.validation.Valid @RequestBody Dto.ReservationRequest r) {
        return reservationService.create(r, null, true);
    }

    @PutMapping("/reservations/{id}/status")
    public Reservation reservationStatus(@PathVariable Long id, @RequestBody Dto.StatusRequest r) {
        try {
            return reservationService.setStatus(id, Enums.ReservationStatus.valueOf(r.status()), r.adminNote());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw Errors.bad("Statut invalide.");
        }
    }

    // ---------- Statistiques ----------
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        LocalDate today = LocalDate.now();
        List<CafeOrder> recent = orders.findByCreatedAtAfterOrderByCreatedAtDesc(today.minusDays(29).atStartOfDay());
        BigDecimal revenueToday = BigDecimal.ZERO;
        BigDecimal revenue30 = BigDecimal.ZERO;
        int ordersToday = 0;
        int orders30 = 0;
        Map<LocalDate, BigDecimal> revenueByDay = new HashMap<>();
        Map<LocalDate, Integer> countByDay = new HashMap<>();
        Map<String, Integer> qtyByProduct = new HashMap<>();
        for (CafeOrder o : recent) {
            if (o.status == Enums.OrderStatus.CANCELLED) continue;
            LocalDate d = o.createdAt.toLocalDate();
            orders30++;
            revenue30 = revenue30.add(o.total);
            revenueByDay.merge(d, o.total, BigDecimal::add);
            countByDay.merge(d, 1, Integer::sum);
            if (d.equals(today)) {
                ordersToday++;
                revenueToday = revenueToday.add(o.total);
            }
            for (OrderItem it : o.items) qtyByProduct.merge(it.name, it.quantity, Integer::sum);
        }
        List<Map<String, Object>> days = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", d.toString());
            row.put("orders", countByDay.getOrDefault(d, 0));
            row.put("revenue", revenueByDay.getOrDefault(d, BigDecimal.ZERO));
            days.add(row);
        }
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(qtyByProduct.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        List<Map<String, Object>> top = new ArrayList<>();
        for (int i = 0; i < Math.min(6, sorted.size()); i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", sorted.get(i).getKey());
            row.put("quantity", sorted.get(i).getValue());
            top.add(row);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ordersToday", ordersToday);
        out.put("revenueToday", revenueToday);
        out.put("orders30", orders30);
        out.put("revenue30", revenue30);
        out.put("pendingReservations", reservations.countByStatus(Enums.ReservationStatus.PENDING));
        out.put("days", days);
        out.put("topProducts", top);
        List<Reservation> upcoming = reservations
                .findByDateGreaterThanEqualAndStatusInOrderByDateAscStartTimeAsc(today, OPEN);
        out.put("upcomingReservations", upcoming.subList(0, Math.min(6, upcoming.size())));
        return out;
    }
}
