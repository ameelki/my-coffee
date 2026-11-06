package lu.coffee.events.repository;

import lu.coffee.events.entity.Enums;
import lu.coffee.events.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByCode(String code);
    boolean existsByCode(String code);
    List<Reservation> findByUserIdOrderByDateDescStartTimeDesc(Long userId);
    List<Reservation> findByDateAndStatusIn(LocalDate date, Collection<Enums.ReservationStatus> statuses);
    List<Reservation> findByDateBetweenAndStatusIn(LocalDate from, LocalDate to, Collection<Enums.ReservationStatus> statuses);
    List<Reservation> findAllByOrderByDateDescStartTimeDesc();
    List<Reservation> findByDateGreaterThanEqualAndStatusInOrderByDateAscStartTimeAsc(LocalDate from, Collection<Enums.ReservationStatus> statuses);
    long countByStatus(Enums.ReservationStatus status);
}
