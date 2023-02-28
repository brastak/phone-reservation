package codes.bespoke.brastak.phones.repository;

import java.util.Optional;

import codes.bespoke.brastak.phones.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Integer> {
    @Query(value = """
        select p.*
        from phone p
        left outer join phone_reservation r on r.phone_id = p.id and r.returned_at is null
        where p.phone_model_id = :id and r.reserved_at is null
        for no key update of p skip locked
        limit 1
        """, nativeQuery = true)
    Optional<Phone> findUnreservedPhoneByModelId(Integer id);
}
