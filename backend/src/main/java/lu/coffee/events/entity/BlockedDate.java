package lu.coffee.events.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class BlockedDate extends BaseEntity {
    @Column(unique = true)
    public LocalDate date;
    public String reason;
}
