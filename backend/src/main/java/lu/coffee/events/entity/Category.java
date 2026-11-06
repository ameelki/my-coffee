package lu.coffee.events.entity;

import jakarta.persistence.*;

@Entity
public class Category extends BaseEntity {
    public String name;
    @Column(length = 500)
    public String description;
    public String icon;
    public int position;
    public boolean active = true;
}
