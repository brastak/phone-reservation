package codes.bespoke.brastak.phones.model;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "phone_spec")
public class Spec {
    @Id
    private Integer id;
    @OneToOne(optional = false)
    @JoinColumn(name = "phone_model_id")
    @ToString.Exclude
    @MapsId
    private PhoneModel phoneModel;
    @CollectionTable(name = "phone_spec_technology", joinColumns = @JoinColumn(name = "spec_id", referencedColumnName = "phone_model_id"))
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "name")
    private List<String> technologies;
    @CollectionTable(name = "phone_spec_2g_band", joinColumns = @JoinColumn(name = "spec_id", referencedColumnName = "phone_model_id"))
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "band")
    private List<String> _2gBands;
    @CollectionTable(name = "phone_spec_3g_band", joinColumns = @JoinColumn(name = "spec_id", referencedColumnName = "phone_model_id"))
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "band")
    private List<String> _3gBands;
    @CollectionTable(name = "phone_spec_4g_band", joinColumns = @JoinColumn(name = "spec_id", referencedColumnName = "phone_model_id"))
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "band")
    private List<String> _4gBands;
    @Column(name = "last_updated_at", nullable = false)
    private OffsetDateTime lastUpdatedAt;
    @Column(name = "last_update_started_at")
    private OffsetDateTime lastUpdateStartedAt;
}
