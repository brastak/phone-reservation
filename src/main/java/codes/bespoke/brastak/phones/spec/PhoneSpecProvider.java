package codes.bespoke.brastak.phones.spec;

import java.util.Optional;

import codes.bespoke.brastak.phones.spec.model.ExternalSpecDetails;

public interface PhoneSpecProvider {
    Optional<ExternalSpecDetails> spec(String vendor, String model);
}
