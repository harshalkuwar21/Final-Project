package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Sale;
import com.Dk3.Cars.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private CarService carService;

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Optional<Sale> getSaleById(Long id) {
        return saleRepository.findById(id);
    }

    public Sale saveSale(Sale sale) {
        // Mark car as sold when sale is created
        if (sale.getCar() != null) {
            carService.markCarAsSold(sale.getCar().getId());
        }
        return saleRepository.save(sale);
    }

    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    public List<Sale> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId);
    }

    public List<Sale> getSalesBySalesExecutive(Long salesExecutiveId) {
        return saleRepository.findBySalesExecutiveUserid(salesExecutiveId);
    }

    public Double getTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        return saleRepository.getTotalRevenueBetweenDates(startDate, endDate);
    }

    public Double getMonthlyRevenue(int year, int month) {
        return saleRepository.getMonthlyRevenue(year, month);
    }

    public Double getYearlyRevenue(int year) {
        return saleRepository.getYearlyRevenue(year);
    }

    public List<Sale> getPendingPayments() {
        return saleRepository.findPendingPayments();
    }

    public long countTodaySales() {
        return saleRepository.countTodaySales();
    }

    public long countCurrentMonthSales() {
        return saleRepository.countCurrentMonthSales();
    }

    public long countYearlySales(int year) {
        return saleRepository.countYearlySales(year);
    }

    public List<Sale> getRecentSales() {
        return saleRepository.findTop5ByOrderBySoldDateDesc();
    }
}