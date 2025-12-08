package io.vestoria.service;

import io.vestoria.constant.Constants;
import io.vestoria.repository.MarketRepository;
import io.vestoria.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EconomicService {

  private final MarketRepository marketRepository;
  private final TransactionRepository transactionRepository;

  @Cacheable(value = "marketPrices", key = "#itemName", unless = "#result == null")
  public BigDecimal getMarketPrice(String itemName) {
    BigDecimal basePrice = Constants.BASE_PRICES.getOrDefault(itemName, BigDecimal.valueOf(10));

    // 1. Calculate Supply (Total Active Quantity)
    long activeSupply = marketRepository.sumActiveQuantityByItemName(itemName);

    // 2. Calculate Demand (Sales Volume in last 24h)
    LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
    Integer demandVolume = transactionRepository.sumByItemNameAndCreatedAtAfter(itemName, twentyFourHoursAgo);
    long activeDemand = demandVolume != null ? demandVolume.longValue() : 0L;

    // Base demand buffer: assume at least 10 units are always needed to prevent /0
    // or huge spikes on low volume
    double effectiveDemand = activeDemand + 10.0;
    double effectiveSupply = activeSupply <= 0 ? 1.0 : activeSupply;

    // 3. Calculate Ratio: Demand / Supply
    double ratio = effectiveDemand / effectiveSupply;

    // 4. Determine Multiplier (Clamped between 0.5 and 3.0)
    double multiplier = Math.max(0.5, Math.min(3.0, ratio));

    // 5. Calculate Final Price
    BigDecimal dynamicPrice = basePrice.multiply(BigDecimal.valueOf(multiplier));

    // Round to 2 decimal places
    return dynamicPrice.setScale(2, RoundingMode.HALF_UP);
  }
}
