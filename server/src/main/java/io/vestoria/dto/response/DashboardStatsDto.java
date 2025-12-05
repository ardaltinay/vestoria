package io.vestoria.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto implements Serializable {
    private BigDecimal dailyEarnings;
    private BigDecimal dailyExpenses;
    private Integer activeBusinesses;
    private Double marketShare;
}
