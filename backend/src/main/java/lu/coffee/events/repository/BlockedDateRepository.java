package lu.coffee.events.repository;

import lu.coffee.events.entity.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {
    List<BlockedDate> findByDateBetween(LocalDate from, LocalDate to);
    List<BlockedDate> findAllByOrderByDateAsc();
    boolean existsByDate(LocalDate date);
}
