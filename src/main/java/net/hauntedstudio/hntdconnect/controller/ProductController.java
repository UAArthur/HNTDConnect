package net.hauntedstudio.hntdconnect.controller;

import net.hauntedstudio.hntdconnect.dto.StatusResponse;
import net.hauntedstudio.hntdconnect.dto.prdct.ProductCreateRequest;
import net.hauntedstudio.hntdconnect.entities.OrganizationEntity;
import net.hauntedstudio.hntdconnect.services.JwtService;
import net.hauntedstudio.hntdconnect.services.OrganizationService;
import net.hauntedstudio.hntdconnect.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    //TODO: Delete Product
    //TODO: Update Product
    private final OrganizationService organizationService;
    private final ProductService productService;
    private final JwtService jwtService;

    public ProductController(OrganizationService organizationService, ProductService productService, JwtService jwtService) {
        this.organizationService = organizationService;
        this.productService = productService;
        this.jwtService = jwtService;
    }


    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductCreateRequest productCreateRequest,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        if (productCreateRequest == null) {
            StatusResponse response = new StatusResponse(400, "Invalid product creation request");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authorizationHeader.replace("Bearer ", "");
        String userUuid = jwtService.getUserUuidFromToken(token);
        OrganizationEntity org = organizationService.findByUuid(productCreateRequest.organizationId());

        if (org == null) {
            StatusResponse response = new StatusResponse(404, "Organization not found");
            return ResponseEntity.status(404).body(response);
        }
        if (!organizationService.isMember(org.getUuid(), userUuid)) {
            StatusResponse response = new StatusResponse(403, "You are not a member of this organization");
            return ResponseEntity.status(403).body(response);
        }

        this.productService.createProduct(productCreateRequest);
        StatusResponse response = new StatusResponse(201, "Product created successfully");
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/get/{organizationId}/{productId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductById(@PathVariable String organizationId,
                                            @PathVariable String productId,
                                            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userUuid = jwtService.getUserUuidFromToken(token);
        OrganizationEntity org = organizationService.findByUuid(organizationId);
        if (org == null) {
            return ResponseEntity.status(404).body(new StatusResponse(404, "Organization not found"));
        }
        if (!organizationService.isMember(org.getUuid(), userUuid)) {
            return ResponseEntity.status(403).body(new StatusResponse(403, "You are not a member of this organization"));
        }

        if (productId.equalsIgnoreCase("@")){
            var products = productService.productRepository.findByOrganization_Uuid(organizationId);
            var productResponses = products.stream().map(productService::toResponse).toList();
            return ResponseEntity.ok().body(productResponses);
        }

        return productService.productRepository.findByUuid(productId)
                .filter(product -> product.getOrganization().getUuid().equals(organizationId))
                .<ResponseEntity<?>>map(product -> {
                    var productResponse = productService.toResponse(product);
                    return ResponseEntity.ok().body(productResponse);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(new StatusResponse(404, "Product not found in this organization")));
    }
}
