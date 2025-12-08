package io.vestoria.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoginRequestDto implements Serializable {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
