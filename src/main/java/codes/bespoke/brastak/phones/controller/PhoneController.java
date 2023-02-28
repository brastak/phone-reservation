package codes.bespoke.brastak.phones.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codes.bespoke.brastak.phones.dto.PhoneInfoDto;
import codes.bespoke.brastak.phones.mapper.PhoneMapper;
import codes.bespoke.brastak.phones.model.Phone;
import codes.bespoke.brastak.phones.model.Reservation;
import codes.bespoke.brastak.phones.service.PhoneService;
import codes.bespoke.brastak.phones.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("phones")
@RequiredArgsConstructor
public class PhoneController {
    private final PhoneService phoneService;
    private final ReservationService reservationService;
    private final PhoneMapper phoneMapper;

    @GetMapping("all")
    @Transactional
    public List<PhoneInfoDto> getAllPhones() {
        log.debug("Find all phones");
        List<Phone> phones = phoneService.findAll();
        log.debug("{} phones found", phones.size());
        log.trace("Found phones {}", phones);

        log.debug("Find active reservations");
        Map<Integer, Reservation> phoneReservations = reservationService.findReservationsByPhoneIds(phones.stream()
                .map(Phone::getId)
                .collect(Collectors.toList())).stream()
            .collect(Collectors.toMap(reservation -> reservation.getPhone().getId(), reservation -> reservation));
        log.debug("{} active reservations found", phoneReservations.size());
        log.trace("Found active reservations {}", phoneReservations);

        log.debug("Enrich phone specs with reservation info");
        return phones.stream()
            .map(phone -> phoneMapper.phoneToPhoneInfoDto(phone, phoneReservations.get(phone.getId())))
            .toList();
    }

    @GetMapping("{id}")
    @Transactional
    public ResponseEntity<PhoneInfoDto> getPhone(@PathVariable("id") Integer id) {
        log.debug("Find phone by id {}", id);
        return phoneService.findById(id)
            .map(phone -> {
                log.debug("Find active reservations for phone with id {}", id);
                List<Reservation> reservations = reservationService.findReservationsByPhoneIds(List.of(phone.getId()));
                log.debug("Found {} active reservations for phone with id {}", reservations.size(), id);

                log.debug("Enrich phone spec with reservation info");
                return phoneMapper.phoneToPhoneInfoDto(phone, reservations.isEmpty() ? null : reservations.get(0));
            })
            .map(ResponseEntity::ok)
            .orElseGet(() -> {
                log.debug("Phone with id {} not found", id);
                return ResponseEntity.notFound().build();
            });
    }
}
