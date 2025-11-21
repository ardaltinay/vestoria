package io.vestoria.dto.request;

import io.vestoria.enums.BuildingSubType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetProductionRequestDto {
  private BuildingSubType productionType;
}
