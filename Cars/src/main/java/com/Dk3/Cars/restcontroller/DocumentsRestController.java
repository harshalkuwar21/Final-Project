package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Customer;
import com.Dk3.Cars.entity.Document;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.CustomerRepository;
import com.Dk3.Cars.repository.DocumentRepository;
import com.Dk3.Cars.service.DocumentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentsRestController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // ============================================
    // GET ALL DOCUMENTS
    // ============================================

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllDocuments() {
        try {
            List<Document> documents = documentService.getAllDocuments();
            List<Map<String, Object>> response = new ArrayList<>();

            for (Document doc : documents) {
                response.add(documentToMap(doc));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // ============================================
    // GET DOCUMENT BY ID
    // ============================================

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDocumentById(@PathVariable Long id) {
        try {
            Optional<Document> document = documentService.getDocumentById(id);
            if (document.isPresent()) {
                return ResponseEntity.ok(documentToMap(document.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }

    // ============================================
    // UPLOAD DOCUMENT
    // ============================================

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "carId", required = false) Long carId,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "expiryDate", required = false) String expiryDateStr) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Upload file and get URL
            String fileUrl = documentService.uploadDocument(file);

            if (fileUrl == null) {
                response.put("success", false);
                response.put("message", "Failed to upload file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Create document entity
            Document document = new Document();
            document.setFileName(file.getOriginalFilename());
            document.setFilePath(fileUrl);
            document.setFileUrl(fileUrl);
            document.setDocumentType(documentType);
            document.setUploadDate(LocalDate.now());

            // Associate with car if provided
            if (carId != null && carId > 0) {
                Optional<Car> car = carRepository.findById(carId);
                car.ifPresent(document::setCar);
            }

            // Associate with customer if provided
            if (customerId != null && customerId > 0) {
                Optional<Customer> customer = customerRepository.findById(customerId);
                customer.ifPresent(document::setCustomer);
            }

            // Set expiry date if provided
            if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                try {
                    LocalDate expiryDate = LocalDate.parse(expiryDateStr);
                    document.setExpiryDate(expiryDate);
                } catch (Exception e) {
                    // Invalid date format, skip expiry date
                }
            }

            // Save document
            Document savedDocument = documentService.saveDocument(document);

            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("document", documentToMap(savedDocument));
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to process file upload");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ============================================
    // DELETE DOCUMENT
    // ============================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Document> document = documentService.getDocumentById(id);

            if (document.isEmpty()) {
                response.put("success", false);
                response.put("message", "Document not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Delete file from disk
            try {
                Path filePath = Paths.get(document.get().getFilePath().replace("/uploads/documents/", "uploads/documents/"));
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (Exception e) {
                // Log but continue with database deletion
                e.printStackTrace();
            }

            // Delete from database
            documentService.deleteDocument(id);

            response.put("success", true);
            response.put("message", "Document deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to delete document");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ============================================
    // GET DOCUMENTS BY TYPE
    // ============================================

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Map<String, Object>>> getDocumentsByType(@PathVariable String type) {
        try {
            List<Document> documents = documentService.getDocumentsByType(type);
            List<Map<String, Object>> response = new ArrayList<>();

            for (Document doc : documents) {
                response.add(documentToMap(doc));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // ============================================
    // GET DOCUMENTS BY CAR
    // ============================================

    @GetMapping("/car/{carId}")
    public ResponseEntity<List<Map<String, Object>>> getDocumentsByCar(@PathVariable Long carId) {
        try {
            List<Document> documents = documentService.getDocumentsByCar(carId);
            List<Map<String, Object>> response = new ArrayList<>();

            for (Document doc : documents) {
                response.add(documentToMap(doc));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // ============================================
    // GET DOCUMENTS BY CUSTOMER
    // ============================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getDocumentsByCustomer(@PathVariable Long customerId) {
        try {
            List<Document> documents = documentService.getDocumentsByCustomer(customerId);
            List<Map<String, Object>> response = new ArrayList<>();

            for (Document doc : documents) {
                response.add(documentToMap(doc));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // ============================================
    // GET EXPIRING DOCUMENTS
    // ============================================

    @GetMapping("/expiring/{days}")
    public ResponseEntity<List<Map<String, Object>>> getExpiringDocuments(@PathVariable int days) {
        try {
            LocalDate futureDate = LocalDate.now().plusDays(days);
            List<Document> documents = documentService.getDocumentsExpiringSoon(futureDate);
            List<Map<String, Object>> response = new ArrayList<>();

            for (Document doc : documents) {
                response.add(documentToMap(doc));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // ============================================
    // GET CARS (for dropdown)
    // ============================================

    @GetMapping("/cars")
    public ResponseEntity<List<Map<String, Object>>> getCars() {
        try {
            List<Car> cars = carRepository.findAll();
            List<Map<String, Object>> response = new ArrayList<>();

            for (Car car : cars) {
                Map<String, Object> carMap = new HashMap<>();
                carMap.put("id", car.getId());
                carMap.put("brand", car.getBrand());
                carMap.put("model", car.getModel());
                carMap.put("vin", car.getVin());
                response.add(carMap);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // ============================================
    // GET CUSTOMERS (for dropdown)
    // ============================================

    @GetMapping("/customers")
    public ResponseEntity<List<Map<String, Object>>> getCustomers() {
        try {
            List<Customer> customers = customerRepository.findAll();
            List<Map<String, Object>> response = new ArrayList<>();

            for (Customer customer : customers) {
                Map<String, Object> custMap = new HashMap<>();
                custMap.put("id", customer.getId());
                custMap.put("firstName", customer.getName());
                custMap.put("lastName", customer.getMobile());
                custMap.put("email", customer.getEmail());
                response.add(custMap);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private Map<String, Object> documentToMap(Document doc) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", doc.getId());
        map.put("documentType", doc.getDocumentType());
        map.put("fileName", doc.getFileName());
        map.put("filePath", doc.getFilePath());
        map.put("fileUrl", doc.getFileUrl());
        map.put("uploadDate", doc.getUploadDate());
        map.put("expiryDate", doc.getExpiryDate());

        // Add related car info
        if (doc.getCar() != null) {
            Map<String, Object> carMap = new HashMap<>();
            carMap.put("id", doc.getCar().getId());
            carMap.put("brand", doc.getCar().getBrand());
            carMap.put("model", doc.getCar().getModel());
            carMap.put("vin", doc.getCar().getVin());
            map.put("car", carMap);
        }

        // Add related customer info
        if (doc.getCustomer() != null) {
            Map<String, Object> custMap = new HashMap<>();
            custMap.put("id", doc.getCustomer().getId());
            custMap.put("firstName", doc.getCustomer().getName());
            custMap.put("lastName", doc.getCustomer().getMobile());
            custMap.put("email", doc.getCustomer().getEmail());
            map.put("customer", custMap);
        }

        return map;
    }
}
