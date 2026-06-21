package com.skill.common.util;

import java.util.UUID;

/**
 * UUID 工具类 - 生成32位无横线UUID
 */
public class UuidUtils {

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private UuidUtils() {}
}
