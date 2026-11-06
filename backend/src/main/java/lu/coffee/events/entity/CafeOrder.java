package lu.coffee.events.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cafe_orders")
public class CafeOrder extends BaseEntity {
    @Column(unique = true, nullable = false)
    public String code;
    @Enumerated(EnumType.STRING)
    public Enums.OrderMode mode;
    @Enumerated(EnumType.STRING)
    public Enums.OrderStatus status = Enums.OrderStatus.NEW;
    public String tableLabel;
    public String customerName;
    public String phone;
    public String email;
    public LocalDateTime pickupAt;
    @Column(length = 1000)
    public String note;
    @Column(precision = 10, scale = 2)
    public BigDecimal total = BigDecimal.ZERO;
    public boolean paid;
    @ManyToOne
    @JsonIgnore
    public User user;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<OrderItem> items = new ArrayList<>();
}
