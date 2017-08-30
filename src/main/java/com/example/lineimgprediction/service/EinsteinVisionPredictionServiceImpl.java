package com.example.lineimgprediction.service;

import com.example.lineimgprediction.entity.EinsteinVisionPredictionResponseEntity;
import com.example.lineimgprediction.properties.EinsteinVisionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Einstein Visionで画像認識を行う実装クラス.
 */
@Service
public class EinsteinVisionPredictionServiceImpl implements EinsteinVisionPredictionService {

    @Autowired
    private EinsteinVisionProperties einsteinVisionProperties;

    @Autowired
    private EinsteinVisionTokenService einsteinVisionTokenCreateService;

    /**
     * Base64の画像情報をEinstein Visionで認識する.
     * @param imageBase64String Base64の画像情報
     * @return 認識結果
     */
    public EinsteinVisionPredictionResponseEntity predictionWithImageBase64String(final String imageBase64String) {
        // アクセストークンを取得する
        final String accessToken = einsteinVisionTokenCreateService.getAccessToken();

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        httpHeaders.set("Cache-Control", "no-cache");
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        final MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("modelId", einsteinVisionProperties.getModelId());
        parts.add("sampleBase64Content", imageBase64String);
        parts.add("numResults", 5);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        // Prediction with Image Base64 String
        ResponseEntity<EinsteinVisionPredictionResponseEntity> responseEntity =
                restTemplate.postForEntity(
                        einsteinVisionProperties.getPredictUrl(),
                        httpEntity,
                        EinsteinVisionPredictionResponseEntity.class);

        return responseEntity.getBody();
    }
}
