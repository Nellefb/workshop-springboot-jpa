package com.ellen.store_web_services.repositories;

import com.ellen.store_web_services.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
