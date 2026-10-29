package com.example.warehouseApp.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CurrentUserProvider {

    private final Long currentUserId;

    public CurrentUserProvider() {
        this.currentUserId = 1L;
    }

    @Autowired
    public CurrentUserProvider(@Value("${warehouse.default-user-id:1}") Long defaultId) {
        this.currentUserId = defaultId;
    }

}
