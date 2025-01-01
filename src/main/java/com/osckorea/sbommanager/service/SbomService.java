package com.osckorea.sbommanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.osckorea.sbommanager.model.Sbom;
import com.osckorea.sbommanager.repository.SbomRepository;
import com.osckorea.sbommanager.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class SbomService {
    private final SbomRepository sbomRepository;
    private final Util util;

    @Transactional
    public Iterable<Sbom> getAllSbom() {
        return sbomRepository.findAll();
    }

    @Transactional
    public Sbom createSbom(String sbomJson) throws Exception {
        Sbom sbom = new Sbom();

        util.test(sbomJson);

        sbom.setName(util.extractMetadataComponentName(sbomJson));
        sbom.setSbomJson(sbomJson);
        return sbomRepository.save(sbom);
    }

}
