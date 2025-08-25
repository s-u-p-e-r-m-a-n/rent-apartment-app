package com.example.rent_module.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name="statistic_info")
public class ApartmentStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column( name = "description")
   private String description;
    @Column( name = "registration_time")
    private String registration_time;

    public ApartmentStatEntity(String description) {
        this.description = description;
        this.registration_time = LocalDateTime.now().toString();
    }
}
