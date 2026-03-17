package com.example.New_Project.Service;

import com.example.New_Project.DTO.TheatreDTO;
import com.example.New_Project.Entity.Screen;
import com.example.New_Project.Entity.Seat;
import com.example.New_Project.Entity.Theatre;
import com.example.New_Project.Repository.ScreenRepository;
import com.example.New_Project.Repository.SeatRepository;
import com.example.New_Project.Repository.TheatreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
@Transactional
public class TheatreService {

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ScreenRepository screenRepository;  // ← add this

    @Autowired
    private SeatRepository seatRepository;      // ← add this

    // ✅ Only this method changes — rest stays exactly the same!
    public Theatre saveTheatre(TheatreDTO dto) {

        // Your existing code — unchanged
        Theatre theatre = new Theatre();
        theatre.setName(dto.getName());
        theatre.setAddress(dto.getAddress());
        theatre.setCityId(dto.getCityId());
        theatre.setOwnerId(dto.getOwnerId());
        theatre.setStatus(com.example.New_Project.enums.TheatreStatus.ACTIVE);
        theatre.setTheatreId("THR-" + UUID.randomUUID()
                .toString().substring(0, 8));
        Theatre savedTheatre = theatreRepository.save(theatre);

        return savedTheatre;
    }


    // ✅ Everything below stays EXACTLY the same!
    public List<Theatre> getAllTheatres() {
        return theatreRepository.findAll();
    }

    public Optional<Theatre> getTheatreById(Long id) {
        return theatreRepository.findById(id);
    }

    public Theatre updateTheatre(Long id, TheatreDTO dto) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theatre not found"));
        theatre.setName(dto.getName());
        theatre.setAddress(dto.getAddress());
        theatre.setStatus(com.example.New_Project.enums.TheatreStatus.valueOf(dto.getStatus().toUpperCase()));
        return theatreRepository.save(theatre);
    }

    public void deleteTheatre(Long id) {
        theatreRepository.deleteById(id);
    }
}