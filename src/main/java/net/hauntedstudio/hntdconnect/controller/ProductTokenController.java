package net.hauntedstudio.hntdconnect.controller;

import net.hauntedstudio.hntdconnect.dto.StatusResponse;
import net.hauntedstudio.hntdconnect.dto.prdct.token.ProductCreateTokenRequest;
import net.hauntedstudio.hntdconnect.dto.prdct.token.ProductTokenResponse;
import net.hauntedstudio.hntdconnect.services.*;
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

    public ProductTokenController(OrganizationService organizationService, UserService userService, ProductService productService, TokenService tokenService, JwtService jwtService) {
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

    //TODO: Add a way to check if the user has permissions to reset the token
    /**
     * Delete a token by its ID
     * Needs to be part of the organization the token belongs to
     * @param tokenId
     * @param authorizationHeader
     * @return
     */
    @DeleteMapping("/token/delete/{tokenId}")
    public ResponseEntity<?> deleteProductToken(
            @PathVariable String tokenId,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userUuid = jwtService.getUserUuidFromToken(token);
        TokenService.TokenPair tokenPair = tokenService.findById(tokenId);
        if (tokenPair == null) {
            return new ResponseEntity<>(new StatusResponse(404, "Token not found"), HttpStatus.NOT_FOUND);
        }
        if (!organizationService.isMember(tokenPair.organizationId(), userUuid)) {
            return new ResponseEntity<>(new StatusResponse(403, "User is not member of the given Organization"), HttpStatus.FORBIDDEN);
        }
        tokenService.deleteToken(tokenId, tokenPair.token());
        return new ResponseEntity<>(new StatusResponse(200, "Token deleted successfully"), HttpStatus.OK);
    }

    //TODO: Add a way to check if the user has permissions to reset the token
    /**
     * Reset a token by its ID
     * Needs to be part of the organization the token belongs to
     * Used when a token is lost or compromised
     * @param tokenId
     * @param authorizationHeader
     * @return
     */
    @PostMapping("/token/reset/{tokenId}")
    public ResponseEntity<?> resetProductToken(
            @PathVariable String tokenId,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userUuid = jwtService.getUserUuidFromToken(token);
        TokenService.TokenPair tokenPair = tokenService.findById(tokenId);
        if (tokenPair == null) {
            return new ResponseEntity<>(new StatusResponse(404, "Token not found"), HttpStatus.NOT_FOUND);
        }
        if (!organizationService.isMember(tokenPair.organizationId(), userUuid)) {
            return new ResponseEntity<>(new StatusResponse(403, "User is not member of the given Organization"), HttpStatus.FORBIDDEN);
        }

        TokenService.TokenPair newToken = tokenService.updateTokenById(tokenPair.organizationId(), tokenPair.hmac());
        return new ResponseEntity<>(new ProductTokenResponse(
                tokenPair.organizationId(),
                tokenPair.id(),
                newToken.id(),
                newToken.token()
        ), HttpStatus.OK);
    }

}
