package com.example.New_Project.Service;

import com.example.New_Project.DTO.ShowDTO;
import com.example.New_Project.Entity.Show;
import com.example.New_Project.Repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowService {

    @Autowired
    private ShowRepository showRepository;


    public Show addShow(ShowDTO dto) {
        Show show = new Show();
        show.setScreenId(dto.getScreenId());
        show.setMovieId(dto.getMovieId());
        show.setStartTime(dto.getStartTime());
        show.setEndTime(dto.getEndTime());
        show.setLanguage(dto.getLanguage());
        show.setPrice(dto.getPrice());
        return showRepository.save(show);
    }


    public List<Show> getShowsForMovie(Integer movieId) {
        return showRepository.findByMovieId(movieId);
    }


    public void deleteShow(Long id) {
        showRepository.deleteById(id);
    }
}
