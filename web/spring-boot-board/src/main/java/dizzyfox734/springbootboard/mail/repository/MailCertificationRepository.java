package dizzyfox734.springbootboard.mail.repository;

import dizzyfox734.springbootboard.mail.domain.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Repository
public class MailCertificationRepository {

    private static final String PREFIX = "mail:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MailProperties mailProperties;

    public void save(String email, String certificationCode) {
        save(email, certificationCode, Duration.ofSeconds(mailProperties.getCertificationExpirationSeconds()));
    }

    public void save(String email, String certificationCode, Duration expiration) {
        stringRedisTemplate.opsForValue().set(
                generateKey(email),
                certificationCode,
                expiration
        );
    }

    public String get(String email) {
        return stringRedisTemplate.opsForValue().get(generateKey(email));
    }

    public Duration getExpiration(String email) {
        Long seconds = stringRedisTemplate.getExpire(generateKey(email), TimeUnit.SECONDS);

        if (seconds == null || seconds <= 0) {
            return null;
        }

        return Duration.ofSeconds(seconds);
    }

    public void remove(String email) {
        stringRedisTemplate.delete(generateKey(email));
    }

    public boolean exists(String email) {
        Boolean result = stringRedisTemplate.hasKey(generateKey(email));
        return Boolean.TRUE.equals(result);
    }

    private String generateKey(String email) {
        return PREFIX + email;
    }
}
