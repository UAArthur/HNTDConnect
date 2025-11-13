package net.hauntedstudio.hntdconnect.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "organization_members")
public class OrganizationMembersEntity {
    @jakarta.persistence.Id
    private String uuid;
    private String memberUuid;
    private String role;

    @ManyToOne
    @JoinColumn(name = "organizationUuid", referencedColumnName = "uuid",
            foreignKey = @ForeignKey(name = "fk_org_members_org_uuid"))
    private OrganizationEntity organization;
}
