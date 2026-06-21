package com.skill.auth.model;

import lombok.Data;

@Data
public class SmsLoginRequest {
    private String phone;
    private String code;
    private String loginType = "sms";
}
