package com.osckorea.sbommanager.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table(name = "users", schema = "test_schema")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    private String password;
    @Column("full_name")
    private String fullName;
    private String phone;
    @Column("is_active")
    private Boolean isActive;
    @Column("created_at")
    private LocalDateTime createdAt;

    // Getters and setters
}
