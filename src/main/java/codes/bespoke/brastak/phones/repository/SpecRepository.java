package codes.bespoke.brastak.phones.repository;

import codes.bespoke.brastak.phones.model.Spec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecRepository extends JpaRepository<Spec, Integer> {
}
