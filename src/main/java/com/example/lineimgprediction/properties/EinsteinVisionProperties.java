package com.example.lineimgprediction.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "einsteinVision")
public class EinsteinVisionProperties {
    private String accountId;
    private String privateKey;
    private String url;
    private float expiryInSeconds;
    private String tokenUrl;
    private String predictUrl;
    private String modelId;
}
