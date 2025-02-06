package com.osckorea.sbomgr.service;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osckorea.sbomgr.domian.dto.SbomDTO;
import com.osckorea.sbomgr.domian.dto.SbomVulnDTO;
import com.osckorea.sbomgr.domian.entity.NvdCveParseItem;
import com.osckorea.sbomgr.domian.entity.Sbom;
import com.osckorea.sbomgr.domian.enums.SbomConstants;
import com.osckorea.sbomgr.repository.NvdCveParseRepository;
import com.osckorea.sbomgr.repository.SbomRepository;
import com.osckorea.sbomgr.util.json.SbomJsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SbomService {
    private final SbomRepository sbomRepository;
    private final SbomJsonParser sbomJsonParser;
    private final NvdCveParseRepository nvdCveParseRepository;

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

    @Transactional
    public SbomVulnDTO getSbomVulnDTO(UUID uuid) throws IOException {
        Sbom sbom = sbomRepository.findByUuid(uuid).orElseThrow(() -> new RuntimeException("SBOM not found"));
        // componentCpesLicenseInfo에는 각 컴포넌트의 cpe들과 license 정보가 들어가있음
        List<SbomDTO.ComponentCpesLicenseInfo> componentInfoList = sbomJsonParser.componentInfosParseForVuln(sbom.getSbomJson());

        List<SbomVulnDTO.VulnComponentInfo> vulnComponentInfoList = new ArrayList<>();

        int vulnCount = 0;

        for (SbomDTO.ComponentCpesLicenseInfo componentCpesLicenseInfo : componentInfoList) {
            String bomRef = componentCpesLicenseInfo.getBomRef();
            List<String> licensesInfo = componentCpesLicenseInfo.getLicenses();

            for (String cpe : componentCpesLicenseInfo.getCpes()){
                String[] result = normalizeCpeUri(cpe);
                String allCepUri = cpe;
                String normalizedCpeUri = result[0];
                String version = result[1];

                List<NvdCveParseItem> matchingItems = nvdCveParseRepository.findMatchingConfigurations(allCepUri, normalizedCpeUri, version);

                if (!matchingItems.isEmpty()) {
                    vulnComponentInfoList.addAll(processVulnerabilities(matchingItems, allCepUri, normalizedCpeUri, version, bomRef, licensesInfo));
                }
            }
        }

        // 중복 제거를 위한 Set으로 변환 후 다시 List로 변환
        Set<SbomVulnDTO.VulnComponentInfo> uniqueComponents = new HashSet<>(vulnComponentInfoList);
        vulnComponentInfoList = new ArrayList<>(uniqueComponents);

        vulnCount = vulnComponentInfoList.size();

        SbomVulnDTO sbomVulnDto = SbomVulnDTO.builder()
                .name(sbom.getName())
                .vulnComponentCount(vulnCount)
                .allComponentCount(sbom.getComponentCount())
                .vulncomponentInfoList(vulnComponentInfoList)
                .build();

        return sbomVulnDto;
    }

    private List<SbomVulnDTO.VulnComponentInfo> processVulnerabilities(List<NvdCveParseItem> matchingItems, String allCepUri, String normalizedCpeUri, String version, String bomRef, List<String> licenseInfo) throws JsonProcessingException {
        List<SbomVulnDTO.VulnComponentInfo> componentInfoList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (NvdCveParseItem item : matchingItems) {

            JsonNode rootNodes = objectMapper.readTree(item.getNvdConfJson());

            for (JsonNode subNode : rootNodes) {
                // 각 배열 항목의 operator 추출, 기본값은 "OR"
                String operator = subNode.path("operator").asText("OR");
                processNode(subNode, allCepUri, normalizedCpeUri, version, item, componentInfoList, operator, null, bomRef, licenseInfo);
            }
        }

        return componentInfoList;
    }

    private void processNode(JsonNode node, String allCepUri, String normalizedCpeUri, String version, NvdCveParseItem item, List<SbomVulnDTO.VulnComponentInfo> componentInfoList, String parentOperator, String thisOperator, String bomRef, List<String> licenseInfo) {

        // 1. 현재 노드의 cpe_match 탐색 및 처리
        JsonNode cpeMatchNodes = node.path("cpe_match");
        if (!cpeMatchNodes.isEmpty()){
            processCpeMatches(cpeMatchNodes, allCepUri, normalizedCpeUri, version, item, componentInfoList, parentOperator, thisOperator, bomRef, licenseInfo);
        }

        parentOperator = node.path("operator").asText();

        // 2. 현재 노드의 children 속성에 대해 재귀적으로 탐색
        for (JsonNode childNode : node.path("children")) {
            // 자식 노드에서 operator 값을 추출
            thisOperator = childNode.path("operator").asText(null);
            // operator가 없다면 부모의 operator를 사용
            processNode(childNode, allCepUri, normalizedCpeUri, version, item, componentInfoList, parentOperator, thisOperator, bomRef, licenseInfo);
        }
    }

    private void processCpeMatches(JsonNode cpeMatchNodes, String allCepUri, String normalizedCpeUri, String version,
                                   NvdCveParseItem item, List<SbomVulnDTO.VulnComponentInfo> componentInfoList, String parentOperator,
                                   String thisOperator, String bomRef, List<String> licenseInfo) {
        for (JsonNode cpeMatchNode : cpeMatchNodes) {
            String cpe23Uri = cpeMatchNode.path("cpe23Uri").asText();
            boolean isVulnerable = cpeMatchNode.path("vulnerable").asBoolean();

            // 1. allCepUri 확인 (추가 버전 확인 없이 바로 처리)
            if (cpe23Uri.equals(allCepUri)) {
                processOperator(cpeMatchNode, isVulnerable, item, componentInfoList, parentOperator, thisOperator, allCepUri, bomRef, licenseInfo);
            }
            // 2. normalizedCpeUri 확인 및 버전 포함 여부 확인
            else if (cpe23Uri.equals(normalizedCpeUri)) {
                boolean versionIncluded = isVersionIncluded(cpeMatchNode, version);
                if (versionIncluded) {
                    processOperator(cpeMatchNode, isVulnerable, item, componentInfoList, parentOperator, thisOperator, allCepUri, bomRef, licenseInfo);
                };
            }
        }
    }

    private boolean isVersionIncluded(JsonNode cpeMatchNode, String version) {
        if (!isValidVersion(version)) {
            handleInvalidVersion(version);
            return true;
        }

        String versionStartIncluding = cpeMatchNode.has("versionStartIncluding") ? cpeMatchNode.path("versionStartIncluding").asText() : null;
        String versionStartExcluding = cpeMatchNode.has("versionStartExcluding") ? cpeMatchNode.path("versionStartExcluding").asText() : null;
        String versionEndIncluding = cpeMatchNode.has("versionEndIncluding") ? cpeMatchNode.path("versionEndIncluding").asText() : null;
        String versionEndExcluding = cpeMatchNode.has("versionEndExcluding") ? cpeMatchNode.path("versionEndExcluding").asText() : null;

        if ((versionStartIncluding != null && !isValidVersion(versionStartIncluding)) ||
                (versionStartExcluding != null && !isValidVersion(versionStartExcluding)) ||
                (versionEndIncluding != null && !isValidVersion(versionEndIncluding)) ||
                (versionEndExcluding != null && !isValidVersion(versionEndExcluding))) {
            return true;
        }

        boolean startIncluding = versionStartIncluding == null || compareVersions(version, versionStartIncluding) >= 0;
        boolean startExcluding = versionStartExcluding == null || compareVersions(version, versionStartExcluding) > 0;
        boolean endIncluding = versionEndIncluding == null || compareVersions(version, versionEndIncluding) <= 0;
        boolean endExcluding = versionEndExcluding == null || compareVersions(version, versionEndExcluding) < 0;

        return startIncluding && startExcluding && endIncluding && endExcluding;
    }

    private boolean isValidVersion(String version) {
        return version.matches("^[0-9]+(\\.[0-9]+)*$");
    }

    private void handleInvalidVersion(String version) {
        System.out.println("Invalid version format detected: " + version);
    }

    private int compareVersions(String version1, String version2) {
        String[] parts1 = normalizeVersion(version1).split("\\.");
        String[] parts2 = normalizeVersion(version2).split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    private String normalizeVersion(String version) {
        return Arrays.stream(version.split("\\."))
                .map(part -> String.valueOf(Integer.parseInt(part)))
                .reduce((a, b) -> a + "." + b)
                .orElse("");
    }

    private void processOperator(JsonNode node, boolean isVulnerable, NvdCveParseItem item, List<SbomVulnDTO.VulnComponentInfo> componentInfoList,
                                 String parentOperator, String thisOperator, String allCpeUri,String bomRef, List<String> licenseInfo) {
        if (isVulnerable) {
            String operator = thisOperator;

            List<String> allCpeUris = new ArrayList<>();
            allCpeUris.add(allCpeUri);

            if ("OR".equalsIgnoreCase(operator) || operator == null) {
                // TODO: OR 연산 처리 완료 후 상위 operator가 AND인 경우 추가 처리 필요 하위는 추가 처리 필요 X
                SbomVulnDTO.VulnComponentInfo vulnInfo = SbomVulnDTO.VulnComponentInfo.builder()
                        .cveName(item.getCveName())
                        .cpe(allCpeUris)
                        .description(item.getDescription())
                        .problemType(item.getProblemType())
                        .referencesJson(item.getReferencesJson())
                        .impactJson(item.getImpactJson())
                        .referenceSite(item.getReferenceSite())
                        .componentName(extractedComponentName(bomRef))
                        .licenseInfo(licenseInfo)
                        .build();
                componentInfoList.add(vulnInfo);
            } else if ("AND".equalsIgnoreCase(operator)) {
                // TODO: AND 연산 처리 로직 추가
                SbomVulnDTO.VulnComponentInfo vulnInfo = SbomVulnDTO.VulnComponentInfo.builder()
                        .cveName(item.getCveName())
                        .cpe(allCpeUris)
                        .description(item.getDescription())
                        .problemType(item.getProblemType())
                        .referencesJson(item.getReferencesJson())
                        .impactJson(item.getImpactJson())
                        .referenceSite(item.getReferenceSite())
                        .componentName(extractedComponentName(bomRef))
                        .licenseInfo(licenseInfo)
                        .build();
                componentInfoList.add(vulnInfo);
            }
        }
    }

    public String[] normalizeCpeUri(String cpeUri) {
        String[] parts = cpeUri.split(":");

        String version = parts[5];

        parts[5] = "*";
        parts[6] = "*";
        parts[7] = "*";

        String normalizedCpeUri = String.join(":", parts);

        return new String[]{normalizedCpeUri, version};
    }

    public String extractedComponentName(String bomRef) {
        // 첫 번째 '/' 이후의 문자열만 추출
        int firstSlashIndex = bomRef.indexOf('/');
        if (firstSlashIndex != -1) {
            bomRef = bomRef.substring(firstSlashIndex + 1);
        }

        // "@"를 기준으로 패키지와 버전 분리
        String[] parts = bomRef.split("@");

        if (parts.length > 1) {
            // 패키지 부분을 ':'로 분리
            String[] packageParts = parts[0].split("/");

            // 버전 부분에 "?"가 있으면 그 이후의 문자열 제거
            int questionMarkIndex = parts[1].indexOf("?");
            if (questionMarkIndex != -1) {
                parts[1] = parts[1].substring(0, questionMarkIndex);
            }

            // 패키지와 버전을 ":"로 결합
            return packageParts.length > 1
                    ? packageParts[0] + ":" + packageParts[1] + ":" + parts[1]
                    : parts[0] + ":" + parts[1];
        }

        return bomRef;
    }
}
