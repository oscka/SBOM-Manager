package com.osckorea.sbomgr.service;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public SbomVulnDTO analyzeSbom(UUID sbomId) throws IOException {
        //###########################################//
        //############### 초기 데이터 세팅 ###############//
        //###########################################//

        Sbom sbom = sbomRepository.findByUuid(sbomId).orElseThrow();
        // ComponentCpesLicenseInfoV2에는 각 컴포넌트의 cpeForm 목록과 라이선스 정보가 들어있음
        List<SbomDTO.ComponentCpesLicenseInfoV2> components =
                sbomJsonParser.componentInfosParseForVulnV2(sbom.getSbomJson());
        List<String> allCpe = extractUniqueCpeValues(components);
        ConcurrentLinkedQueue<NvdCveParseItem> matchedCves = new ConcurrentLinkedQueue<>();

        // DB에서 모든 CPE에 대응하는 CVE들을 조회 (동시성 체크)
        allCpe.parallelStream().forEach(cpe -> {
            List<NvdCveParseItem> result = nvdCveParseRepository.findByCpe(cpe);
//            if (Objects.equals(cpe, "cpe:2.3:a:lettuce-core:lettuce_core:6.1.10.RELEASE:*:*:*:*:*:*:*")) {
//                System.out.println(result);
//            }
            matchedCves.addAll(result);
        });
        List<NvdCveParseItem> matchedCveList = new ArrayList<>(matchedCves);
        Map<String, SbomVulnDTO.VulnComponentInfo> vulnMap = new ConcurrentHashMap<>();

        //###########################################//
        //########## 병렬 conf_json 탐색 수행 ############//
        //###########################################//

        List<Candidate> allCandidates = new ArrayList<>();
        for (SbomDTO.ComponentCpesLicenseInfoV2 comp : components) {
            for (SbomDTO.cpeForm cf : comp.getCpes()) {
                allCandidates.add(new Candidate(comp, cf));
            }
        }

        // 각 CVE 항목에 대해 confJson을 파싱하고 재귀적 매핑을 수행
        matchedCveList.parallelStream().forEach(cve -> {
            try {
                ObjectMapper mapper = new ObjectMapper()
                        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                // CVE의 nvdConfJson은 JSON 배열 형태로 들어있음 이를 ConfNode 리스트로 매핑
                List<ConfNode> confNodes = mapper.readValue(
                        cve.getNvdConfJson(),
                        new TypeReference<List<ConfNode>>() {}
                );

                // 모든 루트 노드에 대해 후보군을 재귀 평가하여 매핑 정보를 생성
                Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> globalMapping = new HashMap<>();
                for (ConfNode root : confNodes) {
                    Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> map = evaluateNodeMappingCandidate(root, allCandidates);
                    globalMapping = unionMapping(globalMapping, map);
                }
                
                // 전체 매핑이 하나라도 있으면 SBOM에서 CVE조건이 만족됐음을 의미
                if (!globalMapping.isEmpty()) {
                    // 각 기여 컴포넌트마다 Vulnerability 정보를 생성
                    globalMapping.forEach((comp, cpeSet) -> {
                        String compositeKey = comp.getBomRef() + ":" + cve.getCveName();
                        SbomVulnDTO.VulnComponentInfo info = SbomVulnDTO.VulnComponentInfo.builder()
                                .cveName(cve.getCveName())
                                .description(cve.getDescription())
                                .problemType(cve.getProblemType())
                                .referencesJson(cve.getReferencesJson())
                                .impactJson(cve.getImpactJson())
                                .referenceSite(cve.getReferenceSite())
                                .componentName(comp.getName())
                                .cpe(new ArrayList<>(cpeSet)) // 해당 컴포넌트가 제공한 취약 CPE 목록
                                .licenseInfo(comp.getLicenses())
                                .build();
                        vulnMap.putIfAbsent(compositeKey, info);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return SbomVulnDTO.builder()
                .name(sbom.getName())
                .vulnComponentCount(vulnMap.size())
                .allComponentCount(components.size())
                .vulncomponentInfoList(new ArrayList<>(vulnMap.values()))
                .build();
    }


    // 후보군 객체
    private static class Candidate {
        private SbomDTO.ComponentCpesLicenseInfoV2 component;
        private SbomDTO.cpeForm cpe;
        public Candidate(SbomDTO.ComponentCpesLicenseInfoV2 component, SbomDTO.cpeForm cpe) {
            this.component = component;
            this.cpe = cpe;
        }
        public SbomDTO.ComponentCpesLicenseInfoV2 getComponent() { return component; }
        public SbomDTO.cpeForm getCpe() { return cpe; }
    }

    // 재귀 평가 로직: 전체 후보군 대상으로 매핑 정보를 생성
    private Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> evaluateNodeMappingCandidate(ConfNode node, List<Candidate> candidates) {
        Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> currentMapping = new HashMap<>();
        // 현재 노드(정확히 말하자면 최상위 노드)의 cpe_match 항목은 OR 관계로 처리: 하나라도 매칭되면 매핑에 추가
        if (node.getCpeMatch() != null) {
            for (CpeMatch cm : node.getCpeMatch()) {
                if (cm.isVulnerable()) {
                    for (Candidate cand : candidates) {
                        if (isCpeMatch(cm, cand.getCpe())) {
                            currentMapping.computeIfAbsent(cand.getComponent(), k -> new HashSet<>())
                                    .add(cand.getCpe().getExactCpe());
                        }
                    }
                }
            }
        }
        // 자식 노드 평가
        Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> childrenMapping = new HashMap<>();
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            if ("AND".equalsIgnoreCase(node.getOperator())) {
                boolean allChildrenSatisfied = true;
                for (ConfNode child : node.getChildren()) {
                    Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> childMap = evaluateNodeMappingCandidate(child, candidates);
                    if (childMap.isEmpty()) {
                        allChildrenSatisfied = false;
                        break;                    }
                    childrenMapping = unionMapping(childrenMapping, childMap);
                }
                if (!allChildrenSatisfied)
                    childrenMapping.clear();
            } else { // OR 연산자인 경우
                for (ConfNode child : node.getChildren()) {
                    Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> childMap = evaluateNodeMappingCandidate(child, candidates);
                    childrenMapping = unionMapping(childrenMapping, childMap);
                }
            }
        }
        Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> combinedMapping = new HashMap<>();
        if ("AND".equalsIgnoreCase(node.getOperator())) {
            if (!currentMapping.isEmpty() && !childrenMapping.isEmpty()) {
                combinedMapping = unionMapping(currentMapping, childrenMapping);
            } else {
                // AND 조건은 모두 적용되어야 하므로, 하나라도 비어있으면 전체 조건 미충족
                if (!currentMapping.isEmpty()) {
                    combinedMapping.putAll(currentMapping);
                }
                else {
                    combinedMapping.putAll(childrenMapping);
                }
            }
        } else {
            // OR 조건: 현재와 자식 결과의 합집합
            combinedMapping = unionMapping(currentMapping, childrenMapping);
        }
        return combinedMapping;
    }

    // 두 개의 매핑을 union하는 헬퍼: 같은 컴포넌트 키의 set들을 합친다.
    private Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> unionMapping(
            Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> map1,
            Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> map2) {
        Map<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> result = new HashMap<>(map1);
        for (Map.Entry<SbomDTO.ComponentCpesLicenseInfoV2, Set<String>> entry : map2.entrySet()) {
            result.merge(entry.getKey(), entry.getValue(), (set1, set2) -> { set1.addAll(set2); return set1; });
        }
        return result;
    }

    // CPE 매칭 로직: exactCpe의 경우 단순 문자열 비교, baseCpe의 경우 버전 비교 포함
    private boolean isCpeMatch(CpeMatch cveCpe, SbomDTO.cpeForm componentCpe) {
        // 정확하게 일치 확인
        if (componentCpe.getExactCpe().equals(cveCpe.getCpe23Uri()))
            return true;
        // 베이스 CPE + 버전 비교
        // 이렇게 2가지로 비교를 하는 이유는 과거 cve 데이터 cpe 기입 양식이 요즘 기입 양식과 다르기 때문..
        return compareBaseCpe(
                cveCpe.getCpe23Uri(),
                componentCpe.getBaseCpe(),
                componentCpe.getVersion(),
                cveCpe.getVersionStartIncluding(),
                cveCpe.getVersionEndIncluding(),
                cveCpe.getVersionStartExcluding(),
                cveCpe.getVersionEndExcluding()
        );
    }

    private boolean compareBaseCpe(String cveUri, String baseUri, String version,
                                   String startIncl, String endIncl,
                                   String startExcl, String endExcl) {
        if (!cveUri.startsWith(baseUri))
            return false;
        try {
            VersionComparator vc = new VersionComparator(version);
            return (startIncl == null || vc.compareTo(startIncl) >= 0)
                    && (endIncl == null || vc.compareTo(endIncl) <= 0)
                    && (startExcl == null || vc.compareTo(startExcl) > 0)
                    && (endExcl == null || vc.compareTo(endExcl) < 0);
        } catch (Exception e) {
            return false;
        }
    }

    // 버전 비교 내부 클래스
    class VersionComparator implements Comparable<String> {
        private final String[] parts;
        public VersionComparator(String version) {
            this.parts = version.split("[\\.\\-]");
        }
        @Override
        public int compareTo(String other) {
            String[] otherParts = other.split("[\\.\\-]");
            int maxLength = Math.max(parts.length, otherParts.length);
            for (int i = 0; i < maxLength; i++) {
                int thisVal = (i < parts.length) ? parse(parts[i]) : 0;
                int otherVal = (i < otherParts.length) ? parse(otherParts[i]) : 0;
                if (thisVal != otherVal)
                    return Integer.compare(thisVal, otherVal);
            }
            return 0;
        }
        private int parse(String part) {
            try {
                return Integer.parseInt(part);
            } catch (NumberFormatException e) {
                return part.equalsIgnoreCase("x") ? Integer.MAX_VALUE : part.hashCode();
            }
        }
    }

    public List<String> extractUniqueCpeValues(List<SbomDTO.ComponentCpesLicenseInfoV2> components) {
        return components.stream()
                // 각 컴포넌트의 cpeForm 리스트가 null이 아니면 처리
                .filter(component -> component.getCpes() != null)
                // 모든 컴포넌트의 cpeForm 리스트를 평면화(flatMap)
                .flatMap(component -> component.getCpes().stream())
                // 각 cpeForm에서 exactCpe와 baseCpe 두 값을 가져와서 스트림의 두 요소로 만듦
                .flatMap(cpeForm -> Stream.of(cpeForm.getExactCpe(), cpeForm.getBaseCpe()))
                // 중복 제거 후 List로 수집
                .distinct()
                .collect(Collectors.toList());
    }

    // JSON 매핑 클래스
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfNode {
        private String operator;
        private List<ConfNode> children;
        @JsonProperty("cpe_match")
        private List<CpeMatch> cpeMatch;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CpeMatch {
        private boolean vulnerable;
        private String cpe23Uri;
        private String versionStartIncluding;
        private String versionEndIncluding;
        private String versionStartExcluding;
        private String versionEndExcluding;
    }
}
