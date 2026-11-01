package com.example.warehouseApp.controller.documents;

import com.example.warehouseApp.dto.documents.ReceiptDocumentResultDto;
import com.example.warehouseApp.dto.documents.WriteOffDocumentResultDto;
import com.example.warehouseApp.service.documents.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/end-act")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
public class EndActApiController {

    private final DocumentService documentService;

    @PostMapping("/write-off/{actId}/complete")
    public ResponseEntity<WriteOffDocumentResultDto> completeWriteOffByAct(@PathVariable Long actId,
                                                                           @RequestParam String reason) {
        var result = documentService.completeWriteOffByActId(actId, reason);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/receipt/{actId}/complete")
    public ResponseEntity<ReceiptDocumentResultDto> completeReceiptByAct(
            @PathVariable Long actId,
            @RequestParam String source) {

        var result = documentService.completeReceiptByActId(actId, source);
        return ResponseEntity.ok(result);
    }
}
