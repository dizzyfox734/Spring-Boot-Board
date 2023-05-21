package dizzyfox734.springbootboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseTimeEntity {

    @Getter
    @CreatedDate
    private LocalDateTime createdDate;

    @Getter
    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @Getter(value = AccessLevel.PROTECTED)
    @Column(columnDefinition = "datetime null default null")
    private LocalDateTime deletedDate;

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }
}
