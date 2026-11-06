package lu.coffee.events.controller;

import jakarta.validation.Valid;
import lu.coffee.events.dto.Dto;
import lu.coffee.events.entity.CafeOrder;
import lu.coffee.events.entity.Reservation;
import lu.coffee.events.entity.User;
import lu.coffee.events.repository.CafeOrderRepository;
import lu.coffee.events.repository.ReservationRepository;
import lu.coffee.events.repository.UserRepository;
import lu.coffee.events.service.Errors;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Espace client : profil, historique de commandes et de réservations. */
@RestController
@RequestMapping("/api/me")
public class MeController {
    private final CurrentUser current;
    private final UserRepository users;
    private final CafeOrderRepository orders;
    private final ReservationRepository reservations;

    public MeController(CurrentUser current, UserRepository users, CafeOrderRepository orders, ReservationRepository reservations) {
        this.current = current;
        this.users = users;
        this.orders = orders;
        this.reservations = reservations;
    }

    private User me(Authentication auth) {
        User u = current.get(auth);
        if (u == null) throw Errors.notFound("Utilisateur introuvable.");
        return u;
    }

    @GetMapping
    public User profile(Authentication auth) {
        return me(auth);
    }

    @PutMapping
    public User update(@Valid @RequestBody Dto.ProfileRequest r, Authentication auth) {
        User u = me(auth);
        u.name = r.name().trim();
        u.phone = r.phone();
        return users.save(u);
    }

    @GetMapping("/orders")
    public List<CafeOrder> orders(Authentication auth) {
        return orders.findByUserIdOrderByCreatedAtDesc(me(auth).id);
    }

    @GetMapping("/reservations")
    public List<Reservation> reservations(Authentication auth) {
        return reservations.findByUserIdOrderByDateDescStartTimeDesc(me(auth).id);
    }
}
