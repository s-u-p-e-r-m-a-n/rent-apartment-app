package com.example.rent_module.service;

import com.example.rent_module.dto.FullInfoApartment;
import com.example.rent_module.dto.LocationDto;
import com.example.rent_module.dto.UserRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RentService  {

    public String saveNewApartment(FullInfoApartment fullInfoApartment, MultipartFile file, UserRequestDto dto) throws IOException;

    public FullInfoApartment getApartmentInfo(Long id);

    public List<FullInfoApartment> showApartment(LocationDto locationDto,Long id);
    public String addCommentUser(Long id,String comment,Integer grade,UserRequestDto dto);
}
