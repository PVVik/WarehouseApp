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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        Long entityId = extractEntityId(result);
        if (entityId == null) {
            log.debug("Не удалось извлечь entityId для логирования: {}", joinPoint.getSignature().toShortString());
            return result;
        }

        AuditLog log = new AuditLog();
        log.setUserId(authContextService.getCurrentUserId());
        log.setUsername(authContextService.getCurrentUsername());
        log.setAction(logAudit.action());
        log.setEntityType(logAudit.entityType().getSimpleName());
        log.setEntityId(entityId);
        log.setTimestamp(LocalDateTime.now());

        String details = extractDetailsSafe(joinPoint, result);
        log.setDetails(details);

        auditLogRepository.save(log);
        return result;
    }

    private Long extractEntityId(Object result) {
        if (result instanceof EntityWithId entity) {
            return entity.getId();
        }
        return null;
    }

    private String extractDetailsSafe(ProceedingJoinPoint joinPoint, Object result) {
        Map<String, Object> data = new HashMap<>();
        data.put("method", joinPoint.getSignature().toShortString());

        List<Object> argsSummary = new ArrayList<>();
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null) {
                argsSummary.add(null);
            } else if (arg instanceof byte[] || arg instanceof InputStream) {
                argsSummary.add("byte[]/InputStream (skipped)");
            } else if (arg instanceof EntityWithId entity) {
                argsSummary.add(Map.of(
                        "type", entity.getClass().getSimpleName(),
                        "id", entity.getId()
                ));
            } else {
                argsSummary.add(Map.of(
                        "type", arg.getClass().getSimpleName(),
                        "value", arg.toString()
                ));
            }
        }
        data.put("args", argsSummary);

        if (result != null) {
            if (result instanceof EntityWithId entity) {
                data.put("result", Map.of(
                        "type", entity.getClass().getSimpleName(),
                        "id", entity.getId()
                ));
            } else {
                data.put("result", Map.of(
                        "type", result.getClass().getSimpleName(),
                        "value", result.toString()
                ));
            }
        } else {
            data.put("result", null);
        }

        return JsonUtils.toJson(data);
    }
}
