package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.contact.ContactReplyRequest;
import com.hoangdinh.delta_shop_app.dto.request.contact.ContactRequest;
import com.hoangdinh.delta_shop_app.dto.response.contact.ContactResponse;
import com.hoangdinh.delta_shop_app.dto.response.contact.ContactStats;
import com.hoangdinh.delta_shop_app.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "APIs for contact management")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @Operation(summary = "Submit contact form (guest)")
    public ResponseEntity<ContactResponse> submitContact(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactService.submitGuestContact(request));
    }

    @PostMapping("/auth")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Submit contact form (authenticated user)")
    public ResponseEntity<ContactResponse> submitAuthContact(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactService.submitContact(request, userId));
    }

    @GetMapping("/admin")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all contacts (Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ContactResponse>> getAllContacts(Pageable pageable) {
        return ResponseEntity.ok(contactService.getAllContacts(pageable));
    }

    @GetMapping("/admin/status/{status}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get contacts by status (Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ContactResponse>> getContactsByStatus(
            @PathVariable String status,
            Pageable pageable) {
        return ResponseEntity.ok(contactService.getContactsByStatus(status, pageable));
    }

    @GetMapping("/admin/{contactId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get contact detail (Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ContactResponse> getContactDetail(@PathVariable UUID contactId) {
        return ResponseEntity.ok(contactService.getContactDetail(contactId));
    }

    @PostMapping("/admin/{contactId}/reply")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Reply to contact (Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ContactResponse> replyToContact(
            @PathVariable UUID contactId,
            @Valid @RequestBody ContactReplyRequest request) {
        return ResponseEntity.ok(contactService.replyToContact(contactId, request));
    }

    @PutMapping("/admin/{contactId}/status")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update contact status (Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ContactResponse> updateContactStatus(
            @PathVariable UUID contactId,
            @RequestParam String status) {
        return ResponseEntity.ok(contactService.updateContactStatus(contactId, status));
    }

    @DeleteMapping("/admin/{contactId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete contact (Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteContact(@PathVariable UUID contactId) {
        contactService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/stats")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get contact statistics (Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ContactStats> getContactStats() {
        return ResponseEntity.ok(contactService.getContactStats());
    }
}
