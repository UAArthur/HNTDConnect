package net.hauntedstudio.hntdconnect.repositories;

import net.hauntedstudio.hntdconnect.entities.OrganizationEntity;
import net.hauntedstudio.hntdconnect.entities.OrganizationMembersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMembersRepository extends JpaRepository<OrganizationMembersEntity, String> {
    List<OrganizationMembersEntity> findAllByOrganization(OrganizationEntity organization);

    Optional<OrganizationMembersEntity> findByOrganizationAndMemberUuid(OrganizationEntity organization, String memberUuid);

    List<OrganizationMembersEntity> findAllByOrganization_Uuid(String organizationUuid);

    Optional<OrganizationMembersEntity> findByOrganization_UuidAndMemberUuid(String organizationUuid, String memberUuid);
}
