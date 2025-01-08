package com.osckorea.sbommanager.domian.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    public static class ComponentInfo {
        private String id;
        private String name;
        private String version;

        @Builder
        public ComponentInfo(String name, String version, String id) {
            this.id = id;
            this.name = name;
            this.version = version;
        }
    }
}

