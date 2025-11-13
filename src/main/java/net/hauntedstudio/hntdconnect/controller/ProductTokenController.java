package net.hauntedstudio.hntdconnect.controller;

import net.hauntedstudio.hntdconnect.dto.StatusResponse;
import net.hauntedstudio.hntdconnect.dto.prdct.token.ProductCreateTokenRequest;
import net.hauntedstudio.hntdconnect.dto.prdct.token.ProductTokenResponse;
import net.hauntedstudio.hntdconnect.services.JwtService;
import net.hauntedstudio.hntdconnect.services.OrganizationService;
import net.hauntedstudio.hntdconnect.services.ProductService;
import net.hauntedstudio.hntdconnect.services.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductTokenController {

    private final OrganizationService organizationService;
    private final ProductService productService;
    private final TokenService tokenService;
    private final JwtService jwtService;

    public ProductTokenController(OrganizationService organizationService, ProductService productService, TokenService tokenService, JwtService jwtService) {
        this.organizationService = organizationService;
        this.productService = productService;
        this.tokenService = tokenService;
        this.jwtService = jwtService;
    }


    /**
     * Creates a token for the user to use for accessing the backend
     * Token is only shared ones, if not stored on user side it may be lost? ye
     * @param request
     * @param authorizationHeader
     * @return
     */
    @PostMapping("/token/create")
    public ResponseEntity<?> createProductToken(
            @RequestBody ProductCreateTokenRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userUuid = jwtService.getUserUuidFromToken(token);

        if (!organizationService.isMember(request.organizationId(), userUuid)) {
            return new ResponseEntity<>(new StatusResponse(403, "User is not member of the given Organization"), HttpStatus.FORBIDDEN);
        }
        if (!productService.existsByUuid(request.productId())) {
            return new ResponseEntity<>(new StatusResponse(404, "Product not found"), HttpStatus.NOT_FOUND);
        }
        if (!productService.belongsToOrganization(request.productId(), request.organizationId())) {
            return new ResponseEntity<>(new StatusResponse(403, "Product does not belong to the given Organization"), HttpStatus.FORBIDDEN);
        }

        TokenService.TokenPair tokenPair = tokenService.createToken(request.organizationId(), request.productId(), request.name(), request.permissions());
        return new ResponseEntity<>(new ProductTokenResponse(
                request.organizationId(),
                request.productId(),
                tokenPair.id(),
                tokenPair.token()
        ), HttpStatus.CREATED);
    }

    //TODO: Add a way to revoke/delete them
    //TODO: Add a way to reset the token for a tokenId
}
