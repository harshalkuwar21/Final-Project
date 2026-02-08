package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Sale;
import com.Dk3.Cars.service.*;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

   
    @Autowired
    private CarService carService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/list")
    public String listSales(Model model) {
        List<Sale> sales = saleService.getAllSales();
        model.addAttribute("sales", sales);
        return "sales";
    }

    @GetMapping("/add")
    public String showAddSaleForm(Model model) {
        model.addAttribute("sale", new Sale());
        model.addAttribute("cars", carService.getAvailableCars());
        model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        return "sale-add";
    }

    @PostMapping("/add")
    public String addSale(@ModelAttribute Sale sale, RedirectAttributes redirectAttributes) {
        // Calculate GST (18% for cars)
        double gstRate = 0.18;
        sale.setGstAmount(sale.getSellingPrice() * gstRate);
        sale.setTotalAmount(sale.getSellingPrice() - sale.getDiscount() + sale.getGstAmount());

        saleService.saveSale(sale);
        redirectAttributes.addFlashAttribute("success", "Sale recorded successfully!");
        return "redirect:/sales";
    }

    @GetMapping("/edit/{id}")
    public String showEditSaleForm(@PathVariable Long id, Model model) {
        Sale sale = saleService.getSaleById(id).orElse(null);
        if (sale == null) {
            return "redirect:/sales";
        }
        model.addAttribute("sale", sale);
        model.addAttribute("cars", carService.getAllCars());
        model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        return "sale-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateSale(@PathVariable Long id, @ModelAttribute Sale sale,
                            RedirectAttributes redirectAttributes) {
        sale.setId(id);
        // Recalculate totals
        double gstRate = 0.18;
        sale.setGstAmount(sale.getSellingPrice() * gstRate);
        sale.setTotalAmount(sale.getSellingPrice() - sale.getDiscount() + sale.getGstAmount());

        saleService.saveSale(sale);
        redirectAttributes.addFlashAttribute("success", "Sale updated successfully!");
        return "redirect:/sales";
    }

    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        saleService.deleteSale(id);
        redirectAttributes.addFlashAttribute("success", "Sale deleted successfully!");
        return "redirect:/sales";
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<ByteArrayResource> downloadInvoice(@PathVariable Long id) throws IOException {
        Sale sale = saleService.getSaleById(id).orElse(null);
        if (sale == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = pdfService.generateInvoicePdf(sale);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    @GetMapping("/pending")
    public String getPendingPayments(Model model) {
        List<Sale> sales = saleService.getPendingPayments();
        model.addAttribute("sales", sales);
        model.addAttribute("statusFilter", "Pending");
        return "sales";
    }
}