package com.example.New_Project.DTO;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class MovieDTO {

        private Long id;
        private String title;
        private String description;
        private Integer duration_minutes;
        private String language;
        private String genre;
        private String poster_Url;
        private String status;

}
