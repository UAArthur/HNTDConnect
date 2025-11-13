package net.hauntedstudio.hntdconnect.services;

import net.hauntedstudio.hntdconnect.dto.org.CreateOrgRequest;
import net.hauntedstudio.hntdconnect.dto.org.OrganizationResponse;
import net.hauntedstudio.hntdconnect.entities.OrganizationEntity;
import net.hauntedstudio.hntdconnect.entities.OrganizationMembersEntity;
import net.hauntedstudio.hntdconnect.entities.UserEntity;
import net.hauntedstudio.hntdconnect.repositories.OrganizationMembersRepository;
import net.hauntedstudio.hntdconnect.repositories.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMembersRepository organizationMembersRepository;
    private final UserService userService;

    public OrganizationService(OrganizationRepository organizationRepository, OrganizationMembersRepository organizationMembersRepository, UserService userService) {
        this.organizationRepository = organizationRepository;
        this.organizationMembersRepository = organizationMembersRepository;
        this.userService = userService;
    }

    public OrganizationEntity createOrganization(CreateOrgRequest request, String userUuid) {
        OrganizationEntity org = new OrganizationEntity();
        org.setUuid(UUID.randomUUID().toString());
        org.setName(request.name());
        org.setDescription("");
        org.setWebsite("");
        org.setContactEmail("");
        org.setOwnerUUID(userUuid);

        OrganizationMembersEntity orgMembersEntity = new OrganizationMembersEntity();
        orgMembersEntity.setUuid(UUID.randomUUID().toString());
        orgMembersEntity.setOrganization(org);
        orgMembersEntity.setMemberUuid(userUuid);
        orgMembersEntity.setRole("owner");
        org.getMembers().add(orgMembersEntity);

        return organizationRepository.save(org);
    }

    public OrganizationEntity findByUuid(String uuid) {
        return organizationRepository.findByUuidWithMembers(uuid)
                .orElse(null);
    }

    public java.util.List<OrganizationEntity> findAllByMemberUuid(String userUuid) {
        return organizationRepository.findAllByMemberUuid(userUuid);
    }

    public boolean isMember(String orgUuid, String userUuid) {
        UserEntity user = userService.findByUuid(userUuid).orElse(null);
        if (user == null) return false;
        if (!organizationRepository.existsByUuid(orgUuid)) return false;
        return organizationMembersRepository.findByOrganization_UuidAndMemberUuid(orgUuid, userUuid).isPresent();
    }

    public OrganizationResponse toResponse(OrganizationEntity org) {
        Set<String> memberUUIDs = org.getMembers().stream()
                .map(OrganizationMembersEntity::getMemberUuid)
                .collect(Collectors.toSet());
        return new OrganizationResponse(
                org.getUuid(),
                org.getName(),
                org.getDescription(),
                org.getWebsite(),
                org.getContactEmail(),
                org.getOwnerUUID(),
                memberUUIDs
        );
    }

}
