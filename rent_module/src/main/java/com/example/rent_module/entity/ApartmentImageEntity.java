package com.example.rent_module.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "apartment_image")
public class ApartmentImageEntity {

    @Id
    @SequenceGenerator(name = "apartment_imageSequence", sequenceName = "apartment_image_sequence", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "apartment_imageSequence")
    @Column(name = "id")
    private Long id;
    @Column(name = "original_name")
    private String originalName;
    @Column(name = "image")
    private String image;
    @Column(name = "size")
    private Long size;

}
