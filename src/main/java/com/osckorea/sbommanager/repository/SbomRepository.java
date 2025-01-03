package com.osckorea.sbommanager.repository;

import com.osckorea.sbommanager.domian.entity.Sbom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SbomRepository extends CrudRepository<Sbom, Long> {

//    @Modifying
//    @Query(value = "INSERT INTO sboms (data) VALUES (:jsonData::jsonb)")
//    void createSbom(@Param("jsonData") String jsonData);

}
