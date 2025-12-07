package io.vestoria.service;

import io.vestoria.dto.response.CategoryBreakdownDto;
import io.vestoria.dto.response.DailyFinancialDto;
import io.vestoria.dto.response.ReportResponseDto;
import io.vestoria.entity.UserEntity;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ReportResponseDto getReports(String username, int days) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        List<DailyFinancialDto> dailyIncome = transactionRepository.getDailyIncome(user, startDate);
        List<DailyFinancialDto> dailyExpense = transactionRepository.getDailyExpense(user, startDate);
        List<CategoryBreakdownDto> incomeByCategory = transactionRepository.getIncomeByCategory(user, startDate);

        return ReportResponseDto.builder().dailyIncome(dailyIncome).dailyExpense(dailyExpense)
                .incomeByCategory(incomeByCategory).build();
    }
}
