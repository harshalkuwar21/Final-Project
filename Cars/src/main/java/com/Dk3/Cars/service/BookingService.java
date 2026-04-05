package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.LoanDetail;
import com.Dk3.Cars.entity.PaymentStage;
import com.Dk3.Cars.repository.BookingRepository;
import com.Dk3.Cars.repository.LoanDetailRepository;
import com.Dk3.Cars.repository.PaymentStageRepository;
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
import java.util.Set;

@Service
public class BookingService {

    private static final Set<String> CUSTOMER_EDITABLE_DOCUMENT_CATEGORIES = Set.of(
            "AadhaarPhoto",
            "PanPhoto",
            "SignaturePhoto",
            "PassportPhoto",
            "PaymentScreenshot",
            "DownPaymentReceipt"
    );

    @Autowired private BookingRepository bookingRepository;
    @Autowired private PdfService pdfService;
    @Autowired private EmailService emailService;
    @Autowired private PaymentStageRepository paymentStageRepository;
    @Autowired private LoanDetailRepository loanDetailRepository;

    public List<Booking> getAllBookings() { return bookingRepository.findAll(); }
    public Optional<Booking> getBookingById(Long id) { return bookingRepository.findById(id); }
    public Booking saveBooking(Booking booking) { return bookingRepository.save(booking); }
    public void deleteBooking(Long id) { bookingRepository.deleteById(id); }
    public List<Booking> getBookingsByStatus(String status) { return bookingRepository.findByStatus(status); }
    public List<Booking> getBookingsByCustomer(Long customerId) { return bookingRepository.findByCustomerId(customerId); }
    public List<Booking> getBookingsByCustomerEmail(String email) { return bookingRepository.findByCustomerEmailOrderByBookingDateDesc(email); }
    public List<Booking> getBookingsBySalesExecutive(Long salesExecutiveId) { return bookingRepository.findBySalesExecutiveUserid(salesExecutiveId); }
    public List<Booking> getPendingDeliveries(LocalDate date) { return bookingRepository.findPendingDeliveries(date); }
    public long countBookingsByStatus(String status) { return bookingRepository.countByStatus(status); }
    public List<Booking> getRecentBookings() { return bookingRepository.findRecentBookings(); }

