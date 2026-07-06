package io.github.soheshts.searchnl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchCriteria {

    private String gender;

    @JsonProperty("baseColour")
    private String baseColour;

    @JsonProperty("articleType")
    private String articleType;

    private String season;
    private String usage;

    @JsonProperty("price_max")
    private Double priceMax;

    @JsonProperty("price_min")
    private Double priceMin;

    @JsonProperty("semantic_query")
    private String semanticQuery;


}