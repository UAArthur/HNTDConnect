package net.hauntedstudio.hntdconnect.repositories;

import net.hauntedstudio.hntdconnect.entities.OrganizationEntity;
import net.hauntedstudio.hntdconnect.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {
    @Query("SELECT o FROM OrganizationEntity o LEFT JOIN FETCH o.members WHERE o.uuid = :uuid")
    Optional<OrganizationEntity> findByUuidWithMembers(@Param("uuid") String uuid);

    @Query("SELECT DISTINCT o FROM OrganizationEntity o LEFT JOIN FETCH o.members m WHERE m.uuid = :userUuid")
    java.util.List<OrganizationEntity> findAllByMemberUuid(@Param("userUuid") String userUuid);

    boolean existsByUuid(String uuid);
}
