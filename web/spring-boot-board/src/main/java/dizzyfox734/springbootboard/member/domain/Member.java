package dizzyfox734.springbootboard.member.domain;

import dizzyfox734.springbootboard.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET deleted_date = NOW() WHERE id = ?")
@Where(clause = "deleted_date is null")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25, unique = true)
    private String username;

    @Column(length = 100)
    private String password;

    @Column(length = 10)
    private String name;

    @Column(length = 50)
    private String email;

    @ManyToMany
    @JoinTable(
            name = "member_authority",
            joinColumns = {@JoinColumn(name = "member_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")}
    )
    private Set<Authority> authorities;

    @Builder
    private Member(String username, String password, String name, String email,
                   Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.authorities = authorities;
    }

    public static Member create(
            String username,
            String encodedPassword,
            String name,
            String email,
            Set<Authority> authorities
    ) {
        validateUsername(username);
        validateEncodedPassword(encodedPassword);
        validateName(name);
        validateEmail(email);
        validateAuthorities(authorities);

        return Member.builder()
                .username(username)
                .password(encodedPassword)
                .name(name)
                .email(email)
                .authorities(authorities)
                .build();
    }

    public void changeEncodedPassword(String encodedPassword) {
        validateEncodedPassword(encodedPassword);
        this.password = encodedPassword;
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }
    }

    private static void validateEncodedPassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
    }

    private static void validateAuthorities(Set<Authority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            throw new IllegalArgumentException("권한은 최소 하나 이상 필요합니다.");
        }
    }
}
