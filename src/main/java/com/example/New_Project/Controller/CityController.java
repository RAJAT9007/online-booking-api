package com.example.New_Project.Controller;


import com.example.New_Project.DTO.CityDTO;
import com.example.New_Project.Entity.CityEntity;
import com.example.New_Project.Service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/city")
//@CrossOrigin(origins = "http://localhost:4200")
public class CityController {

    @Autowired
    private CityService cityService;

    @PostMapping("/add")
    public ResponseEntity<CityDTO> addCity(@RequestBody CityDTO dto) {
        CityDTO savedCity = cityService.addCity(dto);
        return new ResponseEntity<>(savedCity, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CityEntity>> getAllcities(){
        List<CityEntity> cities = cityService.getAllCities();
        return new ResponseEntity<>(cities, HttpStatus.OK);
    }
}
