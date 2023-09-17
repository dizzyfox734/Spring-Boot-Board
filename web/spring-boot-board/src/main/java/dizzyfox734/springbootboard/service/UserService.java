package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.controller.dto.UserModifyDto;
import dizzyfox734.springbootboard.domain.user.Authority;
import dizzyfox734.springbootboard.domain.user.User;
import dizzyfox734.springbootboard.domain.user.UserRepository;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(SignupDto signupDto) {

        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();

        User user = User.builder()
                .username(signupDto.getUsername())
                .password(passwordEncoder.encode(signupDto.getPassword1()))
                .name(signupDto.getName())
                .email(signupDto.getEmail())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        this.userRepository.save(user);

        return user;
    }

    @Transactional
    public User modify(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));

        this.userRepository.save(user);

        return user;
    }

    public boolean validateDuplicateUser(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username).isPresent();
    }

    public boolean validateDuplicateEmail(String email) {
        return userRepository.findOneWithAuthoritiesByEmail(email).isPresent();
    }

    public User getUser(String username) {
        Optional<User> user = this.userRepository.findOneWithAuthoritiesByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new DataNotFoundException("user not found");
        }
    }
}