package codes.bespoke.brastak.phones.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "phone_reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Phone phone;
    @Column(name = "reserved_at")
    private OffsetDateTime reservedAt;
    @Column(name = "reserved_by")
    private String reservedBy;
    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;
}
