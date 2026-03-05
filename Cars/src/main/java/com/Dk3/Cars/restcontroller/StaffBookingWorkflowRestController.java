package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.LoanDetail;
import com.Dk3.Cars.entity.PaymentTransaction;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.BookingRepository;
import com.Dk3.Cars.repository.LoanDetailRepository;
import com.Dk3.Cars.repository.PaymentStageRepository;
import com.Dk3.Cars.repository.PaymentTransactionRepository;
import com.Dk3.Cars.repository.UserRepository;
import com.Dk3.Cars.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/staff/bookings")
public class StaffBookingWorkflowRestController {

    private static final Set<String> LOAN_BANKS = Set.of("State Bank of India", "HDFC Bank");
    private static final Set<String> INSURANCE_COMPANIES = Set.of("ICICI Lombard", "HDFC ERGO");

    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingService bookingService;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired private PaymentStageRepository paymentStageRepository;
    @Autowired private LoanDetailRepository loanDetailRepository;

    private boolean isStaffOrAdmin(HttpSession session) {
        String role = String.valueOf(session.getAttribute("USER_ROLE"));
        return "ROLE_ADMIN".equals(role) || (role != null && !"ROLE_USER".equals(role) && !"null".equals(role));
    }

    private Optional<User> getSessionUser(HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return Optional.empty();
        return userRepository.findById(Long.valueOf(String.valueOf(userIdObj)));
    }

    private boolean isAdmin(HttpSession session) {
        return "ROLE_ADMIN".equals(String.valueOf(session.getAttribute("USER_ROLE")));
    }

    private boolean canAccessBooking(HttpSession session, Booking booking) {
        if (isAdmin(session)) return true;
        Long staffShowroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
        Long bookingShowroomId = booking != null && booking.getCar() != null && booking.getCar().getShowroom() != null
                ? booking.getCar().getShowroom().getId() : null;
        return staffShowroomId != null && staffShowroomId.equals(bookingShowroomId);
    }

    private boolean isClosed(Booking booking) {
        String s = String.valueOf(booking.getWorkflowStatus() == null ? booking.getStatus() : booking.getWorkflowStatus()).toLowerCase();
        return s.contains("reject") || s.contains("cancel") || s.contains("deliver");
    }

    private boolean isLoanBooking(Booking booking) {
        return booking != null && booking.getPaymentOption() != null && "Loan Required".equalsIgnoreCase(booking.getPaymentOption());
    }

