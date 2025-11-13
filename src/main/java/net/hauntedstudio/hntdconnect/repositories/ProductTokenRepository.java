package net.hauntedstudio.hntdconnect.repositories;

import net.hauntedstudio.hntdconnect.entities.ProductTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTokenRepository extends JpaRepository<ProductTokenEntity, String> {

}
