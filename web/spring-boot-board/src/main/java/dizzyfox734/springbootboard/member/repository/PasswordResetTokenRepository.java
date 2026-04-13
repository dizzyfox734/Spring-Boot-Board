package dizzyfox734.springbootboard.member.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Repository
public class PasswordResetTokenRepository {

    private static final String TOKEN_PREFIX = "pwd-reset:token:";
    private static final String USER_PREFIX = "pwd-reset:user:";

    private final StringRedisTemplate stringRedisTemplate;

    public PasswordResetTokenRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void save(String username, String token, Duration expiration) {
        String previousToken = getTokenByUsername(username);
        if (previousToken != null) {
            stringRedisTemplate.delete(generateTokenKey(previousToken));
        }

        stringRedisTemplate.opsForValue().set(generateTokenKey(token), username, expiration);
        stringRedisTemplate.opsForValue().set(generateUserKey(username), token, expiration);
    }

    public String getUsernameByToken(String token) {
        return stringRedisTemplate.opsForValue().get(generateTokenKey(token));
    }

    public String getTokenByUsername(String username) {
        return stringRedisTemplate.opsForValue().get(generateUserKey(username));
    }

    public Duration getExpirationByUsername(String username) {
        Long seconds = stringRedisTemplate.getExpire(generateUserKey(username), TimeUnit.SECONDS);
        if (seconds == null || seconds <= 0) {
            return null;
        }

        return Duration.ofSeconds(seconds);
    }

    public void removeByToken(String token) {
        String username = getUsernameByToken(token);
        if (username != null) {
            stringRedisTemplate.delete(generateUserKey(username));
        }

        stringRedisTemplate.delete(generateTokenKey(token));
    }

    private String generateTokenKey(String token) {
        return TOKEN_PREFIX + token;
    }

    private String generateUserKey(String username) {
        return USER_PREFIX + username;
    }
}
