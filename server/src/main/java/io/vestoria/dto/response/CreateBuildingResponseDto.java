package io.vestoria.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateBuildingResponseDto(String id, String type, String subType, String ownerId,
                                        BigDecimal cost, Integer maxStock, Long productionRate,
                                        Integer level, String status) {
}
