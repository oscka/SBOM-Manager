package com.osckorea.sbomgr.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.osckorea.sbomgr.domian.dto.SbomDTO;
import com.osckorea.sbomgr.domian.dto.SbomVulnDTO;
import com.osckorea.sbomgr.domian.entity.Sbom;
import com.osckorea.sbomgr.domian.enums.SbomConstants;
import com.osckorea.sbomgr.service.SbomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SbomJsonParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final LoadingCache<String, CpeParts> cpeCache;


    public SbomConstants.BomFormat parseSbomFormat(String jsonString) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonString);

        //다른 Json Format 추가시 리팩토링 필요
        if (rootNode.has("$schema")) {
            return SbomConstants.BomFormat.CYCLONEDX;
        } else {
            return SbomConstants.BomFormat.SPDX;
        }
    }

    public Sbom parseCycloneDXSbom(String jsonString) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonString);

        Sbom sbom = new Sbom();
        sbom.setBomFormat(rootNode.path("bomFormat").asText());
        sbom.setSpecVersion(rootNode.path("specVersion").asText());
        sbom.setComponentType(rootNode.path("metadata").path("component").path("type").asText());

        String fullPath = rootNode.path("metadata").path("component").path("name").asText();
        String componentName = new File(fullPath).getName();
        sbom.setName(componentName);

        sbom.setComponentCount(rootNode.path("components").size());

        JsonNode toolsNode = rootNode.path("metadata").path("tools").path("components");
        if (toolsNode.isArray() && toolsNode.size() > 0) {
            JsonNode firstTool = toolsNode.get(0);
            sbom.setClientTool(firstTool.path("name").asText());
            sbom.setClientToolVersion(firstTool.path("version").asText());
        }

        return sbom;
    }

    public Sbom parseSpdxSbom(String jsonString) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonString);

        Sbom sbom = new Sbom();
        String spdxVersion = rootNode.path("spdxVersion").asText();
        sbom.setBomFormat("SPDX");
        sbom.setSpecVersion(spdxVersion.substring(spdxVersion.lastIndexOf("-") + 1));
        sbom.setComponentType(determineComponentType(rootNode, rootNode.path("name").asText()));
        sbom.setName(rootNode.path("name").asText());
        sbom.setComponentCount(rootNode.path("packages").size());

        JsonNode creators = rootNode.path("creationInfo").path("creators");
        for (JsonNode creator : creators) {
            String creatorText = creator.asText();
            if (creatorText.startsWith("Tool: ")) {
                String[] toolInfo = creatorText.substring(6).split("-");
                if (toolInfo.length == 2) {
                    sbom.setClientTool(toolInfo[0]);
                    sbom.setClientToolVersion(toolInfo[1]);
                }
            }
        }

        sbom.setSbomJson(jsonString);

        return sbom;
    }

    private String determineComponentType(JsonNode rootNode, String name) {
        JsonNode packages = rootNode.path("packages");
        if (packages.isArray()) {
            for (JsonNode packageNode : packages) {
                if (name.equals(packageNode.path("name").asText())) {
                    String primaryPackagePurpose = packageNode.path("primaryPackagePurpose").asText();
                    if ("CONTAINER".equals(primaryPackagePurpose)) {
                        return "container";
                    } else {
                        return "file";
                    }
                }
            }
        }
        return "file";
    }

    public List<SbomDTO.ComponentInfo> componentInfosParseCDX(String jsonData) throws IOException {
        JsonNode componentsNode = objectMapper.readTree(jsonData).get("components");

        List<SbomDTO.ComponentInfo> componentInfos = new ArrayList<>();

        if (componentsNode != null && componentsNode.isArray()) {
            for (JsonNode componentNode : componentsNode) {
                String name = componentNode.get("name").asText();
                String version = componentNode.get("version").asText();
                String bomRef = componentNode.get("bom-ref").asText();

                SbomDTO.ComponentInfo componentInfo = SbomDTO.ComponentInfo.builder()
                        .name(name)
                        .version(version)
                        .bomRef(bomRef)
                        .build();

                componentInfos.add(componentInfo);
            }
        }

        return componentInfos;
    }

    public List<SbomDTO.ComponentCpesLicenseInfo> componentInfosParseForVuln(String jsonData) throws IOException {
        JsonNode componentsNode = objectMapper.readTree(jsonData).get("components");

        List<SbomDTO.ComponentCpesLicenseInfo> componentInfos = new ArrayList<>();

        if (componentsNode != null && componentsNode.isArray()) {
            for (JsonNode componentNode : componentsNode) {
                String name = componentNode.get("name").asText();
                String version = componentNode.get("version").asText();
                String bomRef = componentNode.get("bom-ref").asText();
                String mainCpe = componentNode.path("cpe").isNull()
                        ? null
                        : componentNode.path("cpe").asText();

                List<String> cpes = new ArrayList<>();
                if (!mainCpe.isEmpty()){
                    cpes.add(mainCpe);

                    JsonNode propertiesNode = componentNode.get("properties");
                    if (propertiesNode != null && propertiesNode.isArray()) {
                        for (JsonNode propertyNode : propertiesNode) {
                            if ("syft:cpe23".equals(propertyNode.get("name").asText())) {
                                cpes.add(propertyNode.get("value").asText());
                            }
                        }
                    }
                }

                List<String> licenses = new ArrayList<>();
                JsonNode licensesNode = componentNode.path("licenses");
                if (licensesNode.isArray()) {
                    for (JsonNode licenseNode : licensesNode) {
                        JsonNode licenseInfo = licenseNode.path("license");
                        String licenseId = licenseInfo.path("id").asText();
                        String licenseName = licenseInfo.path("name").asText();
                        if (!licenseId.isEmpty()) {
                            licenses.add(licenseId);
                        } else if (!licenseName.isEmpty()) {
                            licenses.add(licenseName);
                        }
                    }
                }

                SbomDTO.ComponentCpesLicenseInfo componentInfo = SbomDTO.ComponentCpesLicenseInfo.builder()
                        .name(name)
                        .version(version)
                        .bomRef(bomRef)
                        .cpes(cpes)
                        .licenses(licenses)
                        .build();

                componentInfos.add(componentInfo);
            }
        }

        return componentInfos;
    }

    public List<SbomDTO.ComponentInfo> componentInfosParseSPDX(String jsonData) throws IOException {
        JsonNode componentsNode = objectMapper.readTree(jsonData).get("packages");

        List<SbomDTO.ComponentInfo> componentInfos = new ArrayList<>();

        if (componentsNode != null && componentsNode.isArray()) {
            for (JsonNode componentNode : componentsNode) {
                String name = componentNode.get("name").asText();
                String version = componentNode.get("versionInfo").asText();
                String bomRef = componentNode.get("SPDXID").asText();

                SbomDTO.ComponentInfo componentInfo = SbomDTO.ComponentInfo.builder()
                        .name(name)
                        .version(version)
                        .bomRef(bomRef)
                        .build();

                componentInfos.add(componentInfo);
            }
        }

        return componentInfos;
    }

    public List<List<String>> extractCpeList(String sbomJson) throws IOException {
        List<List<String>> allCpeList = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(sbomJson);
        JsonNode componentsNode = rootNode.path("components");

        for (JsonNode component : componentsNode) {
            List<String> componentCpes = new ArrayList<>();

            // 대표 CPE 추출
            String representativeCpe = component.path("cpe").asText();
            if (!representativeCpe.isEmpty()) {
                componentCpes.add(representativeCpe);
            }

            // 변형 CPE 추출
            JsonNode propertiesNode = component.path("properties");
            for (JsonNode property : propertiesNode) {
                if (property.path("name").asText().startsWith("syft:cpe23")) {
                    componentCpes.add(property.path("value").asText());
                }
            }

            if (!componentCpes.isEmpty()) {
                allCpeList.add(componentCpes);
            }
        }

        return allCpeList;
    }

    public static boolean isInRange(String version, String startIncl, String endExcl) {
        // 실제 버전 비교 로직 구현 예시
        if (startIncl != null && !startIncl.isEmpty()) {
            if (version.compareTo(startIncl) < 0) return false;
        }
        if (endExcl != null && !endExcl.isEmpty()) {
            if (version.compareTo(endExcl) >= 0) return false;
        }
        return true;
    }

    public List<SbomDTO.ComponentCpesLicenseInfoV2> componentInfosParseForVulnV2(String jsonData) throws IOException {
        JsonNode componentsNode = objectMapper.readTree(jsonData).get("components");

        List<SbomDTO.ComponentCpesLicenseInfoV2> componentInfos = new ArrayList<>();

        if (componentsNode != null && componentsNode.isArray()) {
            for (JsonNode componentNode : componentsNode) {
                String name = componentNode.get("name").asText();
                String version = componentNode.get("version").asText();
                String bomRef = componentNode.get("bom-ref").asText();
                String mainCpe = componentNode.path("cpe").isNull()
                        ? null
                        : componentNode.path("cpe").asText();

                List<SbomDTO.cpeForm> cpes = new ArrayList<>();
                if (!mainCpe.isEmpty()){

                    CpeParts mainCpeParts = cpeCache.get(mainCpe);
                    SbomDTO.cpeForm mainForm = new SbomDTO.cpeForm(mainCpeParts.full(), mainCpeParts.base(), mainCpeParts.version());
                    cpes.add(mainForm);

                    JsonNode propertiesNode = componentNode.get("properties");
                    if (propertiesNode != null && propertiesNode.isArray()) {
                        for (JsonNode propertyNode : propertiesNode) {
                            if ("syft:cpe23".equals(propertyNode.get("name").asText())) {
                                String propertyCpe = propertyNode.get("value").asText();
                                CpeParts propertyCpeParts = cpeCache.get(propertyCpe);
                                SbomDTO.cpeForm propertyForm = new SbomDTO.cpeForm(
                                        propertyCpeParts.full(),
                                        propertyCpeParts.base(),
                                        propertyCpeParts.version());
                                cpes.add(propertyForm);
                            }
                        }
                    }
                }

                List<String> licenses = new ArrayList<>();
                JsonNode licensesNode = componentNode.path("licenses");
                if (licensesNode.isArray()) {
                    for (JsonNode licenseNode : licensesNode) {
                        JsonNode licenseInfo = licenseNode.path("license");
                        String licenseId = licenseInfo.path("id").asText();
                        String licenseName = licenseInfo.path("name").asText();
                        if (!licenseId.isEmpty()) {
                            licenses.add(licenseId);
                        } else if (!licenseName.isEmpty()) {
                            licenses.add(licenseName);
                        }
                    }
                }

                SbomDTO.ComponentCpesLicenseInfoV2 componentInfo = SbomDTO.ComponentCpesLicenseInfoV2.builder()
                        .name(name)
                        .version(version)
                        .bomRef(bomRef)
                        .cpes(cpes)
                        .licenses(licenses)
                        .build();

                componentInfos.add(componentInfo);
            }
        }

        return componentInfos;
    }

    // CpeParts 레코드: 입력된 CPE 문자열을 파싱하여 full, base, version 정보를 보관
    public static record CpeParts(String full, String base, String version) {
        public static CpeParts parse(String cpe) {
            String[] parts = cpe.split(":");
            if (parts.length < 6) {
                throw new IllegalArgumentException("Invalid CPE format: " + cpe);
            }
            String allCpe = cpe;
            // base: 6번째 필드(버전)를 와일드카드(*)로 대체
            String base = String.join(":", Arrays.copyOf(parts, 5)) + ":*:" +
                    String.join(":", Arrays.copyOfRange(parts, 6, parts.length));
            String version = parts[5];
            return new CpeParts(allCpe, base, version);
        }
    }

    public SbomJsonParser.CpeParts parseCpe(String cpe) { // 반드시 public으로 선언
        return SbomJsonParser.CpeParts.parse(cpe);
    }

}
