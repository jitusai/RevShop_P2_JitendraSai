package com.rev.app.rest;

import com.rev.app.dto.OrderDTO;
import com.rev.app.entity.enums.OrderStatus;
import com.rev.app.service.IOrderService;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final IOrderService IOrderService;
    private final IUserService IUserService;

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return IOrderService.findAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return IOrderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<OrderDTO> getOrdersByUserId(@PathVariable Long userId) {
        return IOrderService.findByBuyerId(userId);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        try {
            OrderDTO updated = IOrderService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
