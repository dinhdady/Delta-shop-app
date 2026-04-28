package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Contact;
import com.hoangdinh.delta_shop_app.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    Page<Contact> findByStatus(ContactStatus status, Pageable pageable);

    List<Contact> findByStatusOrderByCreatedAtDesc(ContactStatus status);

    @Query("SELECT c FROM Contact c WHERE c.email = :email ORDER BY c.createdAt DESC")
    List<Contact> findByEmail(@Param("email") String email);

    @Query("SELECT c FROM Contact c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Contact> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // Thống kê theo từng status
    long countByStatus(ContactStatus status);

    // Thống kê tất cả status cùng lúc
    @Query("SELECT c.status, COUNT(c) FROM Contact c GROUP BY c.status")
    List<Object[]> countGroupByStatus();
}