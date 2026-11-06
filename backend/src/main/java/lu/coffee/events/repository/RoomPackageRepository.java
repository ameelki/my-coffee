package lu.coffee.events.repository;

import lu.coffee.events.entity.RoomPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomPackageRepository extends JpaRepository<RoomPackage, Long> {
    List<RoomPackage> findAllByOrderByPositionAscIdAsc();
}
