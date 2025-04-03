package dizzyfox734.springbootboard.domain.member;

import dizzyfox734.springbootboard.domain.BaseTimeEntity;
import lombok.*;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    private boolean activated;

    @ManyToMany
    @JoinTable(
            name = "member_authority",
            joinColumns = {@JoinColumn(name = "member_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")})
    private Set<Authority> authorities;

}