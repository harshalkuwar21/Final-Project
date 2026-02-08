package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Payment;
import com.Dk3.Cars.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    public List<Payment> getPaymentsBySale(Long saleId) {
        return paymentRepository.findBySaleId(saleId);
    }

    public List<Payment> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    public Double getTotalPaymentsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.getTotalPaymentsBetweenDates(startDate, endDate);
    }

    public List<Payment> getPendingPayments() {
        return paymentRepository.findPendingPayments();
    }

    public Double getTodayCashCollection() {
        return paymentRepository.getTodayCashCollection();
    }
}