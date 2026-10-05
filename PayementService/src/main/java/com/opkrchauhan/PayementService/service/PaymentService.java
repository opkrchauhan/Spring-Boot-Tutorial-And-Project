package com.opkrchauhan.PayementService.service;

import com.opkrchauhan.PayementService.model.PaymentRequest;
import com.opkrchauhan.PayementService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);
    PaymentResponse getPaymentDetailsByOrderId(long orderId);
}
