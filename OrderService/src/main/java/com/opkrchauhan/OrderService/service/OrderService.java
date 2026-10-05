package com.opkrchauhan.OrderService.service;

import com.opkrchauhan.OrderService.model.OrderRequest;
import com.opkrchauhan.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
    OrderResponse getOrderDetails(long orderId);
}
