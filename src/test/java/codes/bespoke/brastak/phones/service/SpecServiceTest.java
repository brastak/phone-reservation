package codes.bespoke.brastak.phones.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import codes.bespoke.brastak.phones.mapper.SpecMapperImpl;
import codes.bespoke.brastak.phones.model.PhoneModel;
import codes.bespoke.brastak.phones.model.Spec;
import codes.bespoke.brastak.phones.repository.SpecRepository;
import codes.bespoke.brastak.phones.spec.PhoneSpecProvider;
import codes.bespoke.brastak.phones.spec.SpecRefresher;
import codes.bespoke.brastak.phones.spec.model.ExternalSpecDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpecServiceTest {
    private static final String VENDOR = "Test";
    private static final String MODEL = "Test phone";

    @Mock
    private SpecRepository specRepository;
    @Mock(name = "remoteSpecProvider")
    private PhoneSpecProvider remoteSpecProvider;
    @Mock(name = "localSpecProvider")
    private PhoneSpecProvider localSpecProvider;
    @Mock
    private SpecRefresher specRefresher;

    private SpecService specService;

    private PhoneModel phoneModel;
    private Spec baseSpec;

    @BeforeEach
    public void setUp() {
        phoneModel = new PhoneModel();
        phoneModel.setVendor(VENDOR);
        phoneModel.setName(MODEL);

        baseSpec = new Spec();
        baseSpec.setTechnologies(List.of("GSM"));
        baseSpec.set_2gBands(List.of("GSM_900", "GSM_1800"));

        specService = new SpecService(specRepository, remoteSpecProvider, localSpecProvider, specRefresher,
            new SpecMapperImpl());
        specService.specTtl = Duration.of(1, ChronoUnit.DAYS);
        specService.specRefreshGracePeriod = Duration.of(1, ChronoUnit.HOURS);
    }

    @Test
    public void testNoSpec_successful_remote() {
        Mockito.when(remoteSpecProvider.spec(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(
                new ExternalSpecDetails(List.of("GSM"), List.of("GSM_900", "GSM_1800"), List.of(), List.of())
            ));
        Mockito.when(specRepository.save(Mockito.any())).thenAnswer(args -> args.getArgument(0));

        Spec actual = specService.specEnrich(phoneModel);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(List.of("GSM"), actual.getTechnologies());
        Assertions.assertEquals(List.of("GSM_900", "GSM_1800"), actual.get_2gBands());

        Mockito.verify(remoteSpecProvider).spec(VENDOR, MODEL);
        Mockito.verify(specRepository).save(Mockito.any());
        Mockito.verifyNoInteractions(localSpecProvider, specRefresher);
    }

    @Test
    public void testNoSpec_successful_local() {
        Mockito.when(remoteSpecProvider.spec(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Optional.empty());
        Mockito.when(localSpecProvider.spec(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(
                new ExternalSpecDetails(List.of("GSM"), List.of("GSM_900", "GSM_1800"), List.of(), List.of())
            ));
        Mockito.when(specRepository.save(Mockito.any())).thenAnswer(args -> args.getArgument(0));

        Spec actual = specService.specEnrich(phoneModel);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(List.of("GSM"), actual.getTechnologies());
        Assertions.assertEquals(List.of("GSM_900", "GSM_1800"), actual.get_2gBands());

        Mockito.verify(remoteSpecProvider).spec(VENDOR, MODEL);
        Mockito.verify(remoteSpecProvider).spec(VENDOR, MODEL);
        Mockito.verify(specRepository).save(Mockito.any());
        Mockito.verifyNoInteractions(specRefresher);
    }

    @Test
    public void testNoSpec_failed() {
        Mockito.when(remoteSpecProvider.spec(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());
        Mockito.when(localSpecProvider.spec(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());

        Spec actual = specService.specEnrich(phoneModel);

        Assertions.assertNull(actual);

        Mockito.verify(remoteSpecProvider).spec(VENDOR, MODEL);
        Mockito.verify(remoteSpecProvider).spec(VENDOR, MODEL);
        Mockito.verifyNoInteractions(specRepository, specRefresher);
    }

    @Test
    public void testSpecRefresh_ttlExpires() {
        baseSpec.setLastUpdatedAt(OffsetDateTime.now().minus(2, ChronoUnit.DAYS));
        OffsetDateTime lastUpdateStartedAt = OffsetDateTime.now().minus(2, ChronoUnit.HOURS);
        baseSpec.setLastUpdateStartedAt(lastUpdateStartedAt);
        phoneModel.setSpec(baseSpec);

        Mockito.doNothing().when(specRefresher).refreshSpec(Mockito.any());
        Mockito.when(specRepository.save(Mockito.any())).thenAnswer(args -> args.getArgument(0));

        Spec actual = specService.specEnrich(phoneModel);
        Assertions.assertTrue(actual.getLastUpdateStartedAt().isAfter(lastUpdateStartedAt));

        Mockito.verify(specRepository).save(actual);
        Mockito.verify(specRefresher).refreshSpec(baseSpec);
    }

    @Test
    public void testSpecRefresh_ttlExpires_withinGracePeriod() {
        baseSpec.setLastUpdatedAt(OffsetDateTime.now().minus(2, ChronoUnit.DAYS));
        baseSpec.setLastUpdateStartedAt(OffsetDateTime.now().minus(30, ChronoUnit.MINUTES));
        phoneModel.setSpec(baseSpec);

        Spec actual = specService.specEnrich(phoneModel);
        Assertions.assertSame(baseSpec, actual);

        Mockito.verifyNoInteractions(specRefresher, specRepository);
    }

    @Test
    public void testSpecRefresh_notNeeded() {
        baseSpec.setLastUpdatedAt(OffsetDateTime.now().minus(12, ChronoUnit.HOURS));
        baseSpec.setLastUpdateStartedAt(OffsetDateTime.now().minus(2, ChronoUnit.HOURS));
        phoneModel.setSpec(baseSpec);

        Spec actual = specService.specEnrich(phoneModel);
        Assertions.assertSame(baseSpec, actual);

        Mockito.verifyNoInteractions(specRefresher, specRepository);
    }
}
