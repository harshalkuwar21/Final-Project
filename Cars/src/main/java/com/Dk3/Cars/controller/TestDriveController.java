package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.TestDrive;
import com.Dk3.Cars.service.*;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/testdrives")
public class TestDriveController {

    @Autowired
    private TestDriveService testDriveService;

   
    @Autowired
    private CarService carService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public String listTestDrives(Model model) {
        List<TestDrive> testDrives = testDriveService.getAllTestDrives();
        model.addAttribute("testDrives", testDrives);
        return "testdrives";
    }

    @GetMapping("/add")
    public String showAddTestDriveForm(Model model) {
        model.addAttribute("testDrive", new TestDrive());
        model.addAttribute("cars", carService.getAvailableCars());
        model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        return "testdrive-add";
    }

    @PostMapping("/add")
    public String addTestDrive(@ModelAttribute TestDrive testDrive, RedirectAttributes redirectAttributes) {
        testDriveService.saveTestDrive(testDrive);
        redirectAttributes.addFlashAttribute("success", "Test drive scheduled successfully!");
        return "redirect:/testdrives";
    }

    @GetMapping("/edit/{id}")
    public String showEditTestDriveForm(@PathVariable Long id, Model model) {
        TestDrive testDrive = testDriveService.getTestDriveById(id).orElse(null);
        if (testDrive == null) {
            return "redirect:/testdrives";
        }
        model.addAttribute("testDrive", testDrive);
       
        model.addAttribute("cars", carService.getAvailableCars());
        model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        return "testdrive-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTestDrive(@PathVariable Long id, @ModelAttribute TestDrive testDrive,
                                 RedirectAttributes redirectAttributes) {
        testDrive.setId(id);
        testDriveService.saveTestDrive(testDrive);
        redirectAttributes.addFlashAttribute("success", "Test drive updated successfully!");
        return "redirect:/testdrives";
    }

    @GetMapping("/delete/{id}")
    public String deleteTestDrive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        testDriveService.deleteTestDrive(id);
        redirectAttributes.addFlashAttribute("success", "Test drive deleted successfully!");
        return "redirect:/testdrives";
    }

    @PostMapping("/{id}/complete")
    public String completeTestDrive(@PathVariable Long id, @RequestParam String feedback,
                                   @RequestParam(required = false) Boolean convertedToSale,
                                   RedirectAttributes redirectAttributes) {
        testDriveService.markTestDriveCompleted(id, feedback);

        if (convertedToSale != null && convertedToSale) {
            testDriveService.markConvertedToSale(id);
            redirectAttributes.addFlashAttribute("success", "Test drive completed and marked as converted to sale!");
        } else {
            redirectAttributes.addFlashAttribute("success", "Test drive completed successfully!");
        }

        return "redirect:/testdrives";
    }

    @GetMapping("/scheduled")
    public String getScheduledTestDrives(Model model) {
        List<TestDrive> testDrives = testDriveService.getTestDrivesByStatus("Scheduled");
        model.addAttribute("testDrives", testDrives);
        model.addAttribute("statusFilter", "Scheduled");
        return "testdrives";
    }

    @GetMapping("/completed")
    public String getCompletedTestDrives(Model model) {
        List<TestDrive> testDrives = testDriveService.getTestDrivesByStatus("Completed");
        model.addAttribute("testDrives", testDrives);
        model.addAttribute("statusFilter", "Completed");
        return "testdrives";
    }

    @GetMapping("/feedback")
    public String getTestDrivesWithFeedback(Model model) {
        List<TestDrive> testDrives = testDriveService.getCompletedTestDrivesWithFeedback();
        model.addAttribute("testDrives", testDrives);
        model.addAttribute("feedbackView", true);
        return "testdrives";
    }
}