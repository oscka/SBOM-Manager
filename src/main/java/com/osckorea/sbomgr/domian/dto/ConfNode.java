package com.osckorea.sbomgr.domian.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfNode {
    private String operator;
    private List<ConfNode> children;

    @JsonProperty("cpe_match")
    private List<CpeMatch> cpeMatch;

    @Data
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
