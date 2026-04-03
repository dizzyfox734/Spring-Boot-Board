package dizzyfox734.springbootboard.member.repository;

import dizzyfox734.springbootboard.member.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
}