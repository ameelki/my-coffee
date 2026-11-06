package lu.coffee.events.controller;

import jakarta.validation.Valid;
import lu.coffee.events.dto.Dto;
import lu.coffee.events.entity.*;
import lu.coffee.events.repository.*;
import lu.coffee.events.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/** Tout ce qu'un visiteur peut voir ou faire sans compte. */
@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final SettingsService settings;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final DiningTableRepository tables;
    private final RoomPackageRepository packages;
    private final RoomExtraRepository extras;
    private final CafeOrderRepository orders;
    private final ReservationRepository reservations;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final CurrentUser current;

    public PublicController(SettingsService settings, CategoryRepository categories, ProductRepository products,
                            DiningTableRepository tables, RoomPackageRepository packages, RoomExtraRepository extras,
                            CafeOrderRepository orders, ReservationRepository reservations, OrderService orderService,
                            ReservationService reservationService, CurrentUser current) {
        this.settings = settings;
        this.categories = categories;
        this.products = products;
        this.tables = tables;
        this.packages = packages;
        this.extras = extras;
        this.orders = orders;
        this.reservations = reservations;
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.current = current;
    }

    @GetMapping("/settings")
    public Map<String, String> settings() {
        return settings.all();
    }

    @GetMapping("/menu")
    public Dto.Menu menu() {
        List<Category> cats = categories.findAllByOrderByPositionAscIdAsc().stream().filter(c -> c.active).toList();
        List<Product> prods = products.findAllByOrderByPositionAscIdAsc().stream().filter(p -> p.active).toList();
        return new Dto.Menu(cats, prods);
    }

    @GetMapping("/tables")
    public List<String> tables() {
        return tables.findAllByOrderByLabelAsc().stream().filter(t -> t.active).map(t -> t.label).toList();
    }

    @GetMapping("/room")
    public Dto.RoomInfo room() {
        return new Dto.RoomInfo(
                packages.findAllByOrderByPositionAscIdAsc().stream().filter(p -> p.active).toList(),
                extras.findAllByOrderByIdAsc().stream().filter(e -> e.active).toList());
    }

    @GetMapping("/room/availability")
    public List<Dto.DayInfo> availability(@RequestParam String month) {
        try {
            return reservationService.month(YearMonth.parse(month));
        } catch (java.time.format.DateTimeParseException e) {
            throw Errors.bad("Mois invalide (format AAAA-MM).");
        }
    }

    @PostMapping("/orders")
    public CafeOrder order(@Valid @RequestBody Dto.OrderRequest r, Authentication auth) {
        return orderService.create(r, current.get(auth));
    }

    @GetMapping("/orders/{code}")
    public CafeOrder trackOrder(@PathVariable String code) {
        return orders.findByCode(code.trim().toUpperCase()).orElseThrow(() -> Errors.notFound("Aucune commande avec ce code."));
    }

    @PostMapping("/orders/{code}/cancel")
    public CafeOrder cancelOrder(@PathVariable String code) {
        return orderService.cancelByCode(code.trim().toUpperCase());
    }

    @PostMapping("/reservations")
    public Reservation reserve(@Valid @RequestBody Dto.ReservationRequest r, Authentication auth) {
        return reservationService.create(r, current.get(auth), false);
    }

    @GetMapping("/reservations/{code}")
    public Reservation trackReservation(@PathVariable String code) {
        return reservations.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> Errors.notFound("Aucune réservation avec ce code."));
    }

    @PostMapping("/reservations/{code}/cancel")
    public Reservation cancelReservation(@PathVariable String code) {
        return reservationService.cancelByCode(code.trim().toUpperCase());
    }
}
