package com.skill.system.service.impl;

import com.skill.system.entity.LoginLog;
import com.skill.system.mapper.LoginLogMapper;
import com.skill.system.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    @Override
    public void save(LoginLog log) {
        loginLogMapper.insert(log);
    }
}
