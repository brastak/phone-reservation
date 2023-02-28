package codes.bespoke.brastak.phones.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class PhoneModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String vendor;
    private String name;
    @OneToOne(mappedBy = "phoneModel")
    private Spec spec;

//    public void setSpec(Spec spec) {
//        this.spec = spec;
//        this.spec.setPhoneModel(this);
//    }
}
