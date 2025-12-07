package io.vestoria.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateBuildingResponseDto(String id, String type, String subType, String tier, String ownerId,
                                        BigDecimal cost, Integer maxStock, BigDecimal productionRate, Integer level,
                                        String status) {
}
