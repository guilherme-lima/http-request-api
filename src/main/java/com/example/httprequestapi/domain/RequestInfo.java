package com.example.httprequestapi.domain;

import lombok.Data;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * Created by guilherme-lima on 17/03/18.
 * http://github.com/guilherme-lima
 */
@Data
public class RequestInfo {

    private String url;
    private HttpMethod httpMethod;
    private MultiValueMap<String, String> headerElements;
    private String requestBody;
    private Map<String, List<String>> variables;
    private boolean tokenNeeded;
    private RequestTokenInfo requestTokenInfo;
    private Long sleepTimeInMilliseconds;
    private Integer amountOfTreads;
}
