package lu.coffee.events.repository;

import lu.coffee.events.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByPositionAscIdAsc();
}
