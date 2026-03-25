package com.tcc.antifraude_seguro.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

@Service
public class MlFraudeClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ML_URL = "http://localhost:5001/predict";

    public Double obterProbabilidadeFraude(double valor) {
        try {
            // Monta vetor de 29 features (V1-V28 zerados + Amount)
            Double[] features = new Double[29];
            for (int i = 0; i < 28; i++) features[i] = 0.0;
            features[28] = valor;

            Map<String, Object> body = Map.of("features", features);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ML_URL, request, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ((Number) response.getBody().get("fraud_probability")).doubleValue();
            }
        } catch (Exception e) {
            System.out.println("ML service indisponivel, usando fallback: " + e.getMessage());
        }
        return null; // null = fallback para regras
    }
}