    private void markStage(Long bookingId, String stageName, String status, String remarks) {
        paymentStageRepository.findByBookingIdOrderByStageOrderNoAsc(bookingId).stream()
                .filter(s -> s.getStageName() != null && stageName.equalsIgnoreCase(s.getStageName()))
                .findFirst()
                .ifPresent(s -> {
                    s.setStageStatus(status);
                    s.setRemarks(remarks);
                    paymentStageRepository.save(s);
                });
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending(HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        if (isAdmin(session)) return ResponseEntity.ok(Map.of("ok", true, "bookings", bookingRepository.findByWorkflowStatus("Pending")));
        Long showroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
        if (showroomId == null) return ResponseEntity.ok(Map.of("ok", true, "bookings", java.util.Collections.emptyList()));
        return ResponseEntity.ok(Map.of("ok", true, "bookings", bookingRepository.findByWorkflowStatusAndCarShowroomId("Pending", showroomId)));
    }

    @PostMapping("/{id}/pre-verify")
    public ResponseEntity<?> preVerify(@PathVariable Long id,
                                       @RequestParam(defaultValue = "true") boolean nameMatched,
                                       @RequestParam(required = false) String remarks,
                                       HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        if (isClosed(booking)) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Booking is already closed"));
        if (booking.getAadhaarPhotoUrl() == null || booking.getPanPhotoUrl() == null || booking.getSignaturePhotoUrl() == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Aadhaar, PAN and signature are required"));
        }
        booking.setCustomerNameMatched(nameMatched);
        booking.setPreVerificationRemarks(remarks);
        booking.setPreVerifiedAt(LocalDateTime.now());
        booking.setPreVerifiedBy(getSessionUser(session).map(User::getEmail).orElse("staff"));
        booking.setPreVerificationStatus(nameMatched ? "Pre-Verified" : "Rejected");
        booking.setWorkflowStatus(nameMatched ? "Pre-Verified" : "Rejected");
        booking.setStatus(nameMatched ? "Pre-Verified" : "Rejected");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        if (!nameMatched) booking.setRejectionReason("Name mismatch in uploaded documents.");
        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking));
    }

    @PostMapping("/{id}/loan-approve")
    public ResponseEntity<?> loanApprove(@PathVariable Long id, @RequestParam String bankName, HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        if (!isLoanBooking(booking)) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "This booking is not loan based"));
        if (!LOAN_BANKS.contains(bankName)) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Allowed banks: State Bank of India, HDFC Bank"));
        if (!"Pre-Verified".equalsIgnoreCase(booking.getPreVerificationStatus())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Pre-verification is required before loan approval"));
        }

        LoanDetail loan = loanDetailRepository.findByBookingId(id).orElseGet(LoanDetail::new);
        loan.setBooking(booking);
        loan.setLoanRequired(true);
        loan.setBankName(bankName);
        loan.setStatus("Approved");
        loan.setApprovedAt(LocalDateTime.now());
        loanDetailRepository.save(loan);

        markStage(id, "Loan Approved", "Completed", "Loan approved by " + bankName + ".");
        booking.setWorkflowStatus("Loan Approved");
        booking.setStatus("Loan Approved");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking, "loan", loan));
    }

    @PostMapping("/{id}/complete-full-payment")
    public ResponseEntity<?> completeFullPayment(@PathVariable Long id,
                                                 @RequestParam(required = false) String reference,
                                                 @RequestParam(required = false) String paymentMethod,
                                                 HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        if (isClosed(booking)) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Booking is already closed"));
        if (!"Pre-Verified".equalsIgnoreCase(booking.getPreVerificationStatus())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Pre-verification is required before full payment"));
        }
        if (isLoanBooking(booking)) {
            LoanDetail loan = loanDetailRepository.findByBookingId(id).orElse(null);
            if (loan == null || !"Approved".equalsIgnoreCase(loan.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Loan is not approved"));
            }
        }

        double total = booking.getTotalAmount() == null ? 0D : booking.getTotalAmount();
        double paid = booking.getPaidAmount() == null ? 0D : booking.getPaidAmount();
        double remaining = booking.getRemainingAmount() == null ? Math.max(0D, total - paid) : booking.getRemainingAmount();
        if (remaining <= 0D) return ResponseEntity.ok(Map.of("ok", true, "booking", booking, "message", "Already fully paid"));

        String ref = (reference == null || reference.isBlank()) ? "SHOWROOM-" + id + "-" + System.currentTimeMillis() : reference.trim();
        String method = (paymentMethod == null || paymentMethod.isBlank()) ? (isLoanBooking(booking) ? "Loan Disbursal" : "Cash") : paymentMethod.trim();

        PaymentTransaction tx = new PaymentTransaction();
        tx.setBooking(booking);
        tx.setPaymentType("Final");
        tx.setAmount(remaining);
        tx.setPaymentMethod(method);
        tx.setPaymentGateway("Offline");
        tx.setTransactionId(ref);
        tx.setReferenceNumber(ref);
        tx.setStatus("Completed");
        tx.setVerifiedAt(LocalDateTime.now());
        tx.setNotes(isLoanBooking(booking) ? "Final settlement via loan disbursal." : "Final amount collected at showroom.");
        paymentTransactionRepository.save(tx);

        markStage(id, "Final Amount Received", "Completed", "Final payment completed.");
        booking.setPaidAmount(paid + remaining);
        booking.setRemainingAmount(0D);
        booking.setWorkflowStatus("Payment Verified");
        booking.setStatus("Payment Verified");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking, "transaction", tx));
    }

    @PostMapping("/{id}/verify-down-payment")
    public ResponseEntity<?> verifyDownPayment(@PathVariable Long id,
                                               @RequestParam(required = false) String reference,
                                               HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        if (booking.getDownPaymentAmount() == null || booking.getDownPaymentAmount() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "No down payment recorded"));
        }
        if (Boolean.TRUE.equals(booking.getDownPaymentVerified())) {
            return ResponseEntity.ok(Map.of("ok", true, "booking", booking, "message", "Down payment already verified"));
        }

        PaymentTransaction tx = paymentTransactionRepository.findByBookingIdOrderByCreatedAtDesc(id).stream()
                .filter(t -> t.getPaymentType() != null && "Down Payment".equalsIgnoreCase(t.getPaymentType()))
                .findFirst().orElse(null);
        if (tx == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Down payment transaction not found"));

        tx.setStatus("Completed");
        tx.setVerifiedAt(LocalDateTime.now());
        if (reference != null && !reference.isBlank()) {
            tx.setReferenceNumber(reference);
            tx.setTransactionId(reference);
        }
        tx.setNotes(((tx.getNotes() == null ? "" : tx.getNotes() + " ") + "Verified by staff.").trim());
        paymentTransactionRepository.save(tx);

        markStage(id, "Down Payment Paid", "Completed", "Down payment verified by staff.");
        booking.setDownPaymentVerified(true);
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking, "transaction", tx));
    }

    @PostMapping("/{id}/generate-insurance")
    public ResponseEntity<?> generateInsurance(@PathVariable Long id,
                                               @RequestParam String companyName,
                                               @RequestParam String policyNumber,
                                               @RequestParam(required = false) String insuranceDocumentUrl,
                                               HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        if (booking.getRemainingAmount() != null && booking.getRemainingAmount() > 0) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Complete payment before insurance generation"));
        }
        if (!INSURANCE_COMPANIES.contains(companyName)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Allowed insurance: ICICI Lombard, HDFC ERGO"));
        }

        booking.setInsuranceCompanyName(companyName);
        booking.setInsurancePolicyNumber(policyNumber.trim());
        booking.setInsuranceDocumentUrl(insuranceDocumentUrl);
        booking.setInsuranceGeneratedAt(LocalDateTime.now());
        booking.setWorkflowStatus("Insurance Generated");
        booking.setStatus("Insurance Generated");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        markStage(id, "Delivery Ready", "Pending", "Insurance generated. RTO application pending.");
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking));
    }

    @PostMapping("/{id}/apply-rto")
    public ResponseEntity<?> applyRto(@PathVariable Long id,
                                      @RequestParam String temporaryRegistrationNumber,
                                      @RequestParam(required = false) String temporaryRegistrationUrl,
                                      @RequestParam(defaultValue = "true") boolean form20Submitted,
                                      @RequestParam(defaultValue = "true") boolean form21Submitted,
                                      @RequestParam(defaultValue = "true") boolean form22Submitted,
                                      @RequestParam(defaultValue = "true") boolean invoiceSubmitted,
                                      @RequestParam(defaultValue = "true") boolean insuranceSubmitted,
                                      HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        if (booking.getInsuranceGeneratedAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Insurance required before RTO application"));
        }
        if (!form20Submitted || !form21Submitted || !form22Submitted || !invoiceSubmitted || !insuranceSubmitted) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Form 20/21/22, invoice and insurance are mandatory"));
        }

        booking.setForm20Submitted(true);
        booking.setForm21Submitted(true);
        booking.setForm22Submitted(true);
        booking.setInvoiceSubmittedToRto(true);
        booking.setInsuranceSubmittedToRto(true);
        booking.setRtoAuthority("Ministry of Road Transport and Highways");
        booking.setTemporaryRegistrationNumber(temporaryRegistrationNumber.trim());
        booking.setTemporaryRegistrationUrl(temporaryRegistrationUrl);
        booking.setRtoApplicationStatus("TR Issued");
        booking.setRtoAppliedAt(LocalDateTime.now());
        booking.setWorkflowStatus("RTO Applied");
        booking.setStatus("RTO Applied");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        markStage(id, "Delivery Ready", "Pending", "TR issued. Delivery-day verification pending.");
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking));
    }

    @PostMapping("/{id}/complete-delivery")
    public ResponseEntity<?> completeDelivery(@PathVariable Long id,
                                              @RequestParam(defaultValue = "true") boolean originalDocumentsVerified,
                                              @RequestParam(defaultValue = "true") boolean physicalVerificationDone,
                                              @RequestParam(defaultValue = "true") boolean deliveryNoteSigned,
                                              @RequestParam(required = false) String finalInvoiceUrl,
                                              @RequestParam(required = false) String warrantyDocumentUrl,
                                              @RequestParam(required = false) String loanDocumentUrl,
                                              HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        if (booking.getTemporaryRegistrationNumber() == null || booking.getTemporaryRegistrationNumber().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Temporary registration required before delivery"));
        }
        if ((booking.getRemainingAmount() != null && booking.getRemainingAmount() > 0) || !originalDocumentsVerified || !physicalVerificationDone || !deliveryNoteSigned) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Payment and all delivery-day verifications are mandatory"));
        }

        booking.setOriginalDocumentsVerified(true);
        booking.setPhysicalVerificationDone(true);
        booking.setDeliveryNoteSigned(true);
        booking.setFinalInvoiceUrl(finalInvoiceUrl);
        booking.setWarrantyDocumentUrl(warrantyDocumentUrl);
        booking.setLoanDocumentUrl(loanDocumentUrl);
        booking.setDeliveryCompletedAt(LocalDateTime.now());
        booking.setWorkflowStatus("Delivered");
        booking.setStatus("Delivered");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        markStage(id, "Delivery Ready", "Completed", "Vehicle handed over to customer.");

        Map<String, Object> docs = new LinkedHashMap<>();
        docs.put("invoice", booking.getFinalInvoiceUrl() != null ? booking.getFinalInvoiceUrl() : booking.getProformaInvoiceUrl());
        docs.put("insurance", booking.getInsuranceDocumentUrl());
        docs.put("temporaryRegistration", booking.getTemporaryRegistrationNumber());
        docs.put("temporaryRegistrationUrl", booking.getTemporaryRegistrationUrl());
        docs.put("warranty", booking.getWarrantyDocumentUrl());
        docs.put("loanDocuments", booking.getLoanDocumentUrl());
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking, "handoverDocuments", docs));
    }

    @GetMapping("/{id}/workflow-summary")
    public ResponseEntity<?> workflowSummary(@PathVariable Long id, HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, booking)) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("workflowStatus", booking.getWorkflowStatus());
        summary.put("preVerificationStatus", booking.getPreVerificationStatus());
        summary.put("nameMatched", booking.getCustomerNameMatched());
        summary.put("paymentCompleted", booking.getRemainingAmount() == null || booking.getRemainingAmount() <= 0);
        summary.put("insuranceCompany", booking.getInsuranceCompanyName());
        summary.put("insurancePolicyNumber", booking.getInsurancePolicyNumber());
        summary.put("rtoStatus", booking.getRtoApplicationStatus());
        summary.put("temporaryRegistrationNumber", booking.getTemporaryRegistrationNumber());
        summary.put("deliveryCompletedAt", booking.getDeliveryCompletedAt());
        return ResponseEntity.ok(Map.of("ok", true, "summary", summary));
    }

    @PostMapping("/{id}/verify-payment")
    public ResponseEntity<?> verifyPayment(@PathVariable Long id, HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, existing.get())) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, "Payment Verified", null, null);
        return updated.<ResponseEntity<?>>map(b -> ResponseEntity.ok(Map.of("ok", true, "booking", b)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found")));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate confirmedDeliveryDate,
                                     HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, existing.get())) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, "Approved", null, confirmedDeliveryDate);
        return updated.<ResponseEntity<?>>map(b -> ResponseEntity.ok(Map.of("ok", true, "booking", b)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found")));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestParam String reason, HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, existing.get())) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, "Rejected", reason, null);
        return updated.<ResponseEntity<?>>map(b -> ResponseEntity.ok(Map.of("ok", true, "booking", b)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found")));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        if (!isStaffOrAdmin(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        if (!canAccessBooking(session, existing.get())) return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, status, null, null);
        return updated.<ResponseEntity<?>>map(b -> ResponseEntity.ok(Map.of("ok", true, "booking", b)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found")));
    }
}
