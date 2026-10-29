package com.example.warehouseApp.domain;

import com.example.warehouseApp.model.EntityWithId;
import com.example.warehouseApp.repository.AuditLogRepository;
import com.example.warehouseApp.service.domain.AuthContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.flywaydb.core.internal.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final AuthContextService authContextService;

    @Around("@annotation(logAudit)")
    public Object logAround(ProceedingJoinPoint joinPoint, LogAudit logAudit) throws Throwable {
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error("Ошибка при выполнении метода с логированием", e);
            throw e;
        }

        Long entityId = extractEntityId(result, joinPoint);
        if (entityId == null) {
            log.warn("Не удалось извлечь entityId для логирования: {}", joinPoint.getSignature().toShortString());
            return result;
        }

        AuditLog log = new AuditLog();
        log.setUserId(authContextService.getCurrentUserId());
        log.setUsername(authContextService.getCurrentUsername());
        log.setAction(logAudit.action());
        log.setEntityType(logAudit.entityType().getSimpleName());
        log.setEntityId(entityId);
        log.setTimestamp(LocalDateTime.now());

        String details = extractDetails(joinPoint, result);
        log.setDetails(details);

        auditLogRepository.save(log);
        return result;
    }

    private Long extractEntityId(Object result, ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        if (result instanceof EntityWithId) {
            return ((EntityWithId) result).getId();
        }
        if (result != null && result.getClass().getDeclaredMethod("getId") != null) {
            try {
                return (Long) result.getClass().getMethod("getId").invoke(result);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private String extractDetails(ProceedingJoinPoint joinPoint, Object result) {
        Map<String, Object> data = new HashMap<>();
        data.put("method", joinPoint.getSignature().toShortString());
        data.put("args", Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof byte[] || arg instanceof InputStream))
                .toList());
        data.put("result", result);

        return JsonUtils.toJson(data);
    }
}
