package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.TestDrive;
import com.Dk3.Cars.repository.TestDriveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TestDriveService {

    @Autowired
    private TestDriveRepository testDriveRepository;

    public List<TestDrive> getAllTestDrives() {
        return testDriveRepository.findAll();
    }

    public Optional<TestDrive> getTestDriveById(Long id) {
        return testDriveRepository.findById(id);
    }

    public TestDrive saveTestDrive(TestDrive testDrive) {
        return testDriveRepository.save(testDrive);
    }

    public void deleteTestDrive(Long id) {
        testDriveRepository.deleteById(id);
    }

    public List<TestDrive> getTestDrivesByStatus(String status) {
        return testDriveRepository.findByStatus(status);
    }

    public List<TestDrive> getTestDrivesByCustomer(Long customerId) {
        return testDriveRepository.findByCustomerId(customerId);
    }

    public List<TestDrive> getTestDrivesBySalesExecutive(Long salesExecutiveId) {
        return testDriveRepository.findBySalesExecutiveUserid(salesExecutiveId);
    }

    public List<TestDrive> getScheduledTestDrivesBetween(LocalDateTime start, LocalDateTime end) {
        return testDriveRepository.findScheduledTestDrivesBetween(start, end);
    }

    public long countTodayScheduledTestDrives() {
        return testDriveRepository.countTodayScheduledTestDrives();
    }

    public long countConvertedToSales() {
        return testDriveRepository.countConvertedToSales();
    }

    public List<TestDrive> getCompletedTestDrivesWithFeedback() {
        return testDriveRepository.findCompletedWithFeedback();
    }

    public void markTestDriveCompleted(Long testDriveId, String feedback) {
        Optional<TestDrive> testDriveOpt = testDriveRepository.findById(testDriveId);
        if (testDriveOpt.isPresent()) {
            TestDrive testDrive = testDriveOpt.get();
            testDrive.setStatus("Completed");
            testDrive.setCompletedDateTime(LocalDateTime.now());
            testDrive.setFeedback(feedback);
            testDriveRepository.save(testDrive);
        }
    }

    public void markConvertedToSale(Long testDriveId) {
        Optional<TestDrive> testDriveOpt = testDriveRepository.findById(testDriveId);
        if (testDriveOpt.isPresent()) {
            TestDrive testDrive = testDriveOpt.get();
            testDrive.setConvertedToSale(true);
            testDriveRepository.save(testDrive);
        }
    }
}