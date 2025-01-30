package com.osckorea.sbommanager.repository;

import com.osckorea.sbommanager.domian.entity.NvdCveParseItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

//public interface NvdCveParseRepository extends CrudRepository<NvdCveParseItem, Long> {
    public interface NvdCveParseRepository{

    List<NvdCveParseItem> findMatchingConfigurations(String allCpe, String cpe, String version);

    List<String> findSimpleMatchingConfigurations(String cpe, String version);
}