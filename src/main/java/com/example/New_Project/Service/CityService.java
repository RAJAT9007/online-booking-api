package com.example.New_Project.Service;

import com.example.New_Project.DTO.CityDTO;
import com.example.New_Project.Entity.CityEntity;
import com.example.New_Project.Repository.CityRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public CityDTO addCity(CityDTO dto) {
        CityEntity cityEntity = new CityEntity();
        // cityEntity.setId(dto.getId());
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

    public CityDTO deleteCity(Long id) {
        CityEntity cityEntity = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with pincode: " + id));

        cityRepository.delete(cityEntity);

        CityDTO cityDTO = new CityDTO();
        cityDTO.setCityName(cityEntity.getCityName());
        cityDTO.setPincode(cityEntity.getPincode());
        return cityDTO;
    }

    public CityDTO updateCity(Long id, String cityName) {
        CityEntity cityEntity = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));

        cityEntity.setCityName(cityName);
        // cityEntity.setPincode(pincode);

        CityEntity savedCity = cityRepository.save(cityEntity);

        CityDTO cityDTO = new CityDTO();
        cityDTO.setCityName(savedCity.getCityName());
        // cityDTO.setPincode(savedCity.getPincode());
        return cityDTO;
    }

    public CityEntity getCityByPinCode(Long pincode) {
        CityEntity cityEntity = cityRepository.findByPincode(pincode)
                .orElseThrow(() -> new RuntimeException("City not found with pincode: " + pincode));

        return cityEntity;
    }

}
