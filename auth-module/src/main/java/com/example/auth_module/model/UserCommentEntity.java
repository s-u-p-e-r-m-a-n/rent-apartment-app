package com.example.auth_module.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user_comment")
public class UserCommentEntity {

        @Id
        @SequenceGenerator(name="user_commentSequence",sequenceName="user_comment_sequence", initialValue = 2, allocationSize = 1)
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_commentSequence")
        @Column(name = "id")
        private Long id;

    @Column( name = "comment",length = 2000)
    private String comment;
//    @ManyToOne()
//    @JoinColumn(name = "user_id")
//    private UserEntity user;
//
//    public UserCommentEntity(String comment, UserEntity user) {
//        this.comment = comment;
//        this.user = user;
//    }

}
