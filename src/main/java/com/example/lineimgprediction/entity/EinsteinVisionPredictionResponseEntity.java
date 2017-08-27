package com.example.lineimgprediction.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EinsteinVisionPredictionResponseEntity {
    @JsonProperty("message")
    private String message;
    @JsonProperty("object")
    private String object;
    @JsonProperty("probabilities")
    private List<EinsteinVisionProbabilityResponseEntity> probabilities;
    @JsonProperty("sampleId")
    private String sampleId;
    @JsonProperty("status")
    private String status;
}
