package lu.coffee.events.repository;

import lu.coffee.events.entity.CafeOrder;
import lu.coffee.events.entity.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CafeOrderRepository extends JpaRepository<CafeOrder, Long> {
    Optional<CafeOrder> findByCode(String code);
    boolean existsByCode(String code);
    List<CafeOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<CafeOrder> findByStatusInOrderByCreatedAtAsc(Collection<Enums.OrderStatus> statuses);
    List<CafeOrder> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime from);
    List<CafeOrder> findTop200ByOrderByCreatedAtDesc();
}
