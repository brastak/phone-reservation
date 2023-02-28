package codes.bespoke.brastak.phones.service;

import java.time.Duration;
import java.time.OffsetDateTime;

import codes.bespoke.brastak.phones.mapper.SpecMapper;
import codes.bespoke.brastak.phones.model.PhoneModel;
import codes.bespoke.brastak.phones.model.Spec;
import codes.bespoke.brastak.phones.repository.SpecRepository;
import codes.bespoke.brastak.phones.spec.Local;
import codes.bespoke.brastak.phones.spec.PhoneSpecProvider;
import codes.bespoke.brastak.phones.spec.Remote;
import codes.bespoke.brastak.phones.spec.SpecRefresher;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SpecService {
    private final SpecRepository specRepository;
    @Remote
    private final PhoneSpecProvider remoteSpecProvider;
    @Local
    private final PhoneSpecProvider localSpecProvider;
    private final SpecRefresher specRefresher;
    private final SpecMapper specMapper;

    @Value("${phones.spec.ttl}")
    Duration specTtl;
    @Value("${phones.spec.refreshGracePeriod}")
    Duration specRefreshGracePeriod;

    public Spec specEnrich(PhoneModel phoneModel) {
        if (phoneModel.getSpec() == null) {
            log.debug("No spec found for model {}. Try to load it", phoneModel);
            loadSpec(phoneModel);
        } else if (needToRefreshSpec(phoneModel.getSpec())) {
            log.debug("Spec {} considered out of date. Refresh it async", phoneModel.getSpec());
            startSpecRefreshAsync(phoneModel);
        }
        return phoneModel.getSpec();
    }

    private void loadSpec(PhoneModel phoneModel) {
        remoteSpecProvider.spec(phoneModel.getVendor(), phoneModel.getName())
            .or(() -> localSpecProvider.spec(phoneModel.getVendor(), phoneModel.getName()))
            .ifPresentOrElse(specDto -> {
                Spec spec = specMapper.externalSpecToSpec(specDto);
                spec.setLastUpdatedAt(OffsetDateTime.now());
                spec.setPhoneModel(phoneModel);
                phoneModel.setSpec(spec);
                specRepository.save(spec);
                log.debug("Loaded spec {}", spec);
            }, () -> log.debug("No spec found for model {}: neither local nor remote", phoneModel));
    }

    private boolean needToRefreshSpec(Spec spec) {
        OffsetDateTime now = OffsetDateTime.now();
        return Duration.between(spec.getLastUpdatedAt(), now).compareTo(specTtl) > 0
            && (spec.getLastUpdateStartedAt() == null
            || Duration.between(spec.getLastUpdateStartedAt(), now).compareTo(specRefreshGracePeriod) > 0);
    }

    private void startSpecRefreshAsync(PhoneModel phoneModel) {
        Spec spec = phoneModel.getSpec();
        spec.setLastUpdateStartedAt(OffsetDateTime.now());
        specRepository.save(spec);

        specRefresher.refreshSpec(spec);
    }
}
