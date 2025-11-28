package io.vestoria.converter;

import io.vestoria.dto.response.ItemResponseDto;
import io.vestoria.entity.ItemEntity;
import org.springframework.stereotype.Component;

@Component
public class ItemConverter {

    public ItemResponseDto toItemResponseDto(ItemEntity item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .unit(item.getUnit())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .qualityScore(item.getQualityScore())
                .tier(item.getTier())
                .build();
    }
}
