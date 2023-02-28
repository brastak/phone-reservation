package codes.bespoke.brastak.phones.dto;

public record PhoneInfoDto(int phoneId, int modelId, String vendor, String model, String serialNumber,
                           SpecDto spec, ReservationDto reservation) {
}
