package com.example.New_Project.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Data
public class ShowDTO {
    private Long id;
    private Long screenId;
    private Long movieId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String language;
    private Double price;

}