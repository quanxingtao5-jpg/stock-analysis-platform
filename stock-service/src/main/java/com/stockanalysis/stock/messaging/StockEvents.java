package com.stockanalysis.stock.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockEvents {
    @Bean
    DirectExchange stockExchange() {
        return new DirectExchange("stock.events", true, false);
    }

    @Bean
    Queue stockCreatedQueue() {
        return new Queue("stock.created", true);
    }

    @Bean
    Binding stockCreatedBinding(Queue stockCreatedQueue, DirectExchange stockExchange) {
        return BindingBuilder.bind(stockCreatedQueue).to(stockExchange).with("stock.created");
    }
}
