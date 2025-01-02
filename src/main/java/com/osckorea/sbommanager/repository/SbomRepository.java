package com.osckorea.sbommanager.repository;

import com.osckorea.sbommanager.model.Sbom;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SbomRepository extends CrudRepository<Sbom, Long> {

//    @Modifying
//    @Query(value = "INSERT INTO sboms (data) VALUES (:jsonData::jsonb)")
//    void createSbom(@Param("jsonData") String jsonData);

}
