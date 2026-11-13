package com.pep.toolshare.member;

import com.pep.toolshare.db.ConnectionFactory;
import com.pep.toolshare.domain.Member;

import java.util.Objects;
import java.util.Optional;

/** JDBC implementation completed by the learner. */
public final class JdbcMemberRepository implements MemberRepository {
    private final ConnectionFactory connections;

    public JdbcMemberRepository(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    @Override
    public long create(Member member) {
        Objects.requireNonNull(member, "member");
        // TODO LEARNER 3A: INSERT with a PreparedStatement and return the generated id.
        return 0L;
    }

    @Override
    public Optional<Member> findById(long id) {
        // TODO LEARNER 3B: SELECT by id and map the ResultSet row to Member.
        return Optional.empty();
    }

    @Override
    public boolean updateEmail(long id, String email) {
        // TODO LEARNER 3C: UPDATE with bound parameters and report whether one row changed.
        return false;
    }

    @Override
    public boolean deleteById(long id) {
        // TODO LEARNER 3D: DELETE with a bound id and report whether one row changed.
        return false;
    }
}
