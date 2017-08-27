package com.example.lineimgprediction.service;

import com.example.lineimgprediction.entity.EinsteinVisionImageBase64RequestEntity;
import com.example.lineimgprediction.entity.EinsteinVisionPredictionResponseEntity;
import com.example.lineimgprediction.properties.EinsteinVisionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EinsteinVisionPredictionServiceImpl implements EinsteinVisionPredictionService {

    @Autowired
    private EinsteinVisionProperties einsteinVisionProperties;

    @Autowired
    private EinsteinVisionTokenCreateService einsteinVisionTokenCreateService;

    public EinsteinVisionPredictionResponseEntity predictionWithImageBase64String(final String imageBase64String) {
        final String accessToken = einsteinVisionTokenCreateService.getAccessToken();

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        httpHeaders.set("Cache-Control", "no-cache");
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        final EinsteinVisionImageBase64RequestEntity einsteinVisionImageBase64RequestEntity =
                new EinsteinVisionImageBase64RequestEntity();
        einsteinVisionImageBase64RequestEntity.setModelId("GeneralImageClassifier");
        einsteinVisionImageBase64RequestEntity.setNumResults(3);
        einsteinVisionImageBase64RequestEntity.setSampleBase64Content(imageBase64String);
        einsteinVisionImageBase64RequestEntity.setSampleId("Image Prediction");

        HttpEntity<EinsteinVisionImageBase64RequestEntity> httpEntity =
                new HttpEntity<>(einsteinVisionImageBase64RequestEntity, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<EinsteinVisionPredictionResponseEntity> responseEntity =
                restTemplate.exchange(
                        einsteinVisionProperties.getUrl(),
                        HttpMethod.POST,
                        httpEntity,
                        EinsteinVisionPredictionResponseEntity.class);

        return responseEntity.getBody();
    }
}
