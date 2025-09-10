package com.example.auth_module.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Table(name = "user_info")
@Entity
@Getter
@Setter
public class UserEntity {
    public enum Role {
        SUPER_ADMIN, ADMIN, USER, GUEST
    }


    @Id
    @SequenceGenerator(name = "user_infoSequence", sequenceName = "user_info_sequence", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_infoSequence")
    @Column(name = "id")
    private Long id;
    @Column(name = "username",length = 255, unique = true)
    private String username;
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Role> roles=new HashSet<>();
    @Column(name = "login",length = 255, unique = true)
    private String login;
    @Column(name = "date_registration")
    private LocalDateTime dateRegistration;
    @Column(name = "verification")
    private String verification;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private List<UserCommentEntity> comment = new ArrayList<>();
    @PrePersist
    protected void onCreate() {
        if (dateRegistration == null) {
            dateRegistration = LocalDateTime.now();
        }
    }

    public UserEntity(String username, String login) {
        this.username = username;
        this.login = login;
        this.dateRegistration = LocalDateTime.now();
    }
}
