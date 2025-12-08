package io.vestoria.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements Serializable {
    private int status;
    private String message;
    private LocalDateTime timestamp;
}
