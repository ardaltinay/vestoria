package io.vestoria.dto.request;

import io.vestoria.enums.AnnouncementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreateAnnouncementRequestDto implements Serializable {
  @NotBlank(message = "Başlık boş olamaz")
  private String title;

  @NotBlank(message = "İçerik boş olamaz")
  private String content;

  @NotNull(message = "Tür boş olamaz")
  private AnnouncementType type;
}
