package codes.bespoke.brastak.phones.repository;

import java.util.List;
import java.util.Optional;

import codes.bespoke.brastak.phones.model.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByReturnedAtIsNullAndPhone_IdIn(List<Integer> phoneIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findByIdAndReturnedAtIsNull(Long id);
}
