package dizzyfox734.springbootboard.controller.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class MailCertificationDao {

    private final String PREFIX = "mail: ";
    private final int LIMIT_TIME = 60 * 3;

    private final StringRedisTemplate stringRedisTemplate;

    public void createMailCertification(String mail, String checkcode) {
        stringRedisTemplate.opsForValue()
                .set(PREFIX + mail, checkcode, Duration.ofSeconds(LIMIT_TIME));
    }

    public String getMailCertification(String mail) {
        return stringRedisTemplate.opsForValue().get(PREFIX + mail);
    }

    public void removeMailCertification(String mail) {
        stringRedisTemplate.delete(PREFIX + mail);
    }

    public boolean hasKey(String mail) {
        return stringRedisTemplate.hasKey(PREFIX + mail);
    }
}
