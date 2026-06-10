package com.example.labmanage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String realName;
    private List<String> roles;
    private String roleName;
    private String roleCode;
}
