package com.example.httprequestapi.domain;

import lombok.Data;

/**
 * Created by guilherme-lima on 17/03/18.
 * http://github.com/guilherme-lima
 */
@Data
public class RequestTokenInfo {

    private String accessTokenUrl;
    private String clientId;
    private String secret;
    private String scope;
    private String grantType;
}
