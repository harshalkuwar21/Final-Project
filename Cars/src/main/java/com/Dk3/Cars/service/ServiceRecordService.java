package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.ServiceRecord;
import com.Dk3.Cars.repository.ServiceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceRecordService {

    @Autowired
    private ServiceRecordRepository serviceRecordRepository;

    public List<ServiceRecord> getAllServiceRecords() {
        return serviceRecordRepository.findAll();
    }

    public Optional<ServiceRecord> getServiceRecordById(Long id) {
        return serviceRecordRepository.findById(id);
    }

    public ServiceRecord saveServiceRecord(ServiceRecord serviceRecord) {
        return serviceRecordRepository.save(serviceRecord);
    }

    public void deleteServiceRecord(Long id) {
        serviceRecordRepository.deleteById(id);
    }

    public List<ServiceRecord> getServiceRecordsByCar(Long carId) {
        return serviceRecordRepository.findByCarId(carId);
    }

    public List<ServiceRecord> getUpcomingServices(LocalDate date) {
        return serviceRecordRepository.findUpcomingServices(date);
    }

    public List<ServiceRecord> getExpiringWarranties(LocalDate date) {
        return serviceRecordRepository.findExpiringWarranties(date);
    }

    public Double getServiceCostsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return serviceRecordRepository.getServiceCostsBetweenDates(startDate, endDate);
    }
}