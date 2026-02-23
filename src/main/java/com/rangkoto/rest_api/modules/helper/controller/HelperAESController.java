package com.rangkoto.rest_api.modules.helper.controller;

import com.rangkoto.rest_api.modules.helper.dto.DecryptRequest;
import com.rangkoto.rest_api.modules.helper.dto.EncryptRequest;
import com.rangkoto.rest_api.modules.helper.service.HelperAESService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/helper/aes")
public class HelperAESController {
    private final HelperAESService helperAESService;

    public HelperAESController(HelperAESService helperAESService) {
        this.helperAESService = helperAESService;
    }

    @PostMapping("/encrypt")
    public Map<String, String> encrypt(@Valid @RequestBody EncryptRequest request) throws Exception {
        String encrypted = helperAESService.encryptData(request.getData());
        return Map.of("encrypted", encrypted);
    }

    @PostMapping("/decrypt")
    public Map<String, Object> decrypt(@Valid @RequestBody DecryptRequest request) throws Exception {
        return helperAESService.decryptData(request.getEncrypted());
    }

    @PostMapping("/encrypt2")
    public Map<String, String> encrypt2(@Valid @RequestBody EncryptRequest request) throws Exception {
        String encrypted = helperAESService.encryptData2(request.getData());
        return Map.of("encrypted", encrypted);
    }

    @PostMapping("/decrypt2")
    public Map<String, Object> decrypt2(@Valid @RequestBody DecryptRequest request) throws Exception {
        return helperAESService.decryptData2(request.getEncrypted());
    }
}
