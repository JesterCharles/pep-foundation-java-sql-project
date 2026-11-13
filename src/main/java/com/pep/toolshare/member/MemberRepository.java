package com.pep.toolshare.member;

import com.pep.toolshare.domain.Member;

import java.util.Optional;

/** CRUD operations for tool-library members. */
public interface MemberRepository {
    long create(Member member);

    Optional<Member> findById(long id);

    boolean updateEmail(long id, String email);

    boolean deleteById(long id);
}
