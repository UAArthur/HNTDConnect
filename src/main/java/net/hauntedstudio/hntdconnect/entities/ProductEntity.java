package net.hauntedstudio.hntdconnect.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.hauntedstudio.hntdconnect.dto.prdct.ProductStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "products")
public class ProductEntity {

    @Id
    private String uuid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(name = "current_version")
    private String currentVersion;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String platforms;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductTokenEntity> tokens = new ArrayList<>();
}
