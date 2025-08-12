package com.ozymandias089.devlog_api.member.entity;

import com.ozymandias089.devlog_api.global.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter
    @Column(nullable = false, unique = false)
    private String password;

    @Getter
    @Column(nullable = false)
    private String username;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(UUID uuid, String email, String password, String username, Role role) {
        this.uuid = uuid;
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }

    public void updateUsername(String newUsername) {
        if ((newUsername == null) || newUsername.isBlank())
            throw new IllegalArgumentException("Username Cannot be empty");
        this.username = newUsername;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
