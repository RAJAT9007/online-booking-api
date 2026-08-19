package com.example.New_Project.DTO;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DashboardStatsDTO {

    private Double totalRevenue;
    private Long bookedSeatsCount;
    private Long availableSeatsCount;
    private Long totalMovies;
    private Long totalTheatres;
    private Double occupancyPercentage;
}
