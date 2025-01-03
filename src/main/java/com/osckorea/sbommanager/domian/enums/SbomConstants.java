package com.osckorea.sbommanager.domian.enums;

import lombok.Getter;

@Getter
public class SbomConstants {
    @Getter
    public enum BomFormat {
        CYCLONEDX("cyclonedx"),
        SPDX("spdx");

        private final String value;

        BomFormat(String value) {
            this.value = value;
        }
    }

    @Getter
    public enum ComponentType {
        LIBRARY("library"),
        FRAMEWORK("framework"),
        APPLICATION("application");

        private final String value;

        ComponentType(String value) {
            this.value = value;
        }
    }

    @Getter
    public enum ClientTool {
        SYFT("syft"),
        CYCLONEDX_CLI("cyclonedx-cli"),
        TRIVY("trivy");

        private final String value;

        ClientTool(String value) {
            this.value = value;
        }
    }
}
