package lu.coffee.events.repository;

import lu.coffee.events.entity.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    List<DiningTable> findAllByOrderByLabelAsc();
}
