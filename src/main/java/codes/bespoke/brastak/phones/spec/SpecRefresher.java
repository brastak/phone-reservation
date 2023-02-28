package codes.bespoke.brastak.phones.spec;

import java.time.OffsetDateTime;

import codes.bespoke.brastak.phones.model.Spec;
import codes.bespoke.brastak.phones.repository.SpecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SpecRefresher {
    @Remote
    private final PhoneSpecProvider remoteSpecProvider;
    private final SpecRepository specRepository;

    @Async
    public void refreshSpec(Spec spec) {
        log.debug("Starting refresh spec {} from remote provider", spec);
        remoteSpecProvider.spec(spec.getPhoneModel().getVendor(), spec.getPhoneModel().getName())
            .ifPresentOrElse(specDto -> {
                spec.setTechnologies(specDto.technologies());
                spec.set_2gBands(specDto._2gBands());
                spec.set_3gBands(specDto._3gBands());
                spec.set_4gBands(specDto._4gBands());
                spec.setLastUpdatedAt(OffsetDateTime.now());

                specRepository.save(spec);
                log.debug("Refreshed spec {}", spec);
            }, () -> log.debug("Refreshing spec {} from remote provider failed", spec));
    }
}
