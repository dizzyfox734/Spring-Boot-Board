package dizzyfox734.springbootboard.member.repository;

import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryJpaIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("findOneWithAuthoritiesByUsername 는 authorities 를 함께 조회한다")
    void shouldFetchAuthoritiesWhenFindingMemberByUsername() {
        createAndSaveMemberWithAuthorities();

        Optional<Member> result = memberRepository.findOneWithAuthoritiesByUsername("testuser");

        assertThat(result).isPresent();
        assertMemberWithAuthorities(result.get());
    }

    @Test
    @DisplayName("존재하지 않는 username 으로 조회하면 빈 결과를 반환한다")
    void shouldReturnEmptyWhenUsernameDoesNotExistOnFindOneWithAuthoritiesByUsername() {
        Optional<Member> result = memberRepository.findOneWithAuthoritiesByUsername("not-exists");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findOneWithAuthoritiesByEmail 은 authorities 를 함께 조회한다")
    void shouldFetchAuthoritiesWhenFindingMemberByEmail() {
        createAndSaveMemberWithAuthorities();

        Optional<Member> result = memberRepository.findOneWithAuthoritiesByEmail("test@example.com");

        assertThat(result).isPresent();
        assertMemberWithAuthorities(result.get());
    }

    @Test
    @DisplayName("존재하지 않는 email 로 조회하면 빈 결과를 반환한다")
    void shouldReturnEmptyWhenEmailDoesNotExistOnFindOneWithAuthoritiesByEmail() {
        Optional<Member> result = memberRepository.findOneWithAuthoritiesByEmail("not-exists@example.com");

        assertThat(result).isEmpty();
    }

    private void createAndSaveMemberWithAuthorities() {
        Authority roleUser = authorityRepository.findById("ROLE_USER")
                .orElseThrow(() -> new AssertionError("ROLE_USER 권한이 존재해야 합니다."));
        Authority roleAdmin = authorityRepository.findById("ROLE_ADMIN")
                .orElseThrow(() -> new AssertionError("ROLE_ADMIN 권한이 존재해야 합니다."));

        Member member = Member.create(
                "testuser",
                "encodedPassword",
                "홍길동",
                "test@example.com",
                Set.of(roleUser, roleAdmin)
        );

        entityManager.persist(member);
        entityManager.flush();
        entityManager.clear();
    }

    private void assertMemberWithAuthorities(Member member) {
        assertThat(member.getUsername()).isEqualTo("testuser");
        assertThat(member.getAuthorities()).hasSize(2);
        assertThat(member.getAuthorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
