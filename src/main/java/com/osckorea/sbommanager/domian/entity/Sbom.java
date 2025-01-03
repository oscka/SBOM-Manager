package com.osckorea.sbommanager.domian.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "sboms", schema = "test_schema")
@Getter
//추후 유지보수 고려
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//추후 양방향 참조 고려할 것
@ToString
public class Sbom {
    @Id
    private Long id;

    private String bomFormat;
    private String specVersion;

    private String componentType;
    private String name;

    private Integer componentCount;
    //syft..
    private String clientTool;
    private String clientToolVersion;
    @Column("data")
    private String sbomJson;
}
