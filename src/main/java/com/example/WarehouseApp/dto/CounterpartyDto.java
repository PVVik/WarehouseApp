package com.example.WarehouseApp.dto;

import com.example.WarehouseApp.model.CounterpartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounterpartyDto implements Comparable<CounterpartyDto> {

    private Long id;

    @NotBlank(message = "Название организации обязательно")
    private String name;

    @NotBlank(message = "ИНН организации обязателен")
    @Pattern(regexp = "^\\d{10}|\\d{12}$", message = "ИНН должен содержать только цифры и быть длиной 10 или 12 символов")
    private String inn;

    @NotBlank(message = "КПП организации обязателен")
    @Pattern(regexp = "^\\d{9}$", message = "КПП должен содержать ровно 9 цифр")
    private String kpp;

    @NotNull(message = "Тип организации обязателен")
    private CounterpartyType counterpartyType;

    @Pattern(regexp = "^(?:\\+?7|8)?[\\s\\-()]*\\d{10}$", message = "Неверный формат номера телефона")
    @NotBlank(message = "Название организации обязательно")
    private String phone;

    @NotBlank(message = "Название организации обязательно")
    private String contact;

    @NotNull(message = "Название организации обязательно")
    private Boolean isActive;

    private String counterpartyTypeLabel;

    @Override
    public int compareTo(CounterpartyDto o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}
