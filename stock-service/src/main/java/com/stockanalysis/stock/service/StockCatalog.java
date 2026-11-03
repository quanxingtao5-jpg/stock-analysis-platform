package com.stockanalysis.stock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockanalysis.stock.api.CreateStockRequest;
import com.stockanalysis.stock.mapper.StockMapper;
import com.stockanalysis.stock.model.Stock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class StockCatalog {
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private final StockMapper mapper;
    private final StringRedisTemplate redis;
    private final RabbitTemplate rabbit;
    private final ObjectMapper json;

    public StockCatalog(StockMapper mapper, StringRedisTemplate redis, RabbitTemplate rabbit, ObjectMapper json) {
        this.mapper = mapper;
        this.redis = redis;
        this.rabbit = rabbit;
        this.json = json;
    }

    public Stock get(long id) {
        String key = "stock:" + id;
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            try {
                return json.readValue(cached, Stock.class);
            } catch (JsonProcessingException e) {
                redis.delete(key);
            }
        }

        Stock stock = mapper.selectById(id);
        if (stock == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found");
        }

        try {
            redis.opsForValue().set(key, json.writeValueAsString(stock), CACHE_TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize stock", e);
        }

        return stock;
    }

    public Stock create(CreateStockRequest request) {
        Stock stock = new Stock();
        stock.setSymbol(request.symbol());
        stock.setName(request.name());
        stock.setPrice(request.price());
        mapper.insert(stock);
        rabbit.convertAndSend("stock.events", "stock.created", stock.getId().toString());
        return stock;
    }
}
