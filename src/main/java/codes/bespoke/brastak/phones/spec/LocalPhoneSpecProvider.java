package codes.bespoke.brastak.phones.spec;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import codes.bespoke.brastak.phones.spec.model.ExternalSpecDetails;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Local
public class LocalPhoneSpecProvider implements PhoneSpecProvider {
    @Value("${phones.spec.location}")
    Resource localSpecFile;
    private final Map<String, ExternalSpecDetails> localSpecs = new HashMap<>();

    @PostConstruct
    void loadLocalSpecs() throws IOException, CsvValidationException {
        log.debug("Load phone spec info from {}", localSpecFile.getFilename());
        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(localSpecFile.getInputStream()))
                 .withCSVParser(csvParser).withSkipLines(1).build()) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                ExternalSpecDetails spec = new ExternalSpecDetails(
                    parseTokens(row[2]),
                    parseTokens(row[3]),
                    parseTokens(row[4]),
                    parseTokens(row[5])
                );
                localSpecs.put(row[0] + row[1], spec);
            }
        }
        log.debug("{} specs successfully loaded", localSpecs.size());
    }

    private List<String> parseTokens(String field) {
        if(field.isBlank()) return null;
        return Arrays.stream(field.split("\\|")).map(String::strip).toList();
    }

    @Override
    public Optional<ExternalSpecDetails> spec(String vendor, String model) {
        log.debug("Find spec by vendor {} and model {}", vendor, model);
        Optional<ExternalSpecDetails> specOpt = Optional.ofNullable(localSpecs.get(vendor + " " + model));
        log.debug("Found spec {}", specOpt);
        return specOpt;
    }
}
