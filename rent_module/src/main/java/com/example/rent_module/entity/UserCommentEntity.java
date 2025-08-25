package com.example.rent_module.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user_comment")
public class UserCommentEntity {

    @Id
    @SequenceGenerator(name = "user_commentSequence", sequenceName = "user_comment_sequence", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_commentSequence")
    @Column(name = "id")
    private Long id;
    @Column(name = "comment", length = 2000)
    private String comment;
    @Column(name = "grade")
    private Integer grade;
    @ManyToOne(fetch = FetchType.LAZY)
    UserEntity user;
    @ManyToMany(mappedBy = "comment")
    private List<ApartmentInfoEntity> apartment=new ArrayList<>();

    public UserCommentEntity(String comment) {
        this.comment = comment;
    }
}
