package net.hauntedstudio.hntdconnect.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    private String uuid;
    private String name;
    private String description;
    private String website;
    private String contactEmail;
    private String ownerUUID;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<OrganizationMembersEntity> members = new HashSet<>();
}