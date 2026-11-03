package com.stockanalysis.stock.api;

import com.stockanalysis.common.ApiResponse;
import com.stockanalysis.stock.model.Stock;
import com.stockanalysis.stock.service.StockCatalog;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final StockCatalog catalog;

    public StockController(StockCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/{id}")
    public ApiResponse<Stock> get(@PathVariable long id) {
        return ApiResponse.ok(catalog.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Stock> create(@Valid @RequestBody CreateStockRequest request) {
        return ApiResponse.ok(catalog.create(request));
    }
}
