package com.rev.app.service;

import com.rev.app.entity.Payment;

import java.util.Optional;

public interface PaymentService {

    Payment processPayment(Payment payment);

    Optional<Payment> findByOrderId(Long orderId);
}