package io.vestoria.converter;

import io.vestoria.dto.response.MarketResponseDto;
import io.vestoria.entity.MarketEntity;
import org.springframework.stereotype.Component;

@Component
public class MarketConverter {

    public MarketResponseDto toResponseDto(MarketEntity entity) {
        if (entity == null) {
            return null;
        }

        return MarketResponseDto.builder().id(entity.getId()).sellerUsername(entity.getSeller().getUsername())
                .itemId(entity.getItem().getId()).itemName(entity.getItem().getName())
                .itemUnit(entity.getItem().getUnit()).itemTier(entity.getItem().getTier()).price(entity.getPrice())
                .quantity(entity.getQuantity()).qualityScore(entity.getItem().getQualityScore()).build();
    }
}
