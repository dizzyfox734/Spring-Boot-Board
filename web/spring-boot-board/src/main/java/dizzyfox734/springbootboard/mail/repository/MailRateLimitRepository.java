package dizzyfox734.springbootboard.mail.repository;

import dizzyfox734.springbootboard.mail.domain.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class MailRateLimitRepository {

    private static final String SIGNUP_COOLDOWN_PREFIX = "mail:signup:cooldown:";
    private static final String PASSWORD_RESET_COOLDOWN_PREFIX = "mail:reset:cooldown:";
    private static final String SIGNUP_IP_PREFIX = "mail:signup:ip:";
    private static final String PASSWORD_RESET_IP_PREFIX = "mail:reset:ip:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MailProperties mailProperties;

    public void saveSignupEmailCooldown(String email) {
        saveSignupEmailCooldown(email, Duration.ofSeconds(mailProperties.getCooldownSeconds()));
    }

    public void saveSignupEmailCooldown(String email, Duration ttl) {
        saveCooldown(generateSignupCooldownKey(email), ttl);
    }

    public void savePasswordResetEmailCooldown(String email) {
        savePasswordResetEmailCooldown(email, Duration.ofSeconds(mailProperties.getCooldownSeconds()));
    }

    public void savePasswordResetEmailCooldown(String email, Duration ttl) {
        saveCooldown(generatePasswordResetCooldownKey(email), ttl);
    }

    public void removeSignupEmailCooldown(String email) {
        stringRedisTemplate.delete(generateSignupCooldownKey(email));
    }

    public void removePasswordResetEmailCooldown(String email) {
        stringRedisTemplate.delete(generatePasswordResetCooldownKey(email));
    }

    public boolean existsSignupEmailCooldown(String email) {
        boolean result = stringRedisTemplate.hasKey(generateSignupCooldownKey(email));
        return Boolean.TRUE.equals(result);
    }

    public boolean existsPasswordResetEmailCooldown(String email) {
        boolean result = stringRedisTemplate.hasKey(generatePasswordResetCooldownKey(email));
        return Boolean.TRUE.equals(result);
    }

    public long incrementSignupIpRequestCount(String ip) {
        return incrementIpRequestCount(generateSignupIpKey(ip));
    }

    public long incrementPasswordResetIpRequestCount(String ip) {
        return incrementIpRequestCount(generatePasswordResetIpKey(ip));
    }

    private void saveCooldown(String key, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, "1", ttl);
    }

    private long incrementIpRequestCount(String key) {
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(mailProperties.getIpLimitWindowSeconds()));
        }

        return count != null ? count : 0L;
    }

    private String generateSignupCooldownKey(String email) {
        return SIGNUP_COOLDOWN_PREFIX + email;
    }

    private String generatePasswordResetCooldownKey(String email) {
        return PASSWORD_RESET_COOLDOWN_PREFIX + email;
    }

    private String generateSignupIpKey(String ip) {
        return SIGNUP_IP_PREFIX + ip;
    }

    private String generatePasswordResetIpKey(String ip) {
        return PASSWORD_RESET_IP_PREFIX + ip;
    }
}
