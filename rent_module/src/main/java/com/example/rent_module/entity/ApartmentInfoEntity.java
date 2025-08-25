package com.example.rent_module.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Setter
@Getter
@Table(name = "apartment_info")
public class ApartmentInfoEntity {

    @Id
    @SequenceGenerator(name = "apartment_infoSequence", sequenceName = "apartment_info_sequence", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "apartment_infoSequence")
    @Column(name = "id")
    private Long id;

    @Column(name = "room_count")
    private String roomCount;
    @Column(name = "price")
    private String price;
    @Column(name = "availability")
    private String availability;
    @OneToOne(cascade = CascadeType.ALL)
//  @JoinColumn(name = "address_id",referencedColumnName = "id")
    @JoinColumn(name = "address_id")
    private AddressInfoEntity address;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "apartment_id")
    private List<ApartmentImageEntity> image;
    @Column(name="average_rating")
    private double rating;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
//    @ToString.Exclude
//    @JsonIgnore
    private UserEntity user;
    @ManyToMany(cascade = CascadeType.ALL) //@ManyToMany(Cascade.ALL) в сущности позволяет сохранить без
                                          //предварителного сохранения коментария в своей сущности
    @JoinTable(name = "apartment_comment", joinColumns = @JoinColumn(name = "apartment_id"),
            inverseJoinColumns = @JoinColumn(name = "comment_id"))
    private List<UserCommentEntity> comment = new ArrayList<>();

    public void addComment(UserCommentEntity commentEntity) {

        comment.add(commentEntity);
    }


}
