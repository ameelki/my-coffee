package lu.coffee.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lu.coffee.events.dto.Dto;
import lu.coffee.events.entity.*;
import lu.coffee.events.repository.CafeOrderRepository;
import lu.coffee.events.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final CafeOrderRepository orders;
    private final ProductRepository products;
    private final SettingsService settings;
    private final ObjectMapper mapper;
    private final MailService mail;

    public OrderService(CafeOrderRepository orders, ProductRepository products, SettingsService settings,
                        ObjectMapper mapper, MailService mail) {
        this.orders = orders;
        this.products = products;
        this.settings = settings;
        this.mapper = mapper;
        this.mail = mail;
    }

    private record Resolved(BigDecimal extra, String text) {}

    public CafeOrder create(Dto.OrderRequest r, User user) {
        if (!settings.bool("orders.enabled")) throw Errors.bad("Les commandes sont fermées pour le moment.");
        CafeOrder o = new CafeOrder();
        o.mode = r.mode();
        if (r.mode() == Enums.OrderMode.DINE_IN) {
            if (!settings.bool("orders.dineIn.enabled")) throw Errors.bad("La commande sur place est désactivée.");
            if (r.tableLabel() == null || r.tableLabel().isBlank()) throw Errors.bad("Indiquez votre numéro de table.");
            o.tableLabel = r.tableLabel().trim();
        } else {
            if (!settings.bool("orders.takeaway.enabled")) throw Errors.bad("La vente à emporter est désactivée.");
            LocalDateTime earliest = LocalDateTime.now().plusMinutes(settings.integer("orders.takeaway.minMinutes") - 1L);
            if (r.pickupAt() == null) o.pickupAt = LocalDateTime.now().plusMinutes(settings.integer("orders.takeaway.minMinutes"));
            else if (r.pickupAt().isBefore(earliest))
                throw Errors.bad("Retrait possible dans " + settings.integer("orders.takeaway.minMinutes") + " minutes minimum.");
            else o.pickupAt = r.pickupAt();
        }
        o.customerName = r.customerName().trim();
        o.phone = r.phone();
        o.email = r.email();
        o.note = r.note();
        o.user = user;

        BigDecimal total = BigDecimal.ZERO;
        for (Dto.OrderItemReq it : r.items()) {
            Product p = products.findById(it.productId())
                    .orElseThrow(() -> Errors.bad("Un produit de votre panier n'existe plus."));
            if (!p.active || !p.available) throw Errors.bad(p.name + " n'est plus disponible.");
            Resolved res = resolve(p, it.choices());
            BigDecimal unit = p.price.add(res.extra());
            OrderItem oi = new OrderItem();
            oi.order = o;
            oi.productId = p.id;
            oi.name = p.name;
            oi.unitPrice = unit;
            oi.quantity = it.quantity();
            oi.optionsText = res.text().isEmpty() ? null : res.text();
            oi.note = it.note();
            o.items.add(oi);
            total = total.add(unit.multiply(BigDecimal.valueOf(it.quantity())));
        }
        o.total = total.setScale(2, RoundingMode.HALF_UP);
        String code;
        do {
            code = CodeGenerator.next("C-");
        } while (orders.existsByCode(code));
        o.code = code;
        CafeOrder saved = orders.save(o);
        mail.send(saved.email, "Commande " + saved.code + " reçue",
                "Bonjour " + saved.customerName + ",\n\nVotre commande " + saved.code + " (" + saved.total
                        + " EUR) est bien reçue. Suivez-la avec ce code depuis le site.\n\n" + settings.str("cafe.name"));
        return saved;
    }

    @SuppressWarnings("unchecked")
    private Resolved resolve(Product p, List<String> choices) {
        List<Map<String, Object>> groups = new ArrayList<>();
        if (p.optionsJson != null && !p.optionsJson.isBlank()) {
            try {
                groups = mapper.readValue(p.optionsJson, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                groups = new ArrayList<>();
            }
        }
        List<String> sel = choices == null ? List.of() : choices;
        BigDecimal extra = BigDecimal.ZERO;
        List<String> texts = new ArrayList<>();
        for (Map<String, Object> g : groups) {
            String gname = String.valueOf(g.get("name"));
            boolean required = Boolean.TRUE.equals(g.get("required"));
            boolean multi = Boolean.TRUE.equals(g.get("multi"));
            Object rawChoices = g.get("choices");
            List<Map<String, Object>> cs = rawChoices instanceof List ? (List<Map<String, Object>>) rawChoices : List.of();
            int count = 0;
            for (Map<String, Object> c : cs) {
                String key = gname + "|" + c.get("name");
                if (sel.contains(key)) {
                    count++;
                    Object price = c.get("price");
                    extra = extra.add(price == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(price)));
                    texts.add(String.valueOf(c.get("name")));
                }
            }
            if (required && count == 0) throw Errors.bad("Choisissez « " + gname + " » pour " + p.name + ".");
            if (!multi && count > 1) throw Errors.bad("Un seul choix possible pour « " + gname + " ».");
        }
        return new Resolved(extra, String.join(", ", texts));
    }

    public CafeOrder setStatus(Long id, Enums.OrderStatus status) {
        CafeOrder o = orders.findById(id).orElseThrow(() -> Errors.notFound("Commande introuvable."));
        if (o.status == Enums.OrderStatus.SERVED || o.status == Enums.OrderStatus.CANCELLED)
            throw Errors.bad("Cette commande est déjà clôturée.");
        o.status = status;
        o.updatedAt = LocalDateTime.now();
        CafeOrder saved = orders.save(o);
        if (status == Enums.OrderStatus.READY) {
            mail.send(saved.email, "Commande " + saved.code + " prête",
                    "Bonjour " + saved.customerName + ", votre commande " + saved.code + " est prête.\n\n" + settings.str("cafe.name"));
        }
        return saved;
    }

    public CafeOrder cancelByCode(String code) {
        CafeOrder o = orders.findByCode(code).orElseThrow(() -> Errors.notFound("Commande introuvable."));
        if (o.status != Enums.OrderStatus.NEW) throw Errors.bad("La préparation a déjà commencé, contactez le café.");
        o.status = Enums.OrderStatus.CANCELLED;
        o.updatedAt = LocalDateTime.now();
        return orders.save(o);
    }
}
