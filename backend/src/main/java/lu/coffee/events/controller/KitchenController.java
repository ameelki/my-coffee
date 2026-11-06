package lu.coffee.events.controller;

import lu.coffee.events.dto.Dto;
import lu.coffee.events.entity.CafeOrder;
import lu.coffee.events.entity.Enums;
import lu.coffee.events.entity.Product;
import lu.coffee.events.repository.CafeOrderRepository;
import lu.coffee.events.repository.ProductRepository;
import lu.coffee.events.service.Errors;
import lu.coffee.events.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Écran cuisine : commandes à préparer et disponibilité des produits. */
@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {
    private final CafeOrderRepository orders;
    private final ProductRepository products;
    private final OrderService orderService;

    public KitchenController(CafeOrderRepository orders, ProductRepository products, OrderService orderService) {
        this.orders = orders;
        this.products = products;
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public List<CafeOrder> active() {
        return orders.findByStatusInOrderByCreatedAtAsc(
                List.of(Enums.OrderStatus.NEW, Enums.OrderStatus.PREPARING, Enums.OrderStatus.READY));
    }

    @PutMapping("/orders/{id}/status")
    public CafeOrder status(@PathVariable Long id, @RequestBody Dto.StatusRequest r) {
        try {
            return orderService.setStatus(id, Enums.OrderStatus.valueOf(r.status()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw Errors.bad("Statut invalide.");
        }
    }

    @GetMapping("/products")
    public List<Product> products() {
        return products.findAllByOrderByPositionAscIdAsc();
    }

    @PutMapping("/products/{id}/availability")
    public Product availability(@PathVariable Long id, @RequestBody Dto.AvailableRequest r) {
        Product p = products.findById(id).orElseThrow(() -> Errors.notFound("Produit introuvable."));
        p.available = r.available();
        return products.save(p);
    }
}
