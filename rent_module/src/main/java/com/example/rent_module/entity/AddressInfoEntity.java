package com.example.rent_module.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "address_info")
public class AddressInfoEntity {

    @Id
    @SequenceGenerator(name="address_infoSequence",sequenceName="address_info_sequence", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_infoSequence")
    @Column(name = "id")
    private Long id;

    @Column( name = "city")
    private String city;
    @Column( name = "street")
    private String street;
    @Column( name = "house_number")
    private String houseNumber;
    @OneToOne(mappedBy = "address")
    private ApartmentInfoEntity apartment;

}
