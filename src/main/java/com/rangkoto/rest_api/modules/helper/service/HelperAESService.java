package com.rangkoto.rest_api.modules.helper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rangkoto.rest_api.common.AESUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HelperAESService {
    private final AESUtil aesUtil;

    public HelperAESService(AESUtil aesUtil) {
        this.aesUtil = aesUtil;
    }

    public String encryptData(Object data) throws Exception {
        return aesUtil.encrypt(data);
    }

    public Map<String, Object> decryptData(String encrypted) throws Exception {
        return aesUtil.decrypt(encrypted, new TypeReference<Map<String, Object>>() {
        });
    }
}
