package lu.coffee.events.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Product extends BaseEntity {
    public String name;
    @Column(length = 1000)
    public String description;
    @Column(precision = 10, scale = 2)
    public BigDecimal price = BigDecimal.ZERO;
    @Column(length = 1000)
    public String imageUrl;
    @ManyToOne
    public Category category;
    public boolean available = true;
    public boolean active = true;
    public int position;
    public Integer prepMinutes;
    /** ex : vegan,sans gluten */
    public String tags;
    /** [{"name":"Taille","required":true,"multi":false,"choices":[{"name":"Petit","price":0}]}] */
    @Column(length = 4000)
    public String optionsJson;
}
