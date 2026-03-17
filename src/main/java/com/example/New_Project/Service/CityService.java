package com.example.New_Project.Service;


import com.example.New_Project.DTO.CityDTO;
import com.example.New_Project.Entity.CityEntity;
import com.example.New_Project.Repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {


    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository){
        this.cityRepository = cityRepository;

    }

    public CityDTO addCity(CityDTO dto) {
        CityEntity cityEntity = new CityEntity();
        //        cityEntity.setId(dto.getId());
        cityEntity.setCityName(dto.getCityName());
        cityEntity.setPincode(dto.getPincode());

        CityEntity savedCity = cityRepository.save(cityEntity);

        CityDTO responceDTO = new CityDTO();
        responceDTO.setId(savedCity.getId());
        responceDTO.setCityName(savedCity.getCityName());
        responceDTO.setPincode(savedCity.getPincode());

        return responceDTO;

    }
    public List<CityEntity> getAllCities() {

        return cityRepository.findAll();
    }
}
