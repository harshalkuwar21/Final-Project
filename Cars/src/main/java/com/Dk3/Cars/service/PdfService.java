package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Sale;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateInvoicePdf(Sale sale) throws IOException {
        String htmlContent = buildInvoiceHtml(sale);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Convert HTML to PDF
        ConverterProperties converterProperties = new ConverterProperties();
        HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties);

        document.close();
        return outputStream.toByteArray();
    }

    private String buildInvoiceHtml(Sale sale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
           .append("body { font-family: Arial, sans-serif; margin: 20px; }")
           .append(".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 20px; }")
           .append(".invoice-details { margin-bottom: 20px; }")
           .append(".customer-details, .car-details { margin-bottom: 15px; }")
           .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
           .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
           .append("th { background-color: #f2f2f2; }")
           .append(".total { font-weight: bold; font-size: 18px; }")
           .append("</style></head><body>")

           .append("<div class='header'>")
           .append("<h1>DK3 Cars - Invoice</h1>")
           .append("<p>Invoice No: INV-").append(sale.getId()).append("</p>")
           .append("<p>Date: ").append(sale.getSoldDate().format(formatter)).append("</p>")
           .append("</div>")

           .append("<div class='invoice-details'>")
           .append("<h3>Customer Details:</h3>")
           .append("<p>Name: ").append(sale.getCustomer() != null ? sale.getCustomer().getName() : "N/A").append("</p>")
           .append("<p>Email: ").append(sale.getCustomer() != null ? sale.getCustomer().getEmail() : "N/A").append("</p>")
           .append("<p>Mobile: ").append(sale.getCustomer() != null ? sale.getCustomer().getMobile() : "N/A").append("</p>")
           .append("</div>")

           .append("<div class='car-details'>")
           .append("<h3>Vehicle Details:</h3>")
           .append("<p>Brand: ").append(sale.getCar().getBrand()).append("</p>")
           .append("<p>Model: ").append(sale.getCar().getModel()).append("</p>")
           .append("<p>VIN: ").append(sale.getCar().getVin() != null ? sale.getCar().getVin() : "N/A").append("</p>")
           .append("</div>")

           .append("<table>")
           .append("<tr><th>Description</th><th>Amount</th></tr>")
           .append("<tr><td>Vehicle Price</td><td>₹").append(String.format("%.2f", sale.getSellingPrice())).append("</td></tr>")
           .append("<tr><td>Discount</td><td>-₹").append(String.format("%.2f", sale.getDiscount())).append("</td></tr>")
           .append("<tr><td>GST</td><td>₹").append(String.format("%.2f", sale.getGstAmount())).append("</td></tr>")
           .append("<tr class='total'><td>Total Amount</td><td>₹").append(String.format("%.2f", sale.getTotalAmount())).append("</td></tr>")
           .append("</table>")

           .append("<div style='margin-top: 30px;'>")
           .append("<p>Payment Method: ").append(sale.getPaymentMode()).append("</p>")
           .append("<p>Sales Executive: ").append(sale.getSalesExecutive() != null ?
                   sale.getSalesExecutive().getFirst() + " " + sale.getSalesExecutive().getLast() : "N/A").append("</p>")
           .append("</div>")

           .append("<div style='margin-top: 50px; text-align: center;'>")
           .append("<p>Thank you for your business!</p>")
           .append("<p>DK3 Cars Showroom</p>")
           .append("</div>")

           .append("</body></html>");

        return html.toString();
    }
}