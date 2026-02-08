package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Document;
import com.Dk3.Cars.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    private final String UPLOAD_DIR = "uploads/documents/";

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    public List<Document> getDocumentsByCar(Long carId) {
        return documentRepository.findByCarId(carId);
    }

    public List<Document> getDocumentsByCustomer(Long customerId) {
        return documentRepository.findByCustomerId(customerId);
    }

    public List<Document> getDocumentsByType(String documentType) {
        return documentRepository.findByDocumentType(documentType);
    }

    public List<Document> getExpiringDocuments(LocalDate date) {
        return documentRepository.findExpiringDocuments(date);
    }

    public List<Document> getDocumentsExpiringSoon(LocalDate futureDate) {
        return documentRepository.findDocumentsExpiringSoon(futureDate);
    }

    public String uploadDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        return "/uploads/documents/" + filename;
    }
}