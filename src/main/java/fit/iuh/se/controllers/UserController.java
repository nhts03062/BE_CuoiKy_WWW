package fit.iuh.se.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fit.iuh.se.dtos.UserCreateDTO;
import fit.iuh.se.dtos.UserDTO;
import fit.iuh.se.dtos.UserUpdateDTO;
import fit.iuh.se.services.UserService;
import jakarta.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody UserCreateDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(validationErrors(result));
        }
        try {
            UserDTO saved = userService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Tạo người dùng thành công!",
                    "user", saved
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody UserUpdateDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(validationErrors(result));
        }
        try {
            UserDTO updated = userService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật người dùng thành công!",
                    "user", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.activate(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.deactivate(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> validationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError err : result.getFieldErrors()) {
            errors.put(err.getField(), err.getDefaultMessage());
        }
        return Map.of("status", 400, "errors", errors);
    }
}

