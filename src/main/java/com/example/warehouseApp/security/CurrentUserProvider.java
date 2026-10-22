package com.example.warehouseApp.security;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CurrentUserProvider {

    private final Long currentUserId = 1L;

}
