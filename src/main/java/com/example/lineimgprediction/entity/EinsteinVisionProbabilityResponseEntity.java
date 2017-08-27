package com.example.lineimgprediction.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EinsteinVisionProbabilityResponseEntity {
    @JsonProperty("label")
    private String label;
    @JsonProperty("probability")
    private float probability;
}
