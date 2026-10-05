package com.op.ProductService.service;

import com.op.ProductService.model.ProductRequest;
import com.op.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);
    ProductResponse getProductById(long productId);
    void reduceQuantity(long productId, long quantity);
}
