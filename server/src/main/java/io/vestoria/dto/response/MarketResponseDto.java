package io.vestoria.dto.response;

import io.vestoria.enums.ItemCategory;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

import java.io.Serializable;

@Data
@Builder
public class MarketResponseDto implements Serializable {
  private UUID id;
  private String sellerUsername;
  private UUID itemId;
  private String itemName;
  private ItemUnit itemUnit;
  private ItemCategory itemCategory;
  private ItemTier itemTier;
  private BigDecimal price;
  private Integer quantity;
  private BigDecimal qualityScore;
}
