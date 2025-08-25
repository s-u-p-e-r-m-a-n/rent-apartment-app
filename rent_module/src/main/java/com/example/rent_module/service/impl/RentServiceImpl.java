package com.example.rent_module.service.impl;

import com.example.rent_module.dto.FullInfoApartment;
import com.example.rent_module.dto.LocationDto;
import com.example.rent_module.dto.UserRequestDto;
import com.example.rent_module.dto.location_response.LocationResponseDto;
import com.example.rent_module.entity.AddressInfoEntity;
import com.example.rent_module.entity.ApartmentImageEntity;
import com.example.rent_module.entity.ApartmentInfoEntity;
import com.example.rent_module.entity.UserCommentEntity;
import com.example.rent_module.exception.ApartmentException;
import com.example.rent_module.mapper.RentMapper;
import com.example.rent_module.repository.AddressInfoRepository;
import com.example.rent_module.repository.ApartmentInfoRepository;
import com.example.rent_module.repository.UserRepository;
import com.example.rent_module.service.RentIntegrationService;
import com.example.rent_module.service.RentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.example.rent_module.exception.ApartmentException.APARTMENT_NOT_EXIST;
import static com.example.rent_module.exception.ApartmentException.APARTMENT_NOT_EXIST_CODE;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class RentServiceImpl implements RentService {

    private final RentIntegrationService rentIntegrationService;
    private final RentMapper rentMapper;
    private final AddressInfoRepository addressInfoRepository;
    private final ApartmentInfoRepository apartmentInfoRepository;
    private final UserRepository userRepository;

    @Override
    public String saveNewApartment(FullInfoApartment fullInfoApartment, MultipartFile file, UserRequestDto user) throws ApartmentException, IOException {
        ApartmentInfoEntity apartmentInfoEntity = rentMapper.addressInfoEntityToApartmentInfoEntity(fullInfoApartment);
        AddressInfoEntity addressInfoEntity = rentMapper.addressInfoEntityToAddressInfoEntity(fullInfoApartment);
        apartmentInfoEntity.setImage(prepareApartmentImageEntity(file));//сохранение изображения
        apartmentInfoEntity.setAddress(addressInfoEntity);
        apartmentInfoEntity.setUser(userRepository.findByEmailJpql(user.getLoginValue()));
        apartmentInfoRepository.save(apartmentInfoEntity);
        return "Апартаменты сохранены";


    }

    private List<ApartmentImageEntity> prepareApartmentImageEntity(MultipartFile file) throws IOException {
        List<ApartmentImageEntity> apartmentList = new ArrayList<>();
        ApartmentImageEntity apartmentImageEntity = new ApartmentImageEntity();
        apartmentImageEntity.setImage(Base64.getEncoder().encodeToString(file.getBytes()));
        apartmentImageEntity.setSize(file.getSize());
        apartmentImageEntity.setOriginalName(file.getOriginalFilename());
        apartmentList.add(apartmentImageEntity);

        return apartmentList;

    }

    public FullInfoApartment getApartmentInfo(Long id) {


        AddressInfoEntity addressInfoEntity = addressInfoRepository.findById(id).orElseThrow();
        ApartmentInfoEntity apartmentInfoEntity = apartmentInfoRepository.findById(id)
                .orElseThrow(() -> new ApartmentException(APARTMENT_NOT_EXIST, APARTMENT_NOT_EXIST_CODE));

        FullInfoApartment fullInfoApartment = rentMapper.addressInfoEntityToFullInfoApartment(
                addressInfoEntity, apartmentInfoEntity);
        return fullInfoApartment;

    }

    /* В методе showApartment реализовано получение аппартаментов либо по id или по локации*/
    @Override
    public List<FullInfoApartment> showApartment(LocationDto locationDto, Long id) {

        if (isNull(locationDto) && !isNull(id)) {
            ApartmentInfoEntity value = apartmentInfoRepository.findById(id).orElseThrow(() -> new ApartmentException(APARTMENT_NOT_EXIST, APARTMENT_NOT_EXIST_CODE));
            FullInfoApartment fullInfoApartment = rentMapper.addressInfoEntityToFullInfoApartment(value.getAddress(), value);
            List<FullInfoApartment> fullInfoApartments = new ArrayList<>();
            fullInfoApartments.add(fullInfoApartment);

            return fullInfoApartments;
        }
        if (!isNull(locationDto) && isNull(id)) {

            LocationResponseDto locationResponseDto = rentIntegrationService.geoIntegration(locationDto);

            String resultCity = null;

            resultCity = locationResponseDto.getResults().get(0).getComponents().getCity();

            if (isNull(resultCity)) {
                String resultCity2 = locationResponseDto.getResults().get(0).getComponents().getNormalizedCity();
                if (isNull(resultCity2)) {
                    throw new RuntimeException("отсутсвует информация о городе");
                }
                resultCity = resultCity2;
            }
            //String city = parseResponse(result);
            List<AddressInfoEntity> resultList = addressInfoRepository.findByCity(resultCity)
                    .orElseThrow(() -> new ApartmentException(APARTMENT_NOT_EXIST, APARTMENT_NOT_EXIST_CODE));
            List<FullInfoApartment> fullInfoApartments = new ArrayList<>();
            for (AddressInfoEntity addressInfoEntity : resultList) {
                FullInfoApartment fullInfoApartment = rentMapper.addressInfoEntityToFullInfoApartment(addressInfoEntity,
                        addressInfoEntity.getApartment());
                fullInfoApartments.add(fullInfoApartment);
            }
            return fullInfoApartments;
        }

        return null;
    }

    @Override
    public String addCommentUser(Long id, String comment,Integer grade,UserRequestDto user) {

        ApartmentInfoEntity resultApartment = apartmentInfoRepository.findById(id).orElseThrow(null);
        UserCommentEntity userCommentEntity = new UserCommentEntity(comment);
userCommentEntity.setUser(userRepository.findByEmailJpql(user.getLoginValue()));
userCommentEntity.setGrade(grade);
        resultApartment.addComment(userCommentEntity);
apartmentInfoRepository.save(resultApartment);//@ManyToMany(Cascade.ALL) в сущности коментарии позволяет сохранить без
                                              //предварителного сохранения коментария в своей сущности
return "комент сохранен";
    }
    @Transactional
public  void schedulerRatingApartment() {

        List<ApartmentInfoEntity> allApartment = apartmentInfoRepository.findByCommentNotEmptyJpql();
        for (ApartmentInfoEntity apartment : allApartment) {
            int rating = 0  ;
            List<UserCommentEntity> comment = apartment.getComment();
            for (UserCommentEntity userComment : comment) {
                List<Double> grade = new ArrayList<>();
                if (!isNull(userComment.getGrade())) {
                    grade.add((double) userComment.getGrade());
                }
                int sum = grade.stream().mapToInt(Double::intValue).sum();
                rating = sum / grade.size();
            }
            apartment.setRating(rating);
        }
}
    private String parseResponse(String result) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode jsonNodeCity = jsonNode.get("results").get(0).get("components").get("city");
        if (isNull(jsonNodeCity)) {
            JsonNode jsonNodeCity1 = jsonNode.get("results").get(0).get("components").get("_normalized_city");
            if (isNull(jsonNodeCity1)) {
                throw new RuntimeException("отсутсвует информация о городе");
            }
            return jsonNodeCity1.asText();
        }

        return jsonNodeCity.asText();
    }
}

