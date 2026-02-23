package com.rangkoto.rest_api.modules.helper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rangkoto.rest_api.common.AESHandler;
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

    public String encryptData2(Object data) throws Exception {
        AESHandler aesHandler = new AESHandler("base64:BQ8D86bEK4z0te67ls73hR8nuaWWBMC9mFDGKTnGGv8=", "AES-256-CBC");
        return aesHandler.encrypt(data);
    }

    public Map<String, Object> decryptData2(String encrypted) throws Exception {
        AESHandler aesHandler = new AESHandler("base64:BQ8D86bEK4z0te67ls73hR8nuaWWBMC9mFDGKTnGGv8=", "AES-256-CBC");
        return aesHandler.decrypt(encrypted, new TypeReference<Map<String, Object>>() {
        });
    }
}
