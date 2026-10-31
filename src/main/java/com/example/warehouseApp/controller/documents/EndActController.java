package com.example.warehouseApp.controller.documents;

import com.example.warehouseApp.dto.documents.ReceiptFormDto;
import com.example.warehouseApp.dto.documents.WriteOffFormDto;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.model.DiscrepancyType;
import com.example.warehouseApp.model.DocumentStatus;
import com.example.warehouseApp.repository.InventoryDiscrepancyRepository;
import com.example.warehouseApp.repository.ReceiptDocumentRepository;
import com.example.warehouseApp.repository.WriteOffDocumentRepository;
import com.example.warehouseApp.service.documents.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/end-act")
@RequiredArgsConstructor
public class EndActController {

    private final DocumentService documentService;
    private final InventoryDiscrepancyRepository inventoryDiscrepancyRepository;
    private final WriteOffDocumentRepository writeOffRepo;
    private final ReceiptDocumentRepository receiptDocumentRepository;

    @GetMapping("/write-off/{actId}")
    @PreAuthorize("hasAnyRole('ADMIN','STOREKEEPER')")
    public String showWriteOffForm(@PathVariable Long actId, Model model) {
        var act = inventoryDiscrepancyRepository.findById(actId)
                .orElseThrow(() -> new NotFoundException("Акт не найден"));

        var docOpt = writeOffRepo.findByAct(act);
        if (docOpt.isEmpty()) {
            throw new NotFoundException("Документ на списание не найден. Сначала урегулируйте акт.");
        }
        var doc = docOpt.get();
        boolean hasNoWriteOffLines = act.getLines().stream()
                .noneMatch(l -> DiscrepancyType.WRITE_OFF.equals(l.getType()));

        model.addAttribute("act", act);
        model.addAttribute("doc", doc);
        model.addAttribute("form", new WriteOffFormDto());
        model.addAttribute("hasNoWriteOffLines", hasNoWriteOffLines);
        return "end-act/act-write-off-form";
    }

    @PostMapping("/write-off/{actId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','STOREKEEPER')")
    public String completeWriteOff(
            @PathVariable Long actId,
            @ModelAttribute("form") WriteOffFormDto form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var result = documentService.completeWriteOffByActId(actId, form.getReason());
            redirectAttributes.addFlashAttribute("result", result);
            redirectAttributes.addFlashAttribute("successMessage", "Списание успешно проведено");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return showWriteOffForm(actId, model);
        }

        return "redirect:/end-act/write-off/" + actId;
    }

    @GetMapping("/write-off/{actId}/print")
    public String getWriteOffPrintForm(@PathVariable Long actId, Model model) {
        var act = inventoryDiscrepancyRepository.findById(actId).orElseThrow(() ->
                new NotFoundException(String.format("не найден акт по id %d", actId)));
        var doc = writeOffRepo.findByAct(act).orElseThrow(() ->
                new NotFoundException(String.format("Не найден документ на списание по акту %d", actId)));

        if (!DocumentStatus.COMPLETED.equals(doc.getStatus())) {
            return "redirect:/end-act/write-off/" + actId + "/form";
        }

        var lines = act.getLines();
        var warehouseName = act.getCheck().getWarehouse().getName();

        model.addAttribute("act", act);
        model.addAttribute("doc", doc);
        model.addAttribute("lines", lines);
        model.addAttribute("warehouseName", warehouseName);

        return "end-act/write-off-print";
    }

    @GetMapping("/receipt/{actId}")
    @PreAuthorize("hasAnyRole('ADMIN','STOREKEEPER')")
    public String showReceiptForm(@PathVariable Long actId, Model model) {
        var act = inventoryDiscrepancyRepository.findById(actId)
                .orElseThrow(() -> new NotFoundException("Акт не найден"));

        var docOpt = receiptDocumentRepository.findByAct(act);
        if (docOpt.isEmpty()) {
            throw new NotFoundException("Документ на оприходование не найден. Сначала урегулируйте акт.");
        }
        var doc = docOpt.get();
        var receiptLines = act.getLines().stream()
                .filter(l -> DiscrepancyType.RECEIPT.equals(l.getType()))
                .toList();

        model.addAttribute("act", act);
        model.addAttribute("doc", doc);
        model.addAttribute("form", new ReceiptFormDto());
        model.addAttribute("receiptLines", receiptLines);
        model.addAttribute("hasReceiptLines", !receiptLines.isEmpty());
        return "end-act/act-receipt-form";
    }

    @PostMapping("/receipt/{actId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','STOREKEEPER')")
    public String completeReceipt(
            @PathVariable Long actId,
            @ModelAttribute("form") ReceiptFormDto form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var result = documentService.completeReceiptByActId(actId, form.getSource());
            redirectAttributes.addFlashAttribute("result", result);
            redirectAttributes.addFlashAttribute("successMessage", "Оприходование успешно проведено");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return showReceiptForm(actId, model);
        }

        return "redirect:/end-act/receipt/" + actId;
    }

    @GetMapping("/receipt/{actId}/print")
    public String getReceiptPrintForm(@PathVariable Long actId, Model model) {
        var act = inventoryDiscrepancyRepository.findById(actId).orElseThrow(() ->
                new NotFoundException(String.format("Акт расхождений не найден по id %d", actId)));

        var doc = receiptDocumentRepository.findByAct(act).orElseThrow(() ->
                new NotFoundException(String.format("Документ на оприходование не найден по акту %d", actId)));

        if (!DocumentStatus.COMPLETED.equals(doc.getStatus())) {
            return "redirect:/end-act/receipt/" + actId + "/form";
        }

        var lines = act.getLines();
        var warehouseName = act.getCheck().getWarehouse().getName();

        model.addAttribute("act", act);
        model.addAttribute("doc", doc);
        model.addAttribute("lines", lines);
        model.addAttribute("warehouseName", warehouseName);

        return "end-act/receipt-print";
    }
}