package com.example.lineimgprediction.service;

import com.example.lineimgprediction.entity.EinsteinVisionImageBase64RequestEntity;
import com.example.lineimgprediction.entity.EinsteinVisionPredictionResponseEntity;
import com.example.lineimgprediction.properties.EinsteinVisionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
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

        final MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("modelId", "FoodImageClassifier");
        parts.add("sampleBase64Content", imageBase64String);
        parts.add("numResults", 3);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

//        final EinsteinVisionImageBase64RequestEntity einsteinVisionImageBase64RequestEntity =
//                new EinsteinVisionImageBase64RequestEntity();
//        einsteinVisionImageBase64RequestEntity.setModelId("GeneralImageClassifier");
//        einsteinVisionImageBase64RequestEntity.setNumResults(3);
//        einsteinVisionImageBase64RequestEntity.setSampleBase64Content(imageBase64String);
//        einsteinVisionImageBase64RequestEntity.setSampleId("Image Prediction");

//        HttpEntity<EinsteinVisionImageBase64RequestEntity> httpEntity =
//                new HttpEntity<>(einsteinVisionImageBase64RequestEntity, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<EinsteinVisionPredictionResponseEntity> responseEntity =
                restTemplate.exchange(
                        einsteinVisionProperties.getPredictUrl(),
                        HttpMethod.POST,
                        httpEntity,
                        EinsteinVisionPredictionResponseEntity.class);

        log.info("****Predict Response:" + responseEntity);

        return responseEntity.getBody();
    }
}
