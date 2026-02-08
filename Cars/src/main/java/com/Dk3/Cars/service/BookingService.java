package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    public List<Booking> getBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getBookingsBySalesExecutive(Long salesExecutiveId) {
        return bookingRepository.findBySalesExecutiveUserid(salesExecutiveId);
    }

    public List<Booking> getPendingDeliveries(LocalDate date) {
        return bookingRepository.findPendingDeliveries(date);
    }

    public long countBookingsByStatus(String status) {
        return bookingRepository.countByStatus(status);
    }

    public List<Booking> getRecentBookings() {
        return bookingRepository.findRecentBookings();
    }

    public void convertBookingToSale(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("Converted");
            bookingRepository.save(booking);
        }
    }
}