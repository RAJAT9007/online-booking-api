package com.example.New_Project.Repository;

import com.example.New_Project.Entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<CityEntity, Long> {

    Optional<CityEntity> findBycityName(String cityName);

    boolean existsByCityName(String cityName);

    Optional<CityEntity> findById(Long id);

    Optional<CityEntity> findByPincode(Long pincode);

}
