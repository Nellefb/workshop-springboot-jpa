package com.ellen.store_web_services.repositories;

import com.ellen.store_web_services.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
