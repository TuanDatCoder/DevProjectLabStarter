package com.example.devprojectlabstarter.dto.Auth.Register;

import com.example.devprojectlabstarter.entity.Enum.AccountGenderEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String email;
    private String name;
    private AccountGenderEnum gender;
    private String password;
    private AccountRoleEnum role;

}