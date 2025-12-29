package guru.springframework.springaiintro.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record GetCapitalWithInfoResponse(

        @JsonPropertyDescription("The city name")
        String answer,

        @JsonPropertyDescription("The population of the city")
        Integer population,

        @JsonPropertyDescription("The region where the city is located")
        String region,

        @JsonPropertyDescription("The primary language spoken in the city")
        String language,

        @JsonPropertyDescription("The currency used in the city")
        String currency
) {
}
