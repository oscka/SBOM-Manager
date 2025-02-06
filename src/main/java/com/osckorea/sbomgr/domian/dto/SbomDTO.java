package com.osckorea.sbomgr.domian.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@NoArgsConstructor
public class SbomDTO{

    private String name;
    private String sbomFormat;
    private String type;
    private int componentCount;
    private List<ComponentInfo> componentInfoList;
    private String analyst;
    private LocalDateTime analysisDateTime;

    @Builder
    public SbomDTO(String name, String sbomFormat, String type, int componentCount,
                   List<ComponentInfo> componentInfoList, String analyst, LocalDateTime analysisDateTime) {
        this.name = name;
        this.sbomFormat = sbomFormat;
        this.type = type;
        this.componentCount = componentCount;
        this.componentInfoList = componentInfoList;
        this.analyst = analyst;
        this.analysisDateTime = analysisDateTime;
    }

    @Getter
    @NoArgsConstructor
    @SuperBuilder
    public static class ComponentInfo {
        private String bomRef;
        private String name;
        private String version;
    }

    @Getter
    @NoArgsConstructor
    @SuperBuilder
    public static class ComponentCpesLicenseInfo extends ComponentInfo {
        private List<String> cpes;
        private List<String> licenses;
    }
}

