package lu.coffee.events.repository;

import lu.coffee.events.entity.RoomExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomExtraRepository extends JpaRepository<RoomExtra, Long> {
    List<RoomExtra> findAllByOrderByIdAsc();
}
