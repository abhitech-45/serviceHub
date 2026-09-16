package com.servicehubai.chat.infrastructure;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import com.servicehubai.chat.domain.AiUsageEntity;

public interface AiUsageRepository extends JpaRepository<AiUsageEntity, java.util.UUID> {
    long countByOccurredAtGreaterThanEqual(Instant since);
    long countBySuccessfulTrue();
    long countByTimedOutTrue();
}
