package io.vestoria.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class RegisterRequestDto implements Serializable {
    @NotBlank(message = "Kullanıcı adı zorunludur")
    @Size(min = 4, max = 20, message = "Kullanıcı adı 4-20 karakter arasında olmalıdır")
    private String username;

    @NotBlank(message = "Şifre zorunludur")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._,-]).{8,}$", message = "Şifre en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir")
    private String password;

    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;
}
