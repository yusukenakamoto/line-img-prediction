package com.example.lineimgprediction.service;

import com.example.lineimgprediction.entity.EinsteinVisionPredictionResponseEntity;

/**
 * Einstein Visionで画像認識を行うクラスのインタフェース.
 */
public interface EinsteinVisionPredictionService {
    public EinsteinVisionPredictionResponseEntity predictionWithImageBase64String(final String imageBase64String);
}
