package com.ozymandias089.devlog_api.member.repository;

import com.ozymandias089.devlog_api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUuid(UUID uuid);
    Optional<Member> findByUsername(String Username);
}
