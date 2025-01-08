package com.osckorea.sbommanager.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osckorea.sbommanager.domian.dto.SbomDTO;
import com.osckorea.sbommanager.domian.entity.Sbom;
import com.osckorea.sbommanager.domian.enums.SbomConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SbomJsonParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SbomConstants.BomFormat parseSbomFormat(String jsonString) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonString);

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
        sbom.setComponentType(determineComponentType(rootNode));
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

    private String determineComponentType(JsonNode rootNode) {
        JsonNode packages = rootNode.path("packages");
        if (packages.isArray() && packages.size() > 0) {
            JsonNode firstPackage = packages.get(0);
            String primaryPackagePurpose = firstPackage.path("primaryPackagePurpose").asText();
            if ("CONTAINER".equals(primaryPackagePurpose)) {
                return "container";
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
                String id = componentNode.get("bom-ref").asText();

                SbomDTO.ComponentInfo componentInfo = SbomDTO.ComponentInfo.builder()
                        .name(name)
                        .version(version)
                        .id(id)
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
                String id = componentNode.get("SPDXID").asText();

                SbomDTO.ComponentInfo componentInfo = SbomDTO.ComponentInfo.builder()
                        .name(name)
                        .version(version)
                        .id(id)
                        .build();

                componentInfos.add(componentInfo);
            }
        }

        return componentInfos;
    }

}
