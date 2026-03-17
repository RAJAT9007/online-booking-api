package com.example.New_Project.Service;

import com.example.New_Project.DTO.ScreenDTO;
import com.example.New_Project.Entity.Screen;
import com.example.New_Project.Repository.ScreenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;

    // Used internally by TheatreService — not called from Postman
    public Screen addScreen(ScreenDTO dto) {
        Screen screen = new Screen();
        screen.setTheatreId((long)dto.getTheatreId());
        screen.setScreenName(dto.getScreenName());
        screen.setTotalSeats(dto.getTotalSeats());
        screen.setStatus("ACTIVE");
        return screenRepository.save(screen);
    }

    // ✅ Angular calls this to get screens of a theatre
    public List<Screen> getScreensByTheatre(Long theatreId) {  // ← Long not Integer
        return screenRepository.findByTheatreId(theatreId);
    }

    // Admin can update screen name or status
    public Screen updateScreen(Long id, ScreenDTO dto) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Screen not found"));
        screen.setScreenName(dto.getScreenName());
        screen.setTotalSeats(dto.getTotalSeats());
        screen.setStatus(dto.getStatus());
        return screenRepository.save(screen);
    }

    // Admin can delete screen
    public void deleteScreen(Long id) {
        screenRepository.deleteById(id);
    }
}
