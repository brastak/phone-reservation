package codes.bespoke.brastak.phones.controller;

import java.util.Optional;

import codes.bespoke.brastak.phones.dto.PhoneInfoDto;
import codes.bespoke.brastak.phones.dto.ReservationRequestDto;
import codes.bespoke.brastak.phones.mapper.PhoneMapper;
import codes.bespoke.brastak.phones.model.Reservation;
import codes.bespoke.brastak.phones.service.PhoneService;
import codes.bespoke.brastak.phones.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final PhoneService phoneService;
    private final ReservationService reservationService;
    private final PhoneMapper phoneMapper;

    @PostMapping
    @Transactional
    public ResponseEntity<PhoneInfoDto> reservePhone(@RequestBody ReservationRequestDto reservationRequest) {
        log.debug("Try to reserve phone with model id {} by {}", reservationRequest.modelId(),
            reservationRequest.reservedBy());
        Optional<Reservation> reservationOpt = phoneService.findUnreservedPhone(reservationRequest.modelId())
            .map(phone -> reservationService.reservePhone(phone, reservationRequest.reservedBy()));
        return reservationOpt.map(reservation -> {
                log.debug("Reservation {} successfully created", reservation);
                return phoneMapper.reservationToPhoneInfoDto(reservation);
            })
            .map(ResponseEntity::ok)
            .orElseGet(() -> {
                String errorMsg = "No phones of model id " + reservationRequest.modelId() + " are available";
                log.error(errorMsg);
                return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMsg)).build();
            });
    }

    @PostMapping("{id}/finish")
    @Transactional
    public ResponseEntity<?> finishReservation(@PathVariable long id) {
        log.debug("Try to finish reservation {}", id);
        return reservationService.findActiveReservationById(id)
            .map(reservation -> {
                reservationService.finishReservation(reservation);
                log.debug("Reservation {} successfully finished", id);
                return ResponseEntity.ok().build();
            }).orElseGet(() -> {
                String errorMsg = "No active reservation " + id + " found";
                log.error(errorMsg);
                return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorMsg)).build();
            });
    }
}
