package codes.bespoke.brastak.phones.service;

import java.util.List;
import java.util.Optional;

import codes.bespoke.brastak.phones.model.Phone;
import codes.bespoke.brastak.phones.repository.PhoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhoneService {
    private final PhoneRepository phoneRepository;

    public List<Phone> findAll() {
        return phoneRepository.findAll();
    }

    public Optional<Phone> findById(Integer id) {
        return phoneRepository.findById(id);
    }

    public Optional<Phone> findUnreservedPhone(int modelId) {
        return phoneRepository.findUnreservedPhoneByModelId(modelId);
    }
}
