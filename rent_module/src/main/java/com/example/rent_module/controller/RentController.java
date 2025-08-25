package com.example.rent_module.controller;

import com.example.rent_module.dto.FullInfoApartment;
import com.example.rent_module.dto.LocationDto;
import com.example.rent_module.dto.UserRequestDto;
import com.example.rent_module.dto.СommentRequestDto;
import com.example.rent_module.entity.ApartmentInfoEntity;
import com.example.rent_module.service.RentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.example.rent_module.controller.RentControllerPath.*;

@RestController
@RequiredArgsConstructor
public class RentController {

    private final RentService rentService;
    //@PostMapping()

    @PostMapping(CREATE_APART)
    public String addNewApartment(@RequestPart("data") FullInfoApartment fullInfoApartment,
                                  @RequestPart("file") MultipartFile file,
                                  @RequestPart("user") UserRequestDto user) throws IOException {
        return rentService.saveNewApartment(fullInfoApartment,file,user);

    }

    @GetMapping(GET_APARTMENT_ID)
    public FullInfoApartment getApartmentInfo(@PathVariable Long id) {

        return rentService.getApartmentInfo(id);
    }
    /* в этом эндпоинте данные о местоположении клиента и id аппартаментов*/
    @PostMapping("/show_apartment")
    public List<FullInfoApartment> showApartment(@RequestBody(required = false) LocationDto locationDto,
                                                 @RequestParam(required = false) Long id) {

        return rentService.showApartment(locationDto, id);

    }
    @PostMapping(ADD_COMMENT)
    public String addComment(@RequestBody СommentRequestDto commentDto) {
       return rentService.addCommentUser(commentDto.getApartmentId(),commentDto.getComment(), commentDto.getGrade(),commentDto.getUser());
    }

}
