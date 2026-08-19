package com.example.New_Project.DTO;

import com.example.New_Project.enums.Role;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private Long number;
    private Role role;

}