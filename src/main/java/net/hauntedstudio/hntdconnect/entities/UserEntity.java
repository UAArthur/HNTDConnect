package net.hauntedstudio.hntdconnect.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
//@Table(name = "users")
public class UserEntity {
    @Id
    private String uuid;
    private String email;
    private String username;
    private String password;
}