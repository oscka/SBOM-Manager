package com.osckorea.sbommanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osckorea.sbommanager.domian.dto.SbomDTO;
import com.osckorea.sbommanager.domian.entity.Sbom;
import com.osckorea.sbommanager.domian.enums.SbomConstants;
import com.osckorea.sbommanager.repository.SbomRepository;
import com.osckorea.sbommanager.util.json.SbomJsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SbomService {
    private final SbomRepository sbomRepository;
    private final SbomJsonParser sbomJsonParser;
    private final OauthService oauthService;

    @Transactional
    public Iterable<Sbom> getAllSbom() {
        return sbomRepository.findAll();
    }

    @Transactional
    public SbomDTO getSbomDTO(UUID uuid) throws IOException {

        Sbom sbom = sbomRepository.findByUuid(uuid).orElseThrow(() -> new RuntimeException("SBOM not found"));
        SbomConstants.BomFormat format = sbomJsonParser.parseSbomFormat(sbom.getSbomJson());

        List<SbomDTO.ComponentInfo> componentInfoList;

        switch (format) {
            case CYCLONEDX:
                componentInfoList = sbomJsonParser.componentInfosParseCDX(sbom.getSbomJson());
                break;
            case SPDX:
                componentInfoList = sbomJsonParser.componentInfosParseSPDX(sbom.getSbomJson());
                break;
            default:
                throw new IllegalArgumentException("Unsupported SBOM format");
        }

        SbomDTO sbomDto = SbomDTO.builder()
                .name(sbom.getName())
                .sbomFormat(sbom.getBomFormat())
                .type(sbom.getComponentType())
                .componentCount(sbom.getComponentCount())
                .componentInfoList(componentInfoList)
                .analyst(sbom.getCreatedBy())
                .analysisDateTime(sbom.getCreatedAt())
                .build();

        return sbomDto;
    }

    @Transactional
    public Sbom createSbom(String sbomJson, String user) throws Exception {

        SbomConstants.BomFormat format = sbomJsonParser.parseSbomFormat(sbomJson);
        Sbom sbom;

        switch (format) {
            case CYCLONEDX:
                sbom = sbomJsonParser.parseCycloneDXSbom(sbomJson);
                break;
            case SPDX:
                sbom = sbomJsonParser.parseSpdxSbom(sbomJson);
                break;
            default:
                throw new IllegalArgumentException("Unsupported SBOM format");
        }

        sbom.setCreatedAt(LocalDateTime.now());
        sbom.setSbomJson(sbomJson);
        sbom.setCreatedBy(user);
        sbom.setUuid(UUID.randomUUID());
        return sbomRepository.save(sbom);
    }

}
