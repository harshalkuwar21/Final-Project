package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PdfService pdfService;
    @Autowired
    private EmailService emailService;

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

    public List<Booking> getBookingsByCustomerEmail(String email) {
        return bookingRepository.findByCustomerEmailOrderByBookingDateDesc(email);
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
            booking.setWorkflowStatus("Converted");
            booking.setStatusUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }
    }

    public Optional<Booking> updateWorkflowStatus(Long id, String workflowStatus, String rejectionReason, LocalDate deliveryDate) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = bookingOpt.get();
        booking.setWorkflowStatus(workflowStatus);
        booking.setStatus(workflowStatus);
        booking.setStatusUpdatedAt(LocalDateTime.now());
        booking.setRejectionReason(rejectionReason);
        if (deliveryDate != null) {
            booking.setExpectedDeliveryDate(deliveryDate);
        }

        if ("Approved".equalsIgnoreCase(workflowStatus)) {
            booking.setStatus("Confirmed");
            booking.setWorkflowStatus("Approved");
            generateBookingDocsAndNotify(booking);
        }

        return Optional.of(bookingRepository.save(booking));
    }

    private void generateBookingDocsAndNotify(Booking booking) {
        try {
            Map<String, byte[]> docs = new LinkedHashMap<>();
            docs.put("Booking-Confirmation-Receipt-" + booking.getId() + ".pdf",
                    pdfService.generateBookingDocumentPdf(booking, "Booking Confirmation Receipt",
                            "Your booking has been approved by DK3 Cars."));
            docs.put("Proforma-Invoice-" + booking.getId() + ".pdf",
                    pdfService.generateBookingDocumentPdf(booking, "Proforma Invoice",
                            "This is your provisional invoice for the selected vehicle."));
            docs.put("Car-Allotment-Letter-" + booking.getId() + ".pdf",
                    pdfService.generateBookingDocumentPdf(booking, "Car Allotment Letter",
                            "Your selected vehicle has been allotted and is being prepared."));
            docs.put("Delivery-Confirmation-Letter-" + booking.getId() + ".pdf",
                    pdfService.generateBookingDocumentPdf(booking, "Delivery Confirmation Letter",
                            "Delivery details are confirmed as per your selected schedule."));

            Path generatedDir = Path.of("uploads", "documents", "generated");
            Files.createDirectories(generatedDir);
            int idx = 0;
            for (Map.Entry<String, byte[]> entry : docs.entrySet()) {
                String fileName = "booking-" + booking.getId() + "-" + idx + ".pdf";
                Path filePath = generatedDir.resolve(fileName);
                Files.write(filePath, entry.getValue(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                String url = "/uploads/documents/generated/" + fileName;
                if (idx == 0) booking.setBookingReceiptUrl(url);
                if (idx == 1) booking.setProformaInvoiceUrl(url);
                if (idx == 2) booking.setAllotmentLetterUrl(url);
                if (idx == 3) booking.setDeliveryConfirmationLetterUrl(url);
                idx++;
            }

            String to = booking.getCustomer() != null ? booking.getCustomer().getEmail() : null;
            if (to != null && !to.isBlank()) {
                String subject = "DK3 Cars Booking Confirmed - #" + booking.getId();
                String body = """
                        Your booking is confirmed.

                        Booking ID: #%s
                        Car: %s %s
                        Delivery Date: %s
                        """.formatted(
                        booking.getId(),
                        booking.getCar() != null ? booking.getCar().getBrand() : "N/A",
                        booking.getCar() != null ? booking.getCar().getModel() : "N/A",
                        booking.getExpectedDeliveryDate() != null ? booking.getExpectedDeliveryDate() : "To be assigned"
                );
                emailService.sendBookingConfirmationWithAttachments(to, subject, body, docs);
            }
        } catch (IOException ignored) {
            // Do not fail workflow status update if document generation fails.
        }
    }
}
