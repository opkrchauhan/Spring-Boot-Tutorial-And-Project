package com.opkrchauhan.OrderService.service;

import com.opkrchauhan.OrderService.entity.Order;
import com.opkrchauhan.OrderService.exception.CustomException;
import com.opkrchauhan.OrderService.external.client.PaymentService;
import com.opkrchauhan.OrderService.external.client.ProductService;
import com.opkrchauhan.OrderService.external.request.PaymentRequest;
import com.opkrchauhan.OrderService.external.response.PaymentResponse;
import com.opkrchauhan.OrderService.model.OrderRequest;
import com.opkrchauhan.OrderService.model.OrderResponse;
import com.opkrchauhan.OrderService.model.PaymentMode;
import com.opkrchauhan.OrderService.model.ProductResponse;
import com.opkrchauhan.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {

        log.info("Placing Order Request: {}", orderRequest);

        // 1. Reduce product quantity
        productService.reduceQuantity(
                orderRequest.getProductId(),
                orderRequest.getQuantity()
        );

        // 2. Create Order
        log.info("Creating Order with status CREATED");

        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        // 3. SAVE ORDER FIRST
        order = orderRepository.save(order);

        log.info("Order created with ID: {}", order.getId());

        // 4. Now create PaymentRequest
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())   // ✅ Now contains actual ID
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus;

        // 5. Process Payment
        try {

            log.info("Calling Payment Service for Order ID: {}", order.getId());

            paymentService.doPayment(paymentRequest);

            log.info("Payment completed successfully. Changing Order status to PLACED");

            orderStatus = "PLACED";

        } catch (Exception ex) {

            log.error("Error occurred in payment", ex);

            orderStatus = "FAILED";
        }

        // 6. Update Order status
        order.setOrderStatus(orderStatus);

        order = orderRepository.save(order);

        log.info("Order placed successfully with Order ID: {}", order.getId());

        return order.getId();
    }
    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for Order Id {}", orderId);

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException("Ordr not found for the order Id: " + orderId,
                "NOT_FOUND", 404));

        log.info("Invoking Product service to fetch the product id: {}", order.getProductId());

        ProductResponse productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(), ProductResponse.class);

        log.info("Getting Payment Information from the payment service");

        PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(), PaymentResponse.class);

        OrderResponse.ProductDetails productDetails =
                OrderResponse.ProductDetails.builder()
                        .productName(productResponse.getProductName())
                        .productId(productResponse.getProductId())
                        .build();

        OrderResponse.PaymentDetails paymentDetails =
                OrderResponse.PaymentDetails.builder()
                        .paymentId(paymentResponse.getPaymentId())
                        .paymentStatus(paymentResponse.getStatus())
                        .paymentDate(paymentResponse.getPaymentDate())
                        .paymentMode(paymentResponse.getPaymentMode())
                        .build();


        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
        return orderResponse;
    }
}