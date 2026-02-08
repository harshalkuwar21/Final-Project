package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Customer;
import com.Dk3.Cars.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomersRestController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public List<Customer> list() {
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Customer create(@RequestBody Customer c) {
        c.setId(null);
        return customerRepository.save(c);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer c) {
        return customerRepository.findById(id).map(existing -> {
            existing.setName(c.getName());
            existing.setMobile(c.getMobile());
            existing.setEmail(c.getEmail());
            existing.setAddress(c.getAddress());
            existing.setLeadSource(c.getLeadSource());
            existing.setNextFollowUpDate(c.getNextFollowUpDate());
            existing.setNotes(c.getNotes());
            return customerRepository.save(existing);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        customerRepository.deleteById(id);
    }
}
