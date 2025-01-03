package com.osckorea.sbommanager.service;

import com.osckorea.sbommanager.domian.entity.Sbom;
import com.osckorea.sbommanager.domian.enums.SbomConstants;
import com.osckorea.sbommanager.repository.SbomRepository;
import com.osckorea.sbommanager.util.json.SbomJsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SbomService {
    private final SbomRepository sbomRepository;
    private final SbomJsonParser sbomJsonParser;

    @Transactional
    public Iterable<Sbom> getAllSbom() {
        return sbomRepository.findAll();
    }

    @Transactional
    public Sbom createSbom(String sbomJson) throws Exception {

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

        sbom.setSbomJson(sbomJson);
        return sbomRepository.save(sbom);
    }

}
