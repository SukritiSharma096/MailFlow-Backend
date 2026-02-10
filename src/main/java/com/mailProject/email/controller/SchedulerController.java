package com.mailProject.email.controller;

import com.mailProject.email.dto.SchedulerRequestDto;
import com.mailProject.email.dto.SchedulerResponseDto;
import com.mailProject.email.service.SchedulerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedulers")
@CrossOrigin(origins = "*")
public class SchedulerController {
    @Autowired
    private SchedulerService service;

    @PostMapping("/save")
    public ResponseEntity<SchedulerResponseDto> create(
            @Valid @RequestBody SchedulerRequestDto dto) {

        SchedulerResponseDto response = service.create(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SchedulerResponseDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchedulerResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody SchedulerRequestDto dto) {

        return ResponseEntity.ok(service.update(id, dto));
    }
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<SchedulerResponseDto> toggleStatus(@PathVariable Long id) {
        SchedulerResponseDto response = service.toggleStatus(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Scheduler deleted successfully");
    }
}