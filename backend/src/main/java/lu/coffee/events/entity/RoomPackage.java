package lu.coffee.events.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class RoomPackage extends BaseEntity {
    public String name;
    @Column(length = 1000)
    public String description;
    @Column(precision = 10, scale = 2)
    public BigDecimal pricePerPerson = BigDecimal.ZERO;
    public int minGuests = 1;
    public int position;
    public boolean active = true;
}
