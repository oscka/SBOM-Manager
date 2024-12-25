package com.osckorea.sbommanager.repository;


import com.osckorea.sbommanager.model.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends CrudRepository<User, Long> {
//    @Modifying
//    @Transactional
//    @Query("UPDATE Board b SET b.title = :title, b.contents = :contents, b.modifyId = :modifyId, b.modifyName = :modifyName, b.modifyDate = :modifyDate WHERE b.num = :num")
//    int updateBoard(String title, String contents, String modifyId, String modifyName, LocalDateTime modifyDate, int num);

    @Query("SELECT * FROM test_schema.users")
    List<User> getAllUsers();

}
