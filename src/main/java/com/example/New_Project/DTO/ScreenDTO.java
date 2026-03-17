package com.example.New_Project.DTO;


import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ScreenDTO {

    private Integer theatreId; // Foreign Key to Theatre
    private String screenName;
    private Integer totalSeats;
    private String status;
}
