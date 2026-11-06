package lu.coffee.events.repository;

import lu.coffee.events.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByPositionAscIdAsc();
}
