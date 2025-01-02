package com.osckorea.sbommanager.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.osckorea.sbommanager.converter.StringToJsonbConverter;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

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
    private String name;
    @Column("data")
    private String sbomJson;
}
