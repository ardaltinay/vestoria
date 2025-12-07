package io.vestoria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private List<DailyFinancialDto> dailyIncome;
    private List<DailyFinancialDto> dailyExpense;
    private List<CategoryBreakdownDto> incomeByCategory;
}
