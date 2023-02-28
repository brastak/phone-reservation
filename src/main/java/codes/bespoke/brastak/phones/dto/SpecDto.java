package codes.bespoke.brastak.phones.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpecDto(List<String> technologies, @JsonProperty("2gBands") List<String> _2gBands,
                      @JsonProperty("3gBands") List<String> _3gBands, @JsonProperty("4gBands") List<String> _4gBands) {
}
