package com.example.lineimgprediction.service;

import com.example.lineimgprediction.entity.EinsteinVisionPredictionResponseEntity;

public interface EinsteinVisionPredictionService {
    EinsteinVisionPredictionResponseEntity predictionWithImageBase64String(final String imageBase64String);
}
