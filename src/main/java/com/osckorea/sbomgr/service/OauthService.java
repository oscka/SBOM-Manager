package com.osckorea.sbomgr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final RestTemplate restTemplate;

    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String GITHUB_USER_INFO_URL = "https://api.github.com/user";

    public Map<String, Object> getUserInfo(String accessToken, String host) throws Exception {
        if ("localhost:4180".equals(host)) {
            return fetchUserInfo(GOOGLE_USER_INFO_URL, accessToken, null);
        } else if ("localhost:4181".equals(host)) {
            return fetchUserInfo(GITHUB_USER_INFO_URL, accessToken, "application/vnd.github.v3+json");
        } else {
            throw new IllegalArgumentException("Unknown host: " + host);
        }
    }

    private Map<String, Object> fetchUserInfo(String url, String accessToken, String acceptHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        if (acceptHeader != null) {
            headers.set("Accept", acceptHeader);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

}
