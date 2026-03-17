package com.example.New_Project.DTO;


import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TheatreDTO {
    private Long id;

    @Column(unique = true , updatable = false, nullable = false)
    private String registerId;
    private String name;
    private String address;
    private Long cityId;
    private Long ownerId;
    private String status;
}