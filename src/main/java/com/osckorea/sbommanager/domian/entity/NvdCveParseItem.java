package com.osckorea.sbommanager.domian.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "nvd_cve_parse_item", schema = "test_schema")
@Getter
//추후 유지보수 고려
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//추후 양방향 참조 고려할 것
@ToString
public class NvdCveParseItem {
    @Id
    private Long id;

    @Column("cve_name")
    private String cveName;

    private String description;

    @Column("problem_type")
    private String problemType;

    @Column("references_json")
    private String referencesJson;

    @Column("nvd_conf_json")
    private String nvdConfJson;

    @Column("impact_json")
    private String impactJson;

    @Column("reference_site")
    private String referenceSite;

}
