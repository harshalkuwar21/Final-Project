package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Payment;
import com.Dk3.Cars.repository.PaymentRepository;
import com.Dk3.Cars.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/finance")
public class FinanceRestController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/payments")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/payments/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id).orElse(null);
    }

    @PostMapping("/payments")
    public Payment createPayment(@RequestBody Payment payment) {
        payment.setId(null);
        return paymentService.savePayment(payment);
    }

    @PutMapping("/payments/{id}")
    public Payment updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        return paymentService.getPaymentById(id).map(existing -> {
            existing.setAmount(payment.getAmount());
            existing.setPaymentMethod(payment.getPaymentMethod());
            existing.setStatus(payment.getStatus());
            existing.setTransactionId(payment.getTransactionId());
            existing.setPaymentDate(payment.getPaymentDate());
            return paymentService.savePayment(existing);
        }).orElse(null);
    }

    @DeleteMapping("/payments/{id}")
    public void deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }

    @GetMapping("/summary")
    public Map<String, Object> getFinanceSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        Double todayCollection = paymentService.getTodayCashCollection();
        Double totalPending = paymentRepository.findByStatus("Pending").stream()
            .mapToDouble(Payment::getAmount).sum();
        Double totalCompleted = paymentRepository.findByStatus("Completed").stream()
            .mapToDouble(Payment::getAmount).sum();
        
        summary.put("todayCashCollection", todayCollection != null ? todayCollection : 0);
        summary.put("totalPending", totalPending);
        summary.put("totalCompleted", totalCompleted != null ? totalCompleted : 0);
        summary.put("totalPayments", paymentRepository.count());
        summary.put("pendingCount", paymentRepository.findByStatus("Pending").size());
        summary.put("completedCount", paymentRepository.findByStatus("Completed").size());
        
        return summary;
    }

    @GetMapping("/pending")
    public List<Payment> getPendingPayments() {
        return paymentService.getPendingPayments();
    }

    @GetMapping("/by-status/{status}")
    public List<Payment> getPaymentsByStatus(@PathVariable String status) {
        return paymentService.getPaymentsByStatus(status);
    }

    @GetMapping("/by-date-range")
    public Map<String, Object> getPaymentsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        Double totalAmount = paymentService.getTotalPaymentsBetweenDates(start, end);
        List<Payment> payments = paymentRepository.findAll().stream()
            .filter(p -> !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end))
            .toList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", totalAmount != null ? totalAmount : 0);
        result.put("count", payments.size());
        result.put("payments", payments);
        
        return result;
    }

    @GetMapping("/method-breakdown")
    public Map<String, Object> getPaymentMethodBreakdown() {
        Map<String, Long> breakdown = new HashMap<>();
        
        paymentRepository.findAll().forEach(p -> {
            String method = p.getPaymentMethod() != null ? p.getPaymentMethod() : "Other";
            breakdown.put(method, breakdown.getOrDefault(method, 0L) + 1);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("breakdown", breakdown);
        
        return result;
    }
}
