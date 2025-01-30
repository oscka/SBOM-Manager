package com.osckorea.sbommanager.controller;

import com.osckorea.sbommanager.domian.dto.SbomDTO;
import com.osckorea.sbommanager.domian.dto.SbomVulnDTO;
import com.osckorea.sbommanager.domian.entity.Sbom;
import com.osckorea.sbommanager.service.SbomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/sample-api/v1/test")
@RequiredArgsConstructor
@Tag(name = "Sample SBOM API", description = "샘플 SBOM API 그룹")
@Slf4j
public class SbomController {

    private final SbomService sbomService;

    @Operation(summary = "Get All SBOM", description = "모든 SBOM을 가져옵니다.")
    @GetMapping("/sbom")
    public ResponseEntity<Iterable<Sbom>> getAllUser() {
        Iterable<Sbom> sboms  = sbomService.getAllSbom();
        return ResponseEntity.ok(sboms);
    }

    @Operation(summary = "Get SBOM", description = "SBOM(분석) 조회")
    @GetMapping("/sbom/{uuid}")
    public ResponseEntity<SbomDTO> getSbom(@PathVariable("uuid") UUID uuid) throws IOException {
        SbomDTO sbom = sbomService.getSbomDTO(uuid);

        return ResponseEntity.ok(sbom);
    }

    @Operation(summary = "SBOM Create", description = "SBOM을 저장합니다(사용자 ID 저장을 위한 Oauth2 인증 필수).")
    @PostMapping("/sbom")
    public ResponseEntity<Sbom> createUser(@RequestBody String sbomJson, @RequestHeader("X-Forwarded-Email") String user) throws Exception {
        Sbom createdSbom = sbomService.createSbom(sbomJson, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSbom);
    }

    @Operation(summary = "Get Vuln SBOM", description = "SBOM 취약점(분석) 조회")
    @GetMapping("/sbom/vuln/{uuid}")
    public ResponseEntity<SbomVulnDTO> getSbomVuln(@PathVariable("uuid") UUID uuid) throws IOException {
        SbomVulnDTO sbom = sbomService.getSbomVulnDTO(uuid);

        return ResponseEntity.ok(sbom);
    }

}
