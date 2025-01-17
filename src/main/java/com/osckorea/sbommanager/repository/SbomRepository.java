package com.osckorea.sbommanager.repository;

import com.osckorea.sbommanager.domian.entity.Sbom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SbomRepository extends CrudRepository<Sbom, Long> {

//    @Modifying
//    @Query(value = "INSERT INTO sboms (data) VALUES (:jsonData::jsonb)")
//    void createSbom(@Param("jsonData") String jsonData);

    Optional<Sbom> findByUuid(UUID uuid);

}
