package com.osckorea.sbomgr.domian.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class SbomVulnDTO {

    // 프로젝트 이름
    private String name;
    // 발견된 cve 갯수
    private int vulnComponentCount;

    private int allComponentCount;
    // cve가 발견된 컴포넌트 및 cve 정보
    private List<VulnComponentInfo> vulncomponentInfoList;

    @Builder
    public SbomVulnDTO(String name, int vulnComponentCount, int allComponentCount,
                   List<SbomVulnDTO.VulnComponentInfo> vulncomponentInfoList) {
        this.name = name;
        this.vulnComponentCount = vulnComponentCount;
        this.allComponentCount = allComponentCount;
        this.vulncomponentInfoList = vulncomponentInfoList;
    }

    @Getter
    @NoArgsConstructor
    public static class VulnComponentInfo {
        // 컴포넌트 이름 => cve에 해당된 cpe 이름을 파싱해 진행 (버전도 포함)

        private String componentName;

        private String cveName;

        private List<String> cpe;

        private String description;

        private String problemType;

        private String referencesJson;

        private String impactJson;

        private String referenceSite;

        private List<String> licenseInfo;

        @Builder
        public VulnComponentInfo(String cveName, String description, String problemType, String referencesJson, String impactJson, String referenceSite,
                                 List<String> cpe, String componentName, List<String> licenseInfo) {
            this.cveName = cveName;
            this.description = description;
            this.problemType = problemType;
            this.referencesJson = referencesJson;
            this.impactJson = impactJson;
            this.referenceSite = referenceSite;
            this.cpe = cpe;
            this.componentName = componentName;
            this.licenseInfo = licenseInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VulnComponentInfo that = (VulnComponentInfo) o;
            return Objects.equals(cveName, that.cveName) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(problemType, that.problemType) &&
                    Objects.equals(referencesJson, that.referencesJson) &&
                    Objects.equals(impactJson, that.impactJson) &&
                    Objects.equals(componentName, that.componentName) &&
                    Objects.equals(licenseInfo, that.licenseInfo) &&
                    Objects.equals(cpe, that.cpe) &&
                    Objects.equals(referenceSite, that.referenceSite);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cveName, description, problemType, referenceSite);
        }
    }
}
