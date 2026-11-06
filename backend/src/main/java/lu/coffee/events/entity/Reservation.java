package lu.coffee.events.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Reservation extends BaseEntity {
    @Column(unique = true, nullable = false)
    public String code;
    public String customerName;
    public String email;
    public String phone;
    public String eventType;
    public LocalDate date;
    public LocalTime startTime;
    public LocalTime endTime;
    public int guests;
    public String packageName;
    @Column(length = 1000)
    public String extrasText;
    @Column(length = 1000)
    public String note;
    @Column(precision = 10, scale = 2)
    public BigDecimal total = BigDecimal.ZERO;
    @Column(precision = 10, scale = 2)
    public BigDecimal deposit = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    public Enums.ReservationStatus status = Enums.ReservationStatus.PENDING;
    @Column(length = 1000)
    public String adminNote;
    @ManyToOne
    @JsonIgnore
    public User user;
    public LocalDateTime createdAt = LocalDateTime.now();
}
