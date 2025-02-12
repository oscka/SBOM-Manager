package com.osckorea.sbomgr.repository;

import com.osckorea.sbomgr.domian.entity.NvdCveParseItem;

import java.util.List;

//public interface NvdCveParseRepository extends CrudRepository<NvdCveParseItem, Long> {
    public interface NvdCveParseRepository{

    List<NvdCveParseItem> findMatchingConfigurations(String allCpe, String cpe, String version);

    List<String> findSimpleMatchingConfigurations(String cpe, String version);

    List<NvdCveParseItem> findByBaseCpe(String cpe);

    List<NvdCveParseItem> findByExactCpe(String baseCpe);

    List<NvdCveParseItem> findByCpe(String cpe);

}