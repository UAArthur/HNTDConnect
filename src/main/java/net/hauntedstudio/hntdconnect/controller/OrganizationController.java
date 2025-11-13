package net.hauntedstudio.hntdconnect.controller;

import net.hauntedstudio.hntdconnect.dto.StatusResponse;
import net.hauntedstudio.hntdconnect.dto.org.CreateOrgRequest;
import net.hauntedstudio.hntdconnect.dto.org.OrganizationResponse;
import net.hauntedstudio.hntdconnect.entities.OrganizationEntity;
import net.hauntedstudio.hntdconnect.services.JwtService;
import net.hauntedstudio.hntdconnect.services.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final JwtService jwtService;

    public OrganizationController(OrganizationService organizationService, JwtService jwtService) {
        this.organizationService = organizationService;
        this.jwtService = jwtService;
    }


    /**
     * Create a new organization
     *
     * @param createOrgRequest
     * @param authorizationHeader
     * @return
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrganization(
            @RequestBody CreateOrgRequest createOrgRequest,
            @RequestHeader("Authorization") String authorizationHeader) {
        if (createOrgRequest == null) {
            return ResponseEntity.badRequest().build();
        }
        String token = authorizationHeader.replace("Bearer ", "");
        String userUuid = jwtService.getUserUuidFromToken(token);

        OrganizationEntity org = organizationService.createOrganization(createOrgRequest, userUuid);
        OrganizationResponse response = organizationService.toResponse(org);
        return ResponseEntity.ok(response);
    }

    /**
     * Return orgs user is part of
     * if paired with a "@", it will return a list of all orgs he is part of
     * else it will return the specific org he requested, if he is part of it
     *
     * @param orgUuid
     * @param authorizationHeader
     * @return
     */
    @GetMapping("/get/{orgUuid}")
    public ResponseEntity<?> getOrganization(
            @PathVariable("orgUuid") String orgUuid,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userUuid = jwtService.getUserUuidFromToken(token);

        //Return a list of all orgs user is part of
        if (orgUuid.equalsIgnoreCase("@")) {
            var orgs = organizationService.findAllByMemberUuid(userUuid);
            var responses = orgs.stream()
                    .map(organizationService::toResponse)
                    .toList();
            return ResponseEntity.ok(responses);
        }

        //check if he's part of that org
        if (!organizationService.isMember(orgUuid, userUuid)) {
            return new ResponseEntity<>(new StatusResponse(403, "User is not member of the given Organization"), HttpStatus.FORBIDDEN);
        }
        //return only that org
        OrganizationEntity org = organizationService.findByUuid(orgUuid);
        if (org == null) {
            return new ResponseEntity<>(new StatusResponse(404, "Organization not found"), HttpStatus.NOT_FOUND);
        } else {
            OrganizationResponse response = organizationService.toResponse(org);
            return ResponseEntity.ok(response);
        }
    }


}
