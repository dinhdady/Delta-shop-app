package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.contact.ContactReplyRequest;
import com.hoangdinh.delta_shop_app.dto.request.contact.ContactRequest;
import com.hoangdinh.delta_shop_app.dto.response.contact.ContactResponse;
import com.hoangdinh.delta_shop_app.dto.response.contact.ContactStats;
import com.hoangdinh.delta_shop_app.entity.Contact;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.enums.ContactStatus;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.ContactRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.ContactService;
import com.hoangdinh.delta_shop_app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ContactResponse submitContact(ContactRequest request, UUID userId) {
        User user = userRepository.findById(userId).orElse(null);

        Contact contact = Contact.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(ContactStatus.PENDING)
                .user(user)
                .build();

        Contact saved = contactRepository.save(contact);
        log.info("New contact submitted: {} - {}", saved.getId(), saved.getEmail());

        // Gửi email xác nhận cho người dùng
        sendConfirmationEmail(saved);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ContactResponse submitGuestContact(ContactRequest request) {
        Contact contact = Contact.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(ContactStatus.PENDING)
                .build();

        Contact saved = contactRepository.save(contact);
        log.info("New guest contact submitted: {} - {}", saved.getId(), saved.getEmail());

        sendConfirmationEmail(saved);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ContactResponse replyToContact(UUID contactId, ContactReplyRequest request) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));

        contact.setAdminNote(request.getReply());
        contact.setRepliedAt(LocalDateTime.now());

        if (request.getStatus() != null) {
            contact.setStatus(ContactStatus.valueOf(request.getStatus()));
        } else {
            contact.setStatus(ContactStatus.REPLIED);
        }

        Contact saved = contactRepository.save(contact);

        // Gửi email phản hồi cho người dùng
        sendReplyEmail(saved);

        log.info("Replied to contact: {}", contactId);

        return mapToResponse(saved);
    }

    @Override
    public Page<ContactResponse> getAllContacts(Pageable pageable) {
        return contactRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ContactResponse> getContactsByStatus(String status, Pageable pageable) {
        ContactStatus contactStatus = ContactStatus.valueOf(status);
        return contactRepository.findByStatus(contactStatus, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public ContactResponse getContactDetail(UUID contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));
        return mapToResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse updateContactStatus(UUID contactId, String status) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));

        ContactStatus newStatus = ContactStatus.valueOf(status);
        contact.setStatus(newStatus);

        // Nếu chuyển sang trạng thái đã giải quyết hoặc đã đóng, có thể thêm logic
        if (newStatus == ContactStatus.RESOLVED || newStatus == ContactStatus.CLOSED) {
            contact.setRepliedAt(LocalDateTime.now());
        }

        Contact saved = contactRepository.save(contact);
        log.info("Updated contact status: {} -> {}", contactId, status);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteContact(UUID contactId) {
        if (!contactRepository.existsById(contactId)) {
            throw new ResourceNotFoundException("Contact", "id", contactId);
        }
        contactRepository.deleteById(contactId);
        log.info("Deleted contact: {}", contactId);
    }

    @Override
    public long getPendingCount() {
        return contactRepository.countByStatus(ContactStatus.PENDING);
    }

    @Override
    public long getTotalCount() {
        return contactRepository.count();
    }

    private void sendConfirmationEmail(Contact contact) {
        String subject = "Xác nhận liên hệ - Delta Sports";
        String content = String.format("""
            <html>
            <body>
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã liên hệ với Delta Sports.</p>
                <p>Chúng tôi đã nhận được tin nhắn của bạn và sẽ phản hồi trong thời gian sớm nhất.</p>
                <p><strong>Nội dung tin nhắn:</strong></p>
                <p><em>%s</em></p>
                <br/>
                <p>Trân trọng,<br/>Delta Sports Team</p>
            </body>
            </html>
            """, contact.getName(), contact.getMessage());

        emailService.sendEmail(contact.getEmail(), subject, content);
    }

    private void sendReplyEmail(Contact contact) {
        String subject = "Phản hồi từ Delta Sports - " + contact.getSubject();
        String content = String.format("""
            <html>
            <body>
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã liên hệ với Delta Sports.</p>
                <p><strong>Phản hồi từ chúng tôi:</strong></p>
                <div style="background: #f5f5f5; padding: 15px; border-radius: 5px;">
                    %s
                </div>
                <br/>
                <p>Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ hotline: <strong>1900 1009</strong></p>
                <br/>
                <p>Trân trọng,<br/>Delta Sports Team</p>
            </body>
            </html>
            """, contact.getName(), contact.getAdminNote());

        emailService.sendEmail(contact.getEmail(), subject, content);
    }
    @Override
    public ContactStats getContactStats() {
        long pending = contactRepository.countByStatus(ContactStatus.PENDING);
        long processing = contactRepository.countByStatus(ContactStatus.PROCESSING);
        long replied = contactRepository.countByStatus(ContactStatus.REPLIED);
        long resolved = contactRepository.countByStatus(ContactStatus.RESOLVED);
        long closed = contactRepository.countByStatus(ContactStatus.CLOSED);
        long total = contactRepository.count();

        return ContactStats.builder()
                .pending(pending)
                .processing(processing)
                .replied(replied)
                .resolved(resolved)
                .closed(closed)
                .total(total)
                .build();
    }
    private ContactResponse mapToResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .status(contact.getStatus().name())
                .adminNote(contact.getAdminNote())
                .repliedAt(contact.getRepliedAt())
                .createdAt(contact.getCreatedAt())
                .build();
    }
}