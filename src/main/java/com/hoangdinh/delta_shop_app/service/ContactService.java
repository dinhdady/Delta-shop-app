package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.contact.ContactReplyRequest;
import com.hoangdinh.delta_shop_app.dto.request.contact.ContactRequest;
import com.hoangdinh.delta_shop_app.dto.response.contact.ContactResponse;
import com.hoangdinh.delta_shop_app.dto.response.contact.ContactStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContactService {

    ContactResponse submitContact(ContactRequest request, UUID userId);

    ContactResponse submitGuestContact(ContactRequest request);

    ContactResponse replyToContact(UUID contactId, ContactReplyRequest request);

    Page<ContactResponse> getAllContacts(Pageable pageable);

    Page<ContactResponse> getContactsByStatus(String status, Pageable pageable);

    ContactResponse getContactDetail(UUID contactId);

    ContactResponse updateContactStatus(UUID contactId, String status);

    void deleteContact(UUID contactId);

    long getPendingCount();

    long getTotalCount();
    ContactStats getContactStats();
}