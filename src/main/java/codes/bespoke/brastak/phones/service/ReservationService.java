package codes.bespoke.brastak.phones.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import codes.bespoke.brastak.phones.model.Phone;
import codes.bespoke.brastak.phones.model.Reservation;
import codes.bespoke.brastak.phones.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public List<Reservation> findReservationsByPhoneIds(List<Integer> phoneIds) {
        return reservationRepository.findAllByReturnedAtIsNullAndPhone_IdIn(phoneIds);
    }

    public Optional<Reservation> findActiveReservationById(long reservationId) {
        return reservationRepository.findByIdAndReturnedAtIsNull(reservationId);
    }

    public Reservation reservePhone(Phone phone, String reservedBy) {
        Reservation reservation = new Reservation();
        reservation.setPhone(phone);
        reservation.setReservedAt(OffsetDateTime.now());
        reservation.setReservedBy(reservedBy);
        return reservationRepository.save(reservation);
    }

    public void finishReservation(Reservation reservation) {
        reservation.setReturnedAt(OffsetDateTime.now());
        reservationRepository.save(reservation);
    }
}
