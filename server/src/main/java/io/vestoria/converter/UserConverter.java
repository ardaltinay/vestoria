package io.vestoria.converter;

import io.vestoria.dto.response.DashboardStatsDto;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public DashboardStatsDto toDashboardStatsDto(BigDecimal dailyEarnings, BigDecimal dailyExpenses,
            int activeBusinesses, double marketShare) {
        return DashboardStatsDto.builder().dailyEarnings(dailyEarnings).dailyExpenses(dailyExpenses)
                .activeBusinesses(activeBusinesses).marketShare(marketShare).build();
    }
}
