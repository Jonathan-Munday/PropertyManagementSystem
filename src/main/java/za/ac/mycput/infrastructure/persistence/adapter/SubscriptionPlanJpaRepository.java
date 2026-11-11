package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.SubscriptionPlanJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanJpaRepository extends JpaRepository<SubscriptionPlanJpaEntity, UUID> {

    Optional<SubscriptionPlanJpaEntity> findByCode(String code);

    List<SubscriptionPlanJpaEntity> findAllByOrderByAmountAsc();
}
