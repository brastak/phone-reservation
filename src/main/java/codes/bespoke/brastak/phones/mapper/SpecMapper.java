package codes.bespoke.brastak.phones.mapper;

import codes.bespoke.brastak.phones.dto.SpecDto;
import codes.bespoke.brastak.phones.model.Spec;
import codes.bespoke.brastak.phones.spec.model.ExternalSpecDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpecMapper {
    SpecDto specToSpecDto(Spec spec);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phoneModel", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "lastUpdateStartedAt", ignore = true)
    Spec externalSpecToSpec(ExternalSpecDetails external);

}
