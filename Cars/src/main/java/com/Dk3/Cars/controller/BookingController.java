package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.service.BookingService;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

 

    @Autowired
    private CarService carService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public String listBookings(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "bookings";
    }

    @GetMapping("/add")
    public String showAddBookingForm(Model model) {
        model.addAttribute("booking", new Booking());

        model.addAttribute("cars", carService.getAvailableCars());
        model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        return "booking-add";
    }

    @PostMapping("/add")
    public String addBooking(@ModelAttribute Booking booking, RedirectAttributes redirectAttributes) {
        bookingService.saveBooking(booking);
        redirectAttributes.addFlashAttribute("success", "Booking created successfully!");
        return "redirect:/bookings";
    }

    @GetMapping("/edit/{id}")
    public String showEditBookingForm(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id).orElse(null);
        if (booking == null) {
            return "redirect:/bookings";
        }
        model.addAttribute("booking", booking);
    
        model.addAttribute("cars", carService.getAvailableCars());
        model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        return "booking-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateBooking(@PathVariable Long id, @ModelAttribute Booking booking,
                               RedirectAttributes redirectAttributes) {
        booking.setId(id);
        bookingService.saveBooking(booking);
        redirectAttributes.addFlashAttribute("success", "Booking updated successfully!");
        return "redirect:/bookings";
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookingService.deleteBooking(id);
        redirectAttributes.addFlashAttribute("success", "Booking deleted successfully!");
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/convert")
    public String convertBookingToSale(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookingService.convertBookingToSale(id);
        redirectAttributes.addFlashAttribute("success", "Booking converted to sale successfully!");
        return "redirect:/bookings";
    }

    @GetMapping("/pending")
    public String getPendingBookings(Model model) {
        List<Booking> bookings = bookingService.getBookingsByStatus("Pending");
        model.addAttribute("bookings", bookings);
        model.addAttribute("statusFilter", "Pending");
        return "bookings";
    }

    @GetMapping("/confirmed")
    public String getConfirmedBookings(Model model) {
        List<Booking> bookings = bookingService.getBookingsByStatus("Confirmed");
        model.addAttribute("bookings", bookings);
        model.addAttribute("statusFilter", "Confirmed");
        return "bookings";
    }
}