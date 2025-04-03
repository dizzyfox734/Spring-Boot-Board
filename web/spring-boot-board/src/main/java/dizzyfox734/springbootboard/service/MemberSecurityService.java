package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.member.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component("userDetailsService")
public class MemberSecurityService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public MemberSecurityService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) {
        return memberRepository.findOneWithAuthoritiesByUsername(username)
                .map(member -> createMember(username, member))
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 사용자를 찾을 수 없습니다."));
    }

    private org.springframework.security.core.userdetails.User createMember(String username, Member member) {
        if (!member.isActivated()) {
            throw new RuntimeException(username + " -> 활성화되어 있지 않습니다.");
        }

        List<GrantedAuthority> grantedAuthorities = member.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(member.getUsername(),
                member.getPassword(),
                grantedAuthorities);
    }
}