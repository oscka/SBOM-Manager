package com.osckorea.sbomgr.repository;



import com.osckorea.sbomgr.domian.entity.NvdCveParseItem;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "exactCpeCache", key = "#p0")
    public List<NvdCveParseItem> findByExactCpe(@Param("cpe") String cpe) {

        // SQL 쿼리
        String EXACT_QUERY = """
                SELECT *
                FROM test_schema.nvd_cve_parse_item
                WHERE nvd_conf_json @? '$.**.cpe_match[*] ? (@.cpe23Uri == "%s")'
                """.formatted(cpe);
        return jdbcTemplate.query(EXACT_QUERY, (rs, rowNum) ->
                new NvdCveParseItem(
                        rs.getString("cve_name"),
                        rs.getString("description"),
                        rs.getString("problem_type"),
                        rs.getString("references_json"),
                        rs.getString("nvd_conf_json"),
                        rs.getString("impact_json"),
                        rs.getString("reference_site")
                ));
    }

    @Cacheable(value = "cpeCache", key = "#p0")
    public List<NvdCveParseItem> findByCpe(@Param("cpe") String cpe) {
        // SQL 쿼리
        String QUERY = """
                SELECT *
                FROM test_schema.nvd_cve_parse_item
                WHERE nvd_conf_json @? '$.**.cpe_match[*] ? (@.cpe23Uri == "%s")'
                """.formatted(cpe);
        return jdbcTemplate.query(QUERY, (rs, rowNum) ->
                new NvdCveParseItem(
                        rs.getString("cve_name"),
                        rs.getString("description"),
                        rs.getString("problem_type"),
                        rs.getString("references_json"),
                        rs.getString("nvd_conf_json"),
                        rs.getString("impact_json"),
                        rs.getString("reference_site")
                ));
    }


    @Cacheable(value = "baseCpeCache", key = "#p0")
    public List<NvdCveParseItem> findByBaseCpe(@Param("baseCpe") String baseCpe) {

        String BASE_QUERY = """
        SELECT *
        FROM test_schema.nvd_cve_parse_item 
        WHERE nvd_conf_json @? '$.**.cpe_match[*] ? (@.cpe23Uri == "%s")'
        """.formatted(baseCpe);

        return jdbcTemplate.query(BASE_QUERY, (rs, rowNum) ->
                new NvdCveParseItem(
                        rs.getString("cve_name"),
                        rs.getString("description"),
                        rs.getString("problem_type"),
                        rs.getString("references_json"),
                        rs.getString("nvd_conf_json"),
                        rs.getString("impact_json"),
                        rs.getString("reference_site")
                ));
    }


    @Override
    public List<String> findSimpleMatchingConfigurations(String cpe, String version) {
        return List.of();
    }
}
