package net.hauntedstudio.hntdconnect.services;

import net.hauntedstudio.hntdconnect.dto.prdct.ProductCreateRequest;
import net.hauntedstudio.hntdconnect.dto.prdct.ProductResponse;
import net.hauntedstudio.hntdconnect.entities.ProductEntity;
import net.hauntedstudio.hntdconnect.repositories.OrganizationRepository;
import net.hauntedstudio.hntdconnect.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProductService {
    public final ProductRepository productRepository;
    private final OrganizationService organizationService;
    private final UserService userService;

    public ProductService(ProductRepository productRepository, OrganizationService organizationService, UserService userService) {
        this.productRepository = productRepository;
        this.organizationService = organizationService;
        this.userService = userService;
    }

    public ProductEntity createProduct(ProductCreateRequest productCreateRequest) {
        ProductEntity product = new ProductEntity();
        product.setUuid(UUID.randomUUID().toString());
        product.setOrganization(this.organizationService.findByUuid(productCreateRequest.organizationId()));
        product.setName(productCreateRequest.name());
        product.setDescription(productCreateRequest.description());
        product.setStatus(productCreateRequest.status());
        product.setCurrentVersion(productCreateRequest.currentVersion());
        product.setReleaseDate(null);
        product.setPlatforms(productCreateRequest.platforms());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getUuid(),
                product.getOrganization().getUuid(),
                product.getName(),
                product.getDescription(),
                product.getStatus(),
                product.getCurrentVersion(),
                product.getReleaseDate(),
                product.getPlatforms(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public boolean existsByUuid(String productId) {
        return productRepository.existsByUuid(productId);
    }

    public boolean belongsToOrganization(String productId, String organizationId) {
        return productRepository.existsByUuidAndOrganization_Uuid(productId, organizationId);
    }

    //TODO: Add a way, to create tokens with permissions
    //TODO: ProductPermission enum
}
