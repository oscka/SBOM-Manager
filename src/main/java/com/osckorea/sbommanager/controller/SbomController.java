package com.osckorea.sbommanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osckorea.sbommanager.domian.dto.SbomDTO;
import com.osckorea.sbommanager.domian.entity.Sbom;
import com.osckorea.sbommanager.service.OauthService;
import com.osckorea.sbommanager.service.SbomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/sample-api/v1/test")
@RequiredArgsConstructor
@Tag(name = "Sample SBOM API", description = "샘플 SBOM API 그룹")
@Slf4j
public class SbomController {

    private final SbomService sbomService;
    private final OauthService oauthService;

    @Operation(summary = "Get All SBOM", description = "모든 SBOM을 가져옵니다.")
    @GetMapping("/sbom")
    public ResponseEntity<Iterable<Sbom>> getAllUser02() {
        Iterable<Sbom> sboms  = sbomService.getAllSbom();
        return ResponseEntity.ok(sboms);
    }

    @Operation(summary = "SBOM Create", description = "SBOM을 저장합니다.")
    @PostMapping("/sbom")
    public ResponseEntity<Sbom> createUser(@RequestBody String sbomJson, @RequestHeader("X-Forwarded-Email") String user) throws Exception {
        Sbom createdSbom = sbomService.createSbom(sbomJson, user);
        return new ResponseEntity<>(createdSbom, HttpStatus.CREATED);
    }

    @Operation(summary = "Get All SBOM", description = "특정 SBOM을 가져옵니다.")
    @GetMapping("/sbom/{id}")
    public ResponseEntity<SbomDTO> getSbom(@PathVariable("id") Long id) throws IOException {
        SbomDTO sbom = sbomService.getSbomDTO(id);

        return ResponseEntity.ok(sbom);
    }

}
