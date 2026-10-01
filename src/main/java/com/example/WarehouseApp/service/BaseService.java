package com.example.WarehouseApp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BaseService<T> {

    T create(T t);
    T update(T t);
    T getById(long id);
    Page<T> getAll(Pageable pageable, String search);
    void delete(long id);
}
