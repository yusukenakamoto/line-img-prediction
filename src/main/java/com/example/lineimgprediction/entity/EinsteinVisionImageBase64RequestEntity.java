package com.example.lineimgprediction.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EinsteinVisionImageBase64RequestEntity {
    @JsonProperty("modelId")
    private String modelId;
    @JsonProperty("numResults")
    private int numResults;
    @JsonProperty("sampleBase64Content")
    private String sampleBase64Content;
    @JsonProperty("sampleId")
    private String sampleId;
}
