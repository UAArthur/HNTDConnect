package net.hauntedstudio.hntdconnect.repositories;

import net.hauntedstudio.hntdconnect.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    Optional<ProductEntity> findByUuid(String uuid);
    List<ProductEntity> findByOrganization_Uuid(String organizationUuid);
    boolean existsByUuid(String uuid);
    boolean existsByUuidAndOrganization_Uuid(String uuid, String organizationUuid);
}