    public void sendBookingDocumentsAfterPayment(Long bookingId) {
        if (bookingId == null) return;
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            generateBookingDocsAndNotify(booking);
            bookingRepository.save(booking);
        });
    }

    public void sendInitialBookingEmailPack(Long bookingId) {
        if (bookingId == null) return;
        bookingRepository.findById(bookingId).ifPresent(this::sendInitialBookingEmailPack);
    }

    public boolean isCustomerEditableDocument(String category) {
        return category != null && CUSTOMER_EDITABLE_DOCUMENT_CATEGORIES.contains(category);
    }

    public Optional<String> getBookingDocumentUrl(Booking booking, String category) {
        if (booking == null || category == null || category.isBlank()) return Optional.empty();
        return Optional.ofNullable(switch (category) {
            case "AadhaarPhoto" -> booking.getAadhaarPhotoUrl();
            case "PanPhoto" -> booking.getPanPhotoUrl();
            case "SignaturePhoto" -> booking.getSignaturePhotoUrl();
            case "PassportPhoto" -> booking.getPassportPhotoUrl();
            case "PaymentScreenshot" -> booking.getPaymentScreenshotUrl();
            case "DownPaymentReceipt" -> booking.getDownPaymentReceiptUrl();
            case "BookingReceipt" -> booking.getBookingReceiptUrl();
            case "ProformaInvoice" -> booking.getProformaInvoiceUrl();
            case "AllotmentLetter" -> booking.getAllotmentLetterUrl();
            case "DeliveryConfirmation" -> booking.getDeliveryConfirmationLetterUrl();
            case "InsurancePolicy" -> booking.getInsuranceDocumentUrl();
            case "TemporaryRegistration" -> booking.getTemporaryRegistrationUrl();
            case "FinalInvoice" -> booking.getFinalInvoiceUrl();
            case "RegistrationCertificate" -> booking.getRegistrationCertificateUrl();
            case "PucCertificate" -> booking.getPucCertificateUrl();
            case "WarrantyDocument" -> booking.getWarrantyDocumentUrl();
            case "ServiceBook" -> booking.getServiceBookUrl();
            case "DeliveryNote" -> booking.getDeliveryNoteUrl();
            case "RoadTaxReceipt" -> booking.getRoadTaxReceiptUrl();
            case "FinanceSanctionLetter" -> booking.getFinanceSanctionLetterUrl();
            case "FinanceAgreement" -> booking.getFinanceAgreementUrl();
            case "LoanDocument" -> booking.getLoanDocumentUrl();
            default -> null;
        });
    }

    public boolean deleteBookingDocument(Booking booking, String category) {
        String currentUrl = getBookingDocumentUrl(booking, category).orElse(null);
        if (booking == null || blank(currentUrl)) return false;

        clearBookingDocumentField(booking, category);
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        deleteStoredFile(currentUrl);
        return true;
    }

    public Booking generateInsuranceDocuments(Booking booking) {
        if (booking == null || blank(booking.getInsurancePolicyNumber())) return booking;
        try {
            byte[] insurance = pdfService.generateInsurancePolicyPdf(booking);
            booking.setInsuranceDocumentUrl(storeGeneratedDocument(booking.getInsuranceDocumentUrl(), booking.getId(), "insurance-policy", insurance));
            return bookingRepository.save(booking);
        } catch (IOException ignored) {
            return booking;
        }
    }

    public Booking generateRtoDocuments(Booking booking) {
        if (booking == null) return null;
        try {
            byte[] tempReg = pdfService.generateTemporaryRegistrationPdf(booking);
            byte[] rc = pdfService.generateRegistrationCertificatePdf(booking);
            byte[] tax = pdfService.generateRoadTaxReceiptPdf(booking);
            booking.setTemporaryRegistrationUrl(storeGeneratedDocument(booking.getTemporaryRegistrationUrl(), booking.getId(), "temporary-registration", tempReg));
            booking.setRegistrationCertificateUrl(storeGeneratedDocument(booking.getRegistrationCertificateUrl(), booking.getId(), "registration-certificate", rc));
            booking.setRoadTaxReceiptUrl(storeGeneratedDocument(booking.getRoadTaxReceiptUrl(), booking.getId(), "road-tax-receipt", tax));
            return bookingRepository.save(booking);
        } catch (IOException ignored) {
            return booking;
        }
    }

    public Booking generateDeliveryDocumentsAndNotify(Booking booking) {
        if (booking == null) return null;
        try {
            Map<String, byte[]> docs = new LinkedHashMap<>();
            LoanDetail loan = loanDetailRepository.findByBookingId(booking.getId()).orElse(null);

            byte[] finalInvoice = pdfService.generateFinalInvoicePdf(booking);
            byte[] warranty = pdfService.generateWarrantyBookletPdf(booking);
            byte[] serviceBook = pdfService.generateServiceBookPdf(booking);
            byte[] deliveryNote = pdfService.generateDeliveryNotePdf(booking);
            byte[] puc = pdfService.generatePucCertificatePdf(booking);
            byte[] rc = pdfService.generateRegistrationCertificatePdf(booking);
            byte[] tax = pdfService.generateRoadTaxReceiptPdf(booking);

            booking.setFinalInvoiceUrl(storeGeneratedDocument(booking.getFinalInvoiceUrl(), booking.getId(), "final-invoice", finalInvoice));
            booking.setWarrantyDocumentUrl(storeGeneratedDocument(booking.getWarrantyDocumentUrl(), booking.getId(), "warranty-booklet", warranty));
            booking.setServiceBookUrl(storeGeneratedDocument(booking.getServiceBookUrl(), booking.getId(), "service-book", serviceBook));
            booking.setDeliveryNoteUrl(storeGeneratedDocument(booking.getDeliveryNoteUrl(), booking.getId(), "delivery-note", deliveryNote));
            booking.setPucCertificateUrl(storeGeneratedDocument(booking.getPucCertificateUrl(), booking.getId(), "puc-certificate", puc));
            booking.setRegistrationCertificateUrl(storeGeneratedDocument(booking.getRegistrationCertificateUrl(), booking.getId(), "registration-certificate", rc));
            booking.setRoadTaxReceiptUrl(storeGeneratedDocument(booking.getRoadTaxReceiptUrl(), booking.getId(), "road-tax-receipt", tax));

            docs.put("Final-Invoice-" + booking.getId() + ".pdf", finalInvoice);
            docs.put("Registration-Certificate-" + booking.getId() + ".pdf", rc);
            docs.put("PUC-Certificate-" + booking.getId() + ".pdf", puc);
            docs.put("Warranty-Booklet-" + booking.getId() + ".pdf", warranty);
            docs.put("Service-Book-" + booking.getId() + ".pdf", serviceBook);
            docs.put("Delivery-Note-" + booking.getId() + ".pdf", deliveryNote);
            docs.put("Road-Tax-Receipt-" + booking.getId() + ".pdf", tax);

            if (!blank(booking.getInsurancePolicyNumber())) {
                byte[] insurance = pdfService.generateInsurancePolicyPdf(booking);
                booking.setInsuranceDocumentUrl(storeGeneratedDocument(booking.getInsuranceDocumentUrl(), booking.getId(), "insurance-policy", insurance));
                docs.put("Insurance-Policy-" + booking.getId() + ".pdf", insurance);
            }
            if (!blank(booking.getTemporaryRegistrationNumber())) {
                byte[] tempReg = pdfService.generateTemporaryRegistrationPdf(booking);
                booking.setTemporaryRegistrationUrl(storeGeneratedDocument(booking.getTemporaryRegistrationUrl(), booking.getId(), "temporary-registration", tempReg));
                docs.put("Temporary-Registration-" + booking.getId() + ".pdf", tempReg);
            }
            if (loan != null && "Approved".equalsIgnoreCase(loan.getStatus())) {
                byte[] sanction = pdfService.generateFinanceSanctionLetterPdf(booking, loan);
                byte[] agreement = pdfService.generateFinanceAgreementPdf(booking, loan);
                booking.setFinanceSanctionLetterUrl(storeGeneratedDocument(booking.getFinanceSanctionLetterUrl(), booking.getId(), "finance-sanction-letter", sanction));
                booking.setFinanceAgreementUrl(storeGeneratedDocument(booking.getFinanceAgreementUrl(), booking.getId(), "finance-agreement", agreement));
                if (blank(booking.getLoanDocumentUrl())) booking.setLoanDocumentUrl(booking.getFinanceAgreementUrl());
                docs.put("Finance-Sanction-Letter-" + booking.getId() + ".pdf", sanction);
                docs.put("Finance-Agreement-" + booking.getId() + ".pdf", agreement);
            }

            bookingRepository.save(booking);
            sendDocsEmail(booking, "DK3 Cars Delivery Document Pack - #" + booking.getId(),
                    """
                    Your vehicle delivery document pack is attached.

                    Included documents:
                    - Final invoice / sale bill
                    - RC copy
                    - PUC certificate
                    - Warranty booklet
                    - Service book
                    - Delivery note
                    - Road tax receipt
                    - Insurance / finance papers where applicable
                    """, docs);
            return booking;
        } catch (IOException ignored) {
            return booking;
        }
    }

    public void convertBookingToSale(Long bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            booking.setStatus("Converted");
            booking.setWorkflowStatus("Converted");
            booking.setStatusUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        });
    }

    public Optional<Booking> updateWorkflowStatus(Long id, String workflowStatus, String rejectionReason, LocalDate deliveryDate) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) return Optional.empty();

        Booking booking = bookingOpt.get();
        booking.setWorkflowStatus(workflowStatus);
        booking.setStatus(workflowStatus);
        booking.setStatusUpdatedAt(LocalDateTime.now());
        booking.setRejectionReason(rejectionReason);
        if (deliveryDate != null) booking.setExpectedDeliveryDate(deliveryDate);

        if ("Approved".equalsIgnoreCase(workflowStatus)) {
            booking.setStatus("Confirmed");
            booking.setWorkflowStatus("Approved");
            markStageStatus(booking.getId(), "Final Amount Received", "Pending", "Booking confirmed. Awaiting final payment on delivery day.");
            markStageStatus(booking.getId(), "Delivery Ready", "Pending", "Delivery will be prepared after full payment completion.");
        }
        if ("Payment Verified".equalsIgnoreCase(workflowStatus)) {
            booking.setStatus("Confirmed");
            booking.setWorkflowStatus("Approved");
            markStageStatus(booking.getId(), "Final Amount Received", "Completed", "Full payment approved by staff.");
            markStageStatus(booking.getId(), "Delivery Ready", "Pending", "Documents sent. Delivery preparation in progress.");
            generateBookingDocsAndNotify(booking);
        }

        return Optional.of(bookingRepository.save(booking));
    }

    private void markStageStatus(Long bookingId, String stageName, String stageStatus, String remarks) {
        if (bookingId == null || stageName == null) return;
        List<PaymentStage> stages = paymentStageRepository.findByBookingIdOrderByStageOrderNoAsc(bookingId);
        for (PaymentStage stage : stages) {
            if (stage.getStageName() != null && stageName.equalsIgnoreCase(stage.getStageName())) {
                stage.setStageStatus(stageStatus);
                stage.setRemarks(remarks);
                paymentStageRepository.save(stage);
                return;
            }
        }
    }

    private void generateBookingDocsAndNotify(Booking booking) {
        try {
            Map<String, byte[]> docs = new LinkedHashMap<>();
            byte[] receipt = pdfService.generateBookingDocumentPdf(booking, "Booking Confirmation Receipt", "Your booking has been approved by DK3 Cars.");
            byte[] proforma = pdfService.generateBookingProformaInvoicePdf(booking);
            byte[] allotment = pdfService.generateAllotmentLetterPdf(booking);
            byte[] delivery = pdfService.generateDeliveryConfirmationPdf(booking);

            booking.setBookingReceiptUrl(storeGeneratedDocument(booking.getBookingReceiptUrl(), booking.getId(), "booking-receipt", receipt));
            booking.setProformaInvoiceUrl(storeGeneratedDocument(booking.getProformaInvoiceUrl(), booking.getId(), "proforma-invoice", proforma));
            booking.setAllotmentLetterUrl(storeGeneratedDocument(booking.getAllotmentLetterUrl(), booking.getId(), "allotment-letter", allotment));
            booking.setDeliveryConfirmationLetterUrl(storeGeneratedDocument(booking.getDeliveryConfirmationLetterUrl(), booking.getId(), "delivery-confirmation", delivery));

            docs.put("Booking-Confirmation-Receipt-" + booking.getId() + ".pdf", receipt);
            docs.put("Proforma-Invoice-" + booking.getId() + ".pdf", proforma);
            docs.put("Car-Allotment-Letter-" + booking.getId() + ".pdf", allotment);
            docs.put("Delivery-Confirmation-Letter-" + booking.getId() + ".pdf", delivery);

            LoanDetail loan = loanDetailRepository.findByBookingId(booking.getId()).orElse(null);
            if (loan != null && "Approved".equalsIgnoreCase(loan.getStatus())) {
                byte[] sanction = pdfService.generateFinanceSanctionLetterPdf(booking, loan);
                byte[] agreement = pdfService.generateFinanceAgreementPdf(booking, loan);
                booking.setFinanceSanctionLetterUrl(storeGeneratedDocument(booking.getFinanceSanctionLetterUrl(), booking.getId(), "finance-sanction-letter", sanction));
                booking.setFinanceAgreementUrl(storeGeneratedDocument(booking.getFinanceAgreementUrl(), booking.getId(), "finance-agreement", agreement));
                if (blank(booking.getLoanDocumentUrl())) booking.setLoanDocumentUrl(booking.getFinanceAgreementUrl());
                docs.put("Finance-Sanction-Letter-" + booking.getId() + ".pdf", sanction);
                docs.put("Finance-Agreement-" + booking.getId() + ".pdf", agreement);
            }

            bookingRepository.save(booking);

            sendDocsEmail(booking, "DK3 Cars Booking Confirmed - #" + booking.getId(),
                    """
                    Your booking is confirmed.

                    Booking ID: #%s
                    Car: %s
                    Delivery Date: %s

                    Booking confirmation documents are attached for your reference.
                    """.formatted(booking.getId(), carName(booking), booking.getExpectedDeliveryDate() != null ? booking.getExpectedDeliveryDate() : "To be assigned"),
                    docs);
        } catch (IOException ignored) {
        }
    }

    private void sendInitialBookingEmailPack(Booking booking) {
        try {
            Map<String, byte[]> docs = new LinkedHashMap<>();
            byte[] acknowledgement = pdfService.generateBookingAcknowledgementPdf(booking);
            byte[] application = pdfService.generateBookingApplicationSummaryPdf(booking);
            byte[] receipt = pdfService.generateBookingAmountReceiptPdf(booking);
            byte[] proforma = pdfService.generateBookingProformaInvoicePdf(booking);

            booking.setBookingReceiptUrl(storeGeneratedDocument(booking.getBookingReceiptUrl(), booking.getId(), "booking-receipt", receipt));
            booking.setProformaInvoiceUrl(storeGeneratedDocument(booking.getProformaInvoiceUrl(), booking.getId(), "proforma-invoice", proforma));
            bookingRepository.save(booking);

            docs.put("Booking-Acknowledgement-" + booking.getId() + ".pdf", acknowledgement);
            docs.put("Booking-Application-Summary-" + booking.getId() + ".pdf", application);
            docs.put("Booking-Amount-Receipt-" + booking.getId() + ".pdf", receipt);
            docs.put("Proforma-Invoice-" + booking.getId() + ".pdf", proforma);

            sendDocsEmail(booking, "DK3 Cars Booking Documents - #" + booking.getId(),
                    """
                    Your booking request has been received successfully.

                    Booking ID: #%s
                    Vehicle: %s
                    Booking Amount: %s
                    Expected Delivery: %s

                    Attached documents:
                    - Booking acknowledgement
                    - Booking application summary
                    - Booking amount receipt
                    - Proforma invoice

                    Please review the attached documents and keep them for your records.
                    """.formatted(
                            booking.getId(),
                            carName(booking),
                            money(booking.getBookingAmount()),
                            booking.getExpectedDeliveryDate() != null ? booking.getExpectedDeliveryDate() : "To be assigned"
                    ),
                    docs);
        } catch (IOException ignored) {
        }
    }

    private void sendDocsEmail(Booking booking, String subject, String body, Map<String, byte[]> docs) {
        String to = booking.getCustomer() != null ? booking.getCustomer().getEmail() : null;
        if (!blank(to) && docs != null && !docs.isEmpty()) {
            emailService.sendBookingConfirmationWithAttachments(to, subject, body, docs);
        }
    }

    private String storeGeneratedDocument(String currentUrl, Long bookingId, String key, byte[] bytes) throws IOException {
        Path dir = Path.of("uploads", "documents", "generated");
        Files.createDirectories(dir);
        String fileName = "booking-" + bookingId + "-" + key + ".pdf";
        String generatedUrl = "/uploads/documents/generated/" + fileName;
        if (!blank(currentUrl) && !generatedUrl.equals(currentUrl) && !currentUrl.startsWith("/uploads/documents/generated/booking-" + bookingId + "-")) {
            return currentUrl;
        }
        Files.write(dir.resolve(fileName), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return generatedUrl;
    }

    private void clearBookingDocumentField(Booking booking, String category) {
        if (booking == null || category == null) return;
        switch (category) {
            case "AadhaarPhoto" -> booking.setAadhaarPhotoUrl(null);
            case "PanPhoto" -> booking.setPanPhotoUrl(null);
            case "SignaturePhoto" -> booking.setSignaturePhotoUrl(null);
            case "PassportPhoto" -> booking.setPassportPhotoUrl(null);
            case "PaymentScreenshot" -> booking.setPaymentScreenshotUrl(null);
            case "DownPaymentReceipt" -> booking.setDownPaymentReceiptUrl(null);
            case "BookingReceipt" -> booking.setBookingReceiptUrl(null);
            case "ProformaInvoice" -> booking.setProformaInvoiceUrl(null);
            case "AllotmentLetter" -> booking.setAllotmentLetterUrl(null);
            case "DeliveryConfirmation" -> booking.setDeliveryConfirmationLetterUrl(null);
            case "InsurancePolicy" -> booking.setInsuranceDocumentUrl(null);
            case "TemporaryRegistration" -> booking.setTemporaryRegistrationUrl(null);
            case "FinalInvoice" -> booking.setFinalInvoiceUrl(null);
            case "RegistrationCertificate" -> booking.setRegistrationCertificateUrl(null);
            case "PucCertificate" -> booking.setPucCertificateUrl(null);
            case "WarrantyDocument" -> booking.setWarrantyDocumentUrl(null);
            case "ServiceBook" -> booking.setServiceBookUrl(null);
            case "DeliveryNote" -> booking.setDeliveryNoteUrl(null);
            case "RoadTaxReceipt" -> booking.setRoadTaxReceiptUrl(null);
            case "FinanceSanctionLetter" -> booking.setFinanceSanctionLetterUrl(null);
            case "FinanceAgreement" -> booking.setFinanceAgreementUrl(null);
            case "LoanDocument" -> booking.setLoanDocumentUrl(null);
            default -> { }
        }
    }

    private void deleteStoredFile(String fileUrl) {
        if (blank(fileUrl)) return;
        try {
            String normalized = fileUrl.trim().replace('\\', '/');
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            if (!normalized.startsWith("uploads/")) {
                return;
            }

            Path uploadsRoot = Path.of("uploads").normalize();
            Path filePath = Path.of(normalized).normalize();
            if (!filePath.startsWith(uploadsRoot)) {
                return;
            }
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String carName(Booking booking) {
        return booking.getCar() == null ? "Vehicle not assigned"
                : booking.getCar().getBrand() + " " + booking.getCar().getModel();
    }
    private String money(double value) { return "Rs. " + String.format("%,.2f", value); }
}
