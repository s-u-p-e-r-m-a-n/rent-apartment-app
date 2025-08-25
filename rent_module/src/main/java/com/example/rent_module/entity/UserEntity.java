package com.example.rent_module.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Table(name = "user_info")
@Entity
@Getter
@Setter
public class UserEntity {
    public enum Role {
        SUPER_ADMIN, ADMIN, USER
    }


    @Id
    @SequenceGenerator(name = "user_infoSequence", sequenceName = "user_info_sequence", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_infoSequence")
    @Column(name = "id")
    private Long id;
    @Column(name = "password")
    private String password;
    @Column(name = "username")
    private String username;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Role> roles;
    @Column(name = "login")
    private String login;
    @Column(name = "date_registration")
    private LocalDateTime dateRegistration;
    @Column(name = "token")
    private String token;
    @Column(name = "verification")
    private String verification;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<ApartmentInfoEntity> apartment = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private List<UserCommentEntity> comment = new ArrayList<>();


    public UserEntity(String password, String username, String login) {
        this.password = password;
        this.username = username;
        this.login = login;
        this.dateRegistration = LocalDateTime.now();
    }
}
