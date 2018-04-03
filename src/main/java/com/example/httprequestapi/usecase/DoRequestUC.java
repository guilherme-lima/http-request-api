package com.example.httprequestapi.usecase;

import com.example.httprequestapi.domain.RequestInfo;
import com.example.httprequestapi.domain.RequestTokenInfo;
import com.example.httprequestapi.domain.TokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;


/**
 * Created by guilherme-lima on 17/03/18.
 * http://github.com/guilherme-lima
 */
@Component
public class DoRequestUC {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private TokenInfo tokenInfo;
    private LocalDateTime tokenTime;

    public void execute(RequestInfo requestInfo, RequestTokenInfo requestRequestTokenInfo) {
        LocalDateTime startTime = LocalDateTime.now();
        int requestTimes = howManyRequests(requestInfo);
        ForkJoinPool myPool = new ForkJoinPool(requestInfo.getAmountOfTreads());
        try {
            myPool.submit(() -> IntStream.range(0, requestTimes)
                        .forEach(execTime -> request(requestInfo, requestRequestTokenInfo, execTime)))
                  .get();
            log.info("Elapsed time: " +
                    startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) * 1000L + " segundos.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private int howManyRequests(final RequestInfo requestInfo) {
        int requestTimes = 0;
        for (List<String> value : requestInfo.getVariables().values()) {
            if (value.size() > requestTimes)
                requestTimes = value.size();
        }
        return requestTimes;
    }

    private void request(final RequestInfo requestInfo,
                         final RequestTokenInfo requestRequestTokenInfo,
                         final Integer execTime) {
        String url = getRequestUrl(requestInfo, execTime);
        HttpMethod httpMethod = requestInfo.getHttpMethod();
        HttpHeaders header = getRequestHeader(requestInfo, requestRequestTokenInfo, execTime);
        String body = getRequestBody(requestInfo, execTime);

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, header);
        log.info("Requesting...");
        ResponseEntity<String> response = getRestTemplate().exchange(url, httpMethod, requestEntity, String.class);
        log.info("Response: " + response.getStatusCodeValue() + " " + response.getStatusCode().toString());
        log.info("Body: \n" + response.getBody());
        if (Objects.nonNull(requestInfo.getSleepTimeInMilliseconds())) {
            try {
                Thread.sleep(requestInfo.getSleepTimeInMilliseconds());
            } catch (InterruptedException e) {
                log.error("Error during Thread Sleep.");
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    private String getRequestUrl(final RequestInfo requestInfo, final Integer execTime) {
        return replaceVariables(requestInfo, execTime, new String[]{requestInfo.getUrl()});
    }

    private HttpHeaders getRequestHeader(final RequestInfo requestInfo,
                                         final RequestTokenInfo requestRequestTokenInfo,
                                         final Integer execTime) {
        requestInfo.getHeaderElements().forEach((key, value) -> requestInfo.getHeaderElements()
                .set(key, replaceVariables(requestInfo, execTime, new String[]{String.valueOf(value)})));

        HttpHeaders header = new HttpHeaders();
        header.addAll(requestInfo.getHeaderElements());
        if (requestInfo.isTokenNeeded() && newTokenNeeded()) {
            tokenInfo = getToken(requestRequestTokenInfo);
            header.set("Authorization", "Bearer " + tokenInfo.getAccess_token());
        }
        return header;
    }

    private String getRequestBody(final RequestInfo requestInfo, final Integer execTime) {
        return replaceVariables(requestInfo, execTime, new String[]{requestInfo.getRequestBody()});
    }

    private String replaceVariables(RequestInfo requestInfo, Integer execTime, String[] url) {
        requestInfo.getVariables().keySet().forEach(key -> {
            if (url[0].contains(key)) {
                List<String> varList = requestInfo.getVariables().get(key);
                if (!CollectionUtils.isEmpty(varList)) {
                    int index;
                    if (varList.size() > execTime)
                        index = execTime;
                    else
                        index = varList.size() - 1;
                    url[0] = url[0].replace(key, varList.get(index));
                }
            }
        });
        return url[0];
    }

    private synchronized TokenInfo getToken(final RequestTokenInfo requestRequestTokenInfo) {
        log.info("Getting a new token.");
        tokenTime = LocalDateTime.now();

        String url = requestRequestTokenInfo.getAccessTokenUrl();
        HttpHeaders header = getTokenRequestHeader(requestRequestTokenInfo);
        MultiValueMap<String, String> body = getTokenRequestBody(requestRequestTokenInfo);

        return getRestTemplate()
                .postForEntity(url, getRequest(header, body) , TokenInfo.class)
                .getBody();
    }

    private HttpEntity<MultiValueMap<String, String>> getRequest(final HttpHeaders header,
                                                                 final MultiValueMap<String, String> body) {
        return new HttpEntity<>(body, header);
    }

    private RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    private HttpHeaders getTokenRequestHeader(final RequestTokenInfo requestRequestTokenInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + getBase64Auth(requestRequestTokenInfo));
        return headers;
    }

    private MultiValueMap<String, String> getTokenRequestBody(final RequestTokenInfo requestRequestTokenInfo) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", requestRequestTokenInfo.getGrantType());
        body.add("scope", requestRequestTokenInfo.getScope());
        return body;
    }

    private String getBase64Auth(final RequestTokenInfo requestRequestTokenInfo) {
        String auth = requestRequestTokenInfo.getClientId() + ":" + requestRequestTokenInfo.getSecret();
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    private boolean newTokenNeeded() {
        return tokenInfo == null ||
            tokenTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) >= (tokenInfo.getExpires_in() - 10);
    }
}
