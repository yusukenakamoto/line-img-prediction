package com.example.lineimgprediction.service;

import com.example.lineimgprediction.entity.EinsteinVisionTokenResponseEntity;
import com.example.lineimgprediction.properties.EinsteinVisionProperties;
import org.apache.commons.codec.binary.Base64;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.UUID;

/**
 * Einstein Visionのアクセストークンを取得する実装クラス.
 */
@Service
public class EinsteinVisionTokenServiceImpl implements EinsteinVisionTokenService {

    @Autowired
    private EinsteinVisionProperties einsteinVisionProperties;

    /**
     * アクセストークンを取得する.
     * @return アクセストークン
     */
    public String getAccessToken() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("assertion", createJwtAssertion());
        bodyMap.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

        final HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        final ResponseEntity<EinsteinVisionTokenResponseEntity> responseEntity =
                restTemplate.postForEntity(
                        einsteinVisionProperties.getTokenUrl(),
                        httpEntity,
                        EinsteinVisionTokenResponseEntity.class);

        return responseEntity.getBody().getAccessToken();
    }

    /**
     * JWTアサーションを作成する.
     * @return JWTアサーション
     */
    private String createJwtAssertion() {
        final JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setSubject(einsteinVisionProperties.getAccountId());
        jwtClaims.setAudience(einsteinVisionProperties.getTokenUrl());
        jwtClaims.setExpirationTimeMinutesInTheFuture(einsteinVisionProperties.getExpiryInSeconds() / 60);
        jwtClaims.setIssuedAtToNow();

        // generate the payload
        final JsonWebSignature jwt = new JsonWebSignature();
        jwt.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jwt.setPayload(jwtClaims.toJson());
        jwt.setKeyIdHeaderValue(UUID.randomUUID().toString());

        // sign using the private key
        jwt.setKey(createPrivateKey());

        try {
            return jwt.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * プライベートキーを作成する.
     * @return プライベートキー
     */
    private PrivateKey createPrivateKey() {
        final String privateKeyBase64 = einsteinVisionProperties.getPrivateKey();
        String privateKeyPEM =
                privateKeyBase64.replace("-----BEGIN RSA PRIVATE KEY-----\n", "")
                                .replace("\n-----END RSA PRIVATE KEY-----", "");

        // Base64 decode the data
        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        try {
            DerInputStream derReader = new DerInputStream(encoded);
            DerValue[] seq = derReader.getSequence(0);

            // skip version seq[0];
            BigInteger modulus = seq[1].getBigInteger();
            BigInteger publicExp = seq[2].getBigInteger();
            BigInteger privateExp = seq[3].getBigInteger();
            BigInteger primeP = seq[4].getBigInteger();
            BigInteger primeQ = seq[5].getBigInteger();
            BigInteger expP = seq[6].getBigInteger();
            BigInteger expQ = seq[7].getBigInteger();
            BigInteger crtCoeff = seq[8].getBigInteger();

            RSAPrivateCrtKeySpec keySpec =
                    new RSAPrivateCrtKeySpec(
                            modulus,
                            publicExp,
                            privateExp,
                            primeP,
                            primeQ,
                            expP,
                            expQ,
                            crtCoeff);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }
}
