package com.magictickets.backend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<EventEntity, String> {
}