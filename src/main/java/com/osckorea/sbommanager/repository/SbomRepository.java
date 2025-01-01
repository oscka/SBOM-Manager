package com.osckorea.sbommanager.repository;

import com.osckorea.sbommanager.model.Sbom;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SbomRepository extends CrudRepository<Sbom, Long> {
}
