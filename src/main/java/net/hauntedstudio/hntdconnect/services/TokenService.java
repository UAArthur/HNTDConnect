package net.hauntedstudio.hntdconnect.services;

import net.hauntedstudio.hntdconnect.entities.OrganizationEntity;
import net.hauntedstudio.hntdconnect.entities.ProductEntity;
import net.hauntedstudio.hntdconnect.entities.ProductTokenEntity;
import net.hauntedstudio.hntdconnect.repositories.OrganizationRepository;
import net.hauntedstudio.hntdconnect.repositories.ProductRepository;
import net.hauntedstudio.hntdconnect.repositories.ProductTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class TokenService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProductTokenRepository productTokenRepository;
    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;

    @Value("${token.secret}")
    private byte[] hmacKey;

    public TokenService(ProductTokenRepository productTokenRepository,
                        ProductRepository productRepository,
                        OrganizationRepository organizationRepository) {
        this.productTokenRepository = productTokenRepository;
        this.productRepository = productRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public TokenPair createToken(String organizationId, String productId, String name, long permissions) {
        String tokenId = UUID.randomUUID().toString();
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String hmac = hmacSha256(token);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        OrganizationEntity organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        ProductTokenEntity productToken = new ProductTokenEntity(
                UUID.randomUUID().toString(),
                tokenId,
                hmac,
                name,
                Long.valueOf(permissions),
                product,
                organization
        );
        productTokenRepository.save(productToken);

        return new TokenPair(tokenId, organizationId, name, token, hmac);
    }

    public void deleteToken(String tokenId, String token) {
        TokenPair existing = findById(tokenId);
        if (existing == null) {
            throw new IllegalArgumentException("Token not found: " + tokenId);
        }
        String computedHmac = hmacSha256(token);
        if (!constantTimeEquals(existing.hmac, computedHmac)) {
            throw new IllegalArgumentException("Invalid token provided for deletion");
        }
        productTokenRepository.deleteById(existing.id);
    }

    public TokenPair updateTokenById(String organizationId, String tokenId) {
        TokenPair existing = findById(tokenId);
        if (existing == null) {
            throw new IllegalArgumentException("Token not found: " + tokenId);
        }
        deleteToken(existing.id, existing.hmac);
        return createToken(organizationId, existing.id, existing.name, 0);
    }

    public TokenPair findById(String tokenId) {
        ProductTokenEntity entity = productTokenRepository.findByTokenId(tokenId);
        if (entity == null) {
            return null;
        }
        return new TokenPair(entity.getTokenId(), entity.getOrganization().getUuid(), entity.getName(), null, entity.getTokenHmac());
    }

    private String hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
            byte[] out = mac.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] y = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int result = 0;
        for (int i = 0; i < x.length; i++) result |= x[i] ^ y[i];
        return result == 0;
    }

    public record TokenPair(String id, String organizationId, String name, String token, String hmac) { }
}
