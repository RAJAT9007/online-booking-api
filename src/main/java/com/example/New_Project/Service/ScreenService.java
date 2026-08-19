package com.example.New_Project.Service;

import com.example.New_Project.DTO.ScreenDTO;
import com.example.New_Project.Entity.Screen;
import com.example.New_Project.Repository.ScreenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;

    // @Lazy avoids circular dependency: ScreenService ↔ SeatService
    @Autowired
    @Lazy
    private SeatService seatService;

    /**
     * Creates a screen and immediately auto-generates 200 seats for it.
     * This is the single entry point for screen creation.
     */
    @Transactional
    public Screen addScreen(ScreenDTO dto) {
        Screen screen = new Screen();
        screen.setTheatreId((long) dto.getTheatreId());
        screen.setScreenName(dto.getScreenName());
        screen.setTotalSeats(dto.getTotalSeats() != null ? dto.getTotalSeats() : 200);
        screen.setStatus("ACTIVE");

        Screen saved = screenRepository.save(screen);

        //  Auto-generate 200 seats immediately after screen is created
        try {
            seatService.createSeatsForScreen(saved.getId());
        } catch (Exception e) {
            // Log but don't fail screen creation if seat gen fails
            System.err.println(" Seat generation failed for screen " + saved.getId() + ": " + e.getMessage());
        }

        return saved;
    }

    public List<Screen> getScreensByTheatre(Long theatreId) {
        return screenRepository.findByTheatreId(theatreId);
    }

    public Screen updateScreen(Long id, ScreenDTO dto) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Screen not found: " + id));
        screen.setScreenName(dto.getScreenName());
        screen.setTotalSeats(dto.getTotalSeats());
        if (dto.getStatus() != null) screen.setStatus(dto.getStatus());
        return screenRepository.save(screen);
    }

    public void deleteScreen(Long id) {
        screenRepository.deleteById(id);
    }
}
