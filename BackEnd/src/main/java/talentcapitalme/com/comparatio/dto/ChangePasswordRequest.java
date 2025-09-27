package talentcapitalme.com.comparatio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "New password is required")
    private String newPassword;
}