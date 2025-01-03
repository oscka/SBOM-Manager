package com.osckorea.sbommanager.controller;

import com.osckorea.sbommanager.domian.entity.Sbom;
import com.osckorea.sbommanager.service.SbomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sample-api/v1/test/managed")
@RequiredArgsConstructor
@Tag(name = "Sample SBOM API", description = "샘플 SBOM API 그룹")
@Slf4j
public class SbomController {

    private final SbomService sbomService;

    @Operation(summary = "Get All SBOM", description = "모든 SBOM을 가져옵니다.")
    @GetMapping("/sbom")
    public ResponseEntity<Iterable<Sbom>> getAllUser02() {
        Iterable<Sbom> sboms  = sbomService.getAllSbom();
        return ResponseEntity.ok(sboms);
    }

    @Operation(summary = "SBOM Create", description = "SBOM을 저장합니다.")
    @PostMapping("/sbom")
    public ResponseEntity<Sbom> createUser(@RequestBody String sbomJson) throws Exception {
        Sbom createdSbom = sbomService.createSbom(sbomJson);
        return new ResponseEntity<>(createdSbom, HttpStatus.CREATED);
    }

}
