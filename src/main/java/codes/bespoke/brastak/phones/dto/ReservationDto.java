package codes.bespoke.brastak.phones.dto;

import java.time.OffsetDateTime;

public record ReservationDto(long id, OffsetDateTime reservedAt, String reservedBy) {
}
