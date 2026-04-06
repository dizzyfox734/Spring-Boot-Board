package dizzyfox734.springbootboard.member.service;

import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.repository.MemberRepository;
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
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 회원를 찾을 수 없습니다."));
    }

    private org.springframework.security.core.userdetails.User createMember(String username, Member member) {
        List<GrantedAuthority> grantedAuthorities = member.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(member.getUsername(),
                member.getPassword(),
                grantedAuthorities);
    }
}