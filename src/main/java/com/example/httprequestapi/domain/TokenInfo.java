package com.example.httprequestapi.domain;

import lombok.Data;

/**
 * Created by guilherme-lima on 17/03/18.
 * http://github.com/guilherme-lima
 */
@Data
public class TokenInfo {

    private String access_token;
    private String token_type;
    private Integer expires_in;
    private String scope;
    private String jit;
}
