package lu.coffee.events.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class RoomExtra extends BaseEntity {
    public String name;
    @Column(length = 500)
    public String description;
    @Column(precision = 10, scale = 2)
    public BigDecimal price = BigDecimal.ZERO;
    /** true : prix multiplié par le nombre d'invités */
    public boolean perPerson;
    public boolean active = true;
}
