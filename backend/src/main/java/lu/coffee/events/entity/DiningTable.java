package lu.coffee.events.entity;

import jakarta.persistence.*;

@Entity
public class DiningTable extends BaseEntity {
    @Column(unique = true)
    public String label;
    public int seats = 2;
    public boolean active = true;
}
