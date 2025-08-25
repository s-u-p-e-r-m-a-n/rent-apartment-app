package com.example.rent_module.mapper;

import com.example.rent_module.dto.FullInfoApartment;
import com.example.rent_module.entity.AddressInfoEntity;
import com.example.rent_module.entity.ApartmentInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface RentMapper {


    public AddressInfoEntity addressInfoEntityToAddressInfoEntity(FullInfoApartment fullInfoApartment);
    @Mapping(target = "availability",constant = "true")
    public ApartmentInfoEntity addressInfoEntityToApartmentInfoEntity(FullInfoApartment fullInfoApartment);

    public FullInfoApartment addressInfoEntityToFullInfoApartment(AddressInfoEntity addressInfoEntity, ApartmentInfoEntity apartmentInfoEntity);

}
