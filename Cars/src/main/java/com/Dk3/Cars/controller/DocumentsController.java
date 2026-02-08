package com.Dk3.Cars.controller;

import com.Dk3.Cars.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentsController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/documents")
    public String documentsPage(Model model) {
        // Add any model attributes if needed for the page
        model.addAttribute("documentTypes", new String[]{"RC", "Insurance", "Invoice", "Form20", "Form21", "CustomerID", "Other"});
        return "documents";
    }
}