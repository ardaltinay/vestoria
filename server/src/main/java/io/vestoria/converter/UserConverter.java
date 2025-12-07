package io.vestoria.converter;

import io.vestoria.dto.response.DashboardStatsDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UserConverter {

    public DashboardStatsDto toDashboardStatsDto(BigDecimal dailyEarnings, BigDecimal dailyExpenses,
                                                 int activeBusinesses, double marketShare) {
        return DashboardStatsDto.builder().dailyEarnings(dailyEarnings).dailyExpenses(dailyExpenses)
                .activeBusinesses(activeBusinesses).marketShare(marketShare).build();
    }
}
