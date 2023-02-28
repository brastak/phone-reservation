package codes.bespoke.brastak.phones.spec;

import java.util.Optional;

import codes.bespoke.brastak.phones.spec.model.ExternalSpecDetails;
import org.springframework.stereotype.Component;

@Component
@Remote
public class RemotePhoneSpecProvider implements PhoneSpecProvider {
    @Override
    public Optional<ExternalSpecDetails> spec(String vendor, String model) {
        return Optional.empty();
    }
}
