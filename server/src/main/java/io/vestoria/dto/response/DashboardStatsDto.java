package io.vestoria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
  private BigDecimal dailyEarnings;
  private Integer activeBusinesses;
  private Double marketShare;
}
