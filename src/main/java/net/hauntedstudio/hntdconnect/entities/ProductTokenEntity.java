package net.hauntedstudio.hntdconnect.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_tokens")
@Getter
@Setter
@NoArgsConstructor
public class ProductTokenEntity {

    @Id
    private String uuid;

    @Column(name = "token_id", nullable = false, unique = true)
    private String tokenId;

    @Column(name = "token_hmac", nullable = false, length = 512)
    private String tokenHmac;

    @Column(name = "token_name")
    private String name;

    @Column(name = "permissions")
    private Long permissions;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_uuid", nullable = false)
    private ProductEntity product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ProductTokenEntity(String uuid, String tokenId, String tokenHmac, String name, Long permissions, ProductEntity product, OrganizationEntity organization) {
        this.uuid = uuid;
        this.tokenId = tokenId;
        this.tokenHmac = tokenHmac;
        this.name = name;
        this.permissions = permissions;
        this.product = product;
        this.organization = organization;
    }
}
