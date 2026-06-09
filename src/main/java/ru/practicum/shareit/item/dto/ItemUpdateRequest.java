package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ItemUpdateRequest {

    @Pattern(regexp = ".*\\S.*", message = "Название не должно быть пустым или состоять только из пробелов")
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "Описание не должно быть пустым или состоять только из пробелов")
    private String description;

    private Boolean available;

    public boolean hasName() {
        return name != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasAvailavle() {
        return available != null;
    }
}
