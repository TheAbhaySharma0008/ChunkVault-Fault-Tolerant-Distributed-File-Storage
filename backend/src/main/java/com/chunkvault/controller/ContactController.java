package com.chunkvault.controller;

import com.chunkvault.entity.ContactMessage;
import com.chunkvault.repository.ContactMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitContact(@RequestBody ContactMessage message) {
        contactMessageRepository.save(message);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Your message has been recorded successfully.");
        return ResponseEntity.ok(response);
    }
}
