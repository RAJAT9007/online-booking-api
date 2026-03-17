package com.example.New_Project.Entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Integer duration_minutes;
    private String language;
    private String genre;

    @Column(name = "poster_url")
    private String poster_Url;
    private String status;

}
