package codes.bespoke.brastak.phones.spec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import codes.bespoke.brastak.phones.model.PhoneModel;
import codes.bespoke.brastak.phones.model.Spec;
import codes.bespoke.brastak.phones.repository.SpecRepository;
import codes.bespoke.brastak.phones.spec.model.ExternalSpecDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpecRefresherTest {
    private static final String MODEL = "Test Phone";
    private static final String VENDOR = "Test";

    @Mock
    @Remote
    private PhoneSpecProvider remoteSpecProvider;
    @Mock
    private SpecRepository specRepository;
    @InjectMocks
    private SpecRefresher refresher;

    @Captor
    private ArgumentCaptor<Spec> specCaptor;

    private final Spec spec;

    public SpecRefresherTest() {
        spec = new Spec();
        LocalDateTime timestamp = LocalDateTime.of(2023, Month.FEBRUARY, 18, 14, 22);
        spec.setLastUpdatedAt(OffsetDateTime.of(timestamp, ZoneOffset.ofHours(3)));
        spec.setLastUpdateStartedAt(OffsetDateTime.now());

        PhoneModel phoneModel = new PhoneModel();
        phoneModel.setId(1);
        phoneModel.setName(MODEL);
        phoneModel.setVendor(VENDOR);
        phoneModel.setSpec(spec);
        spec.setPhoneModel(phoneModel);

        spec.setTechnologies(List.of("GSM"));
        spec.set_2gBands(List.of("GSM_900", "GSM_1800"));
    }

    @Test
    public void testSuccessfulUpdate() {
        Mockito.when(remoteSpecProvider.spec(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(
                new ExternalSpecDetails(List.of("GSM"), List.of("GSM_900", "GSM_1900"), List.of(), List.of())
            ));
        Mockito.when(specRepository.save(specCaptor.capture())).thenAnswer(args -> args.getArgument(0));

        refresher.refreshSpec(spec);

        Mockito.verify(remoteSpecProvider).spec(VENDOR, MODEL);
        Mockito.verify(specRepository).save(Mockito.any());

        Spec updatedSpec = specCaptor.getValue();
        Assertions.assertEquals(List.of("GSM"), updatedSpec.getTechnologies());
        Assertions.assertEquals(List.of("GSM_900", "GSM_1900"), updatedSpec.get_2gBands());
    }

    @Test
    public void testNoExternalSpecFound() {
        Mockito.when(remoteSpecProvider.spec(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());

        refresher.refreshSpec(spec);

        Mockito.verify(remoteSpecProvider).spec(VENDOR, MODEL);
        Mockito.verifyNoInteractions(specRepository);
    }
}
