package com.hoangdinh.delta_shop_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    @Column(nullable = false, length = 200) private String name;
    @Column(nullable = false, unique = true, length = 255) private String slug;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "image_url", length = 500) private String imageUrl;
    @Column(name = "icon_class", length = 100) private String iconClass;
    @Column(name = "sort_order", nullable = false) @Builder.Default private Integer sortOrder = 0;
    @Column(name = "is_active", nullable = false) @Builder.Default private boolean active = true;
    @Column(name = "meta_title", length = 300) private String metaTitle;
    @Column(name = "meta_description", length = 500) private String metaDescription;
    @Column private String path;
    @Column(nullable = false) @Builder.Default private Short depth = 0;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default private List<Category> children = new ArrayList<>();
    @OneToMany(mappedBy = "category")
    @Builder.Default private List<Product> products = new ArrayList<>();
}