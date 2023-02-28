package codes.bespoke.brastak.phones.mapper;

import codes.bespoke.brastak.phones.dto.PhoneInfoDto;
import codes.bespoke.brastak.phones.dto.ReservationDto;
import codes.bespoke.brastak.phones.dto.SpecDto;
import codes.bespoke.brastak.phones.model.Phone;
import codes.bespoke.brastak.phones.model.PhoneModel;
import codes.bespoke.brastak.phones.model.Reservation;
import codes.bespoke.brastak.phones.service.SpecService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PhoneMapper {
    @Autowired
    private SpecService specService;
    @Autowired
    private SpecMapper specMapper;

    @Mapping(target = "phoneId", source = "phone.id")
    @Mapping(target = "modelId", source = "phone.model.id")
    @Mapping(target = "vendor", source = "phone.model.vendor")
    @Mapping(target = "model", source = "phone.model.name")
    @Mapping(target = "spec", expression = "java(specEnrich(phone.getModel()))")
    public abstract PhoneInfoDto phoneToPhoneInfoDto(Phone phone, Reservation reservation);

    public PhoneInfoDto reservationToPhoneInfoDto(Reservation reservation) {
        return phoneToPhoneInfoDto(reservation.getPhone(), reservation);
    }

    abstract ReservationDto reservationToReservationDto(Reservation reservation);

    protected SpecDto specEnrich(PhoneModel phoneModel) {
        return specMapper.specToSpecDto(specService.specEnrich(phoneModel));
    }
}
