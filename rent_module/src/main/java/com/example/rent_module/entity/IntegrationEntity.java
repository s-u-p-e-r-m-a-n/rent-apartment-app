package com.example.rent_module.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name ="integration_info")
public class IntegrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private String id;
    private  String path;
    private  String token;
    private  String description;
}
