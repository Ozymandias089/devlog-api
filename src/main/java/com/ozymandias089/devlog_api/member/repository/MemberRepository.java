package com.ozymandias089.devlog_api.member.repository;

import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByEmail(String email);
    Optional<MemberEntity> findByUuid(UUID uuid);
    Optional<MemberEntity> findByUsername(String Username);
    boolean existsByEmail(String email);
}
