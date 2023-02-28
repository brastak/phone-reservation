package codes.bespoke.brastak.phones.spec;

import java.io.IOException;
import java.util.Optional;

import codes.bespoke.brastak.phones.spec.model.ExternalSpecDetails;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class LocalPhoneSpecProviderTest {
    private final LocalPhoneSpecProvider localSpecProvider;

    public LocalPhoneSpecProviderTest() throws CsvValidationException, IOException {
        localSpecProvider = new LocalPhoneSpecProvider();
        localSpecProvider.localSpecFile = new ClassPathResource("devices_spec.csv");
        localSpecProvider.loadLocalSpecs();
    }

    @Test
    public void testLocalSpecs() {
        Assertions.assertFalse(localSpecProvider.spec("foo", "bar").isPresent());

        Optional<ExternalSpecDetails> iPhoneOpt = localSpecProvider.spec("Apple", "iPhone 13");
        Assertions.assertTrue(iPhoneOpt.isPresent());

        ExternalSpecDetails spec = iPhoneOpt.get();
        Assertions.assertTrue(spec.technologies().contains("5G"));
        Assertions.assertTrue(spec._2gBands().contains("CDMA_800"));
        Assertions.assertTrue(spec._3gBands().contains("HSDPA_1900"));
        Assertions.assertTrue(spec._4gBands().contains("4G_4"));
    }
}
