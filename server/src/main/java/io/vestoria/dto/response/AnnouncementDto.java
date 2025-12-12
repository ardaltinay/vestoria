package io.vestoria.dto.response;

import io.vestoria.enums.AnnouncementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
  private UUID id;
  private String title;
  private String content;
  private AnnouncementType type;
  private String createdTime;
  private Boolean isActive;
}
