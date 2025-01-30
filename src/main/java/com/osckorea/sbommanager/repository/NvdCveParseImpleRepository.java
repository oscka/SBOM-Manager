package com.osckorea.sbommanager.repository;


import com.osckorea.sbommanager.domian.entity.NvdCveParseItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NvdCveParseImpleRepository implements NvdCveParseRepository{

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<NvdCveParseItem> findMatchingConfigurations(
            @Param("allCpe") String allCpe,
            @Param("cpe") String cpe,
            @Param("version") String version
    ) {
        // JSONPath용 이스케이프 처리
        String escapedCpe = cpe.replace("\\", "\\\\").replace("\"", "\\\"");
        String escapedVersion = version.replace("\\", "\\\\").replace("\"", "\\\"");

        // SQL 쿼리
        String sql = """
        SELECT *
        FROM test_schema.nvd_cve_parse_item
        WHERE nvd_conf_json @? '$.**.cpe_match[*] ? (@.cpe23Uri == "%s")'
           OR nvd_conf_json @? '$.**.cpe_match[*] ? (
                @.cpe23Uri == "%s" &&
                (!exists(@.versionStartIncluding) || @.versionStartIncluding <= "%s") &&
                (!exists(@.versionStartExcluding) || @.versionStartExcluding < "%s") &&
                (!exists(@.versionEndIncluding) || @.versionEndIncluding >= "%s") &&
                (!exists(@.versionEndExcluding) || @.versionEndExcluding > "%s")
            )'
        """.formatted(allCpe, escapedCpe, escapedVersion, escapedVersion, escapedVersion, escapedVersion);

        // 쿼리 실행 및 결과 매핑
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            NvdCveParseItem item = new NvdCveParseItem();
            item.setCveName(rs.getString("cve_name"));
            item.setDescription(rs.getString("description"));
            item.setProblemType(rs.getString("problem_type"));
            item.setReferencesJson(rs.getString("references_json"));
            item.setNvdConfJson(rs.getString("nvd_conf_json"));
            item.setImpactJson(rs.getString("impact_json"));
            item.setReferenceSite(rs.getString("reference_site"));
            return item;
        });
    }


    @Override
    public List<String> findSimpleMatchingConfigurations(String cpe, String version) {
        return List.of();
    }
}
