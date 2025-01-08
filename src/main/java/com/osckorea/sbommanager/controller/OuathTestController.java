package com.osckorea.sbommanager.controller;

import com.osckorea.sbommanager.service.OauthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/sample-api/v1/test")
@RequiredArgsConstructor
@Tag(name = "Oauth API", description = "샘플 Oauth 인증 API 그룹")
@Slf4j
public class OuathTestController {

    private final OauthService oauthService;

    // (Google AND GitHub)인증 성공 시 api 사용을 위한 토큰 및 헤더 값 출력
    @GetMapping("/login")
    public String login() {
        return "";
    }

    // (Google AND GitHub)인증 성공 시 Proxy Server로 부터 받은 헤더 정보로 부터의 사용자 정보 출력
    @GetMapping("/email")
    public String getUserEmail(@RequestHeader(name = "X-Forwarded-Email", required = false) String email, HttpServletRequest request) {
        if (email != null && !email.isEmpty()) {
            return "Authenticated user email: " + email;
        } else {
            return "No authenticated user email found";
        }
    }

    // (Google, Github) 인증 성공 시 Proxy Server로 부터 받은 액세스 토큰을 사용해 Google 리소스 서버로 부터 받아온 사용자 정보 출력 API
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader("X-Forwarded-Access-Token") String accessToken, @RequestHeader("host") String host) {
        try {
            Map<String, Object> userInfo = oauthService.getUserInfo(accessToken, host);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user info");
        }
    }

}
