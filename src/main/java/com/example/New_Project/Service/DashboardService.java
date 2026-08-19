package com.example.New_Project.Service;

import com.example.New_Project.DTO.DashboardStatsDTO;
import com.example.New_Project.Repository.BookingRepository;
import com.example.New_Project.Repository.MovieRepository;
import com.example.New_Project.Repository.ShowSeatRepository;
import com.example.New_Project.Repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private ShowSeatRepository showSeatRepo;
    @Autowired
    private MovieRepository movieRepo;
    @Autowired
    private TheatreRepository theatreRepo;

    public DashboardStatsDTO getStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();

        dto.setTotalRevenue(bookingRepo.getTotalRevenue());
        dto.setBookedSeatsCount(showSeatRepo.countByStatus("BOOKED"));
        dto.setAvailableSeatsCount(showSeatRepo.countByStatus("AVAILABLE"));
        dto.setTotalMovies(movieRepo.count());
        dto.setTotalTheatres(theatreRepo.count());

        // Calculate percentage logic (include all seats for total to be accurate, but
        // keeping current approach plus LOCKED would be better, using
        // showSeatRepo.count())
        long total = dto.getBookedSeatsCount() + dto.getAvailableSeatsCount() + showSeatRepo.countByStatus("LOCKED");
        dto.setOccupancyPercentage(total > 0 ? (dto.getBookedSeatsCount() * 100.0 / total) : 0.0);

        return dto;
    }
}