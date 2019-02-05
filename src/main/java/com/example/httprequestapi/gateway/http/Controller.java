package com.example.httprequestapi.gateway.http;

import com.example.httprequestapi.domain.RequestInfo;
import com.example.httprequestapi.usecase.DoRequestUC;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by guilherme-lima on 17/10/18.
 * https://github.com/guilherme-lima
 */
@RestController
@RequestMapping(Controller.URI)
@RequiredArgsConstructor
public class Controller {

    static final String URI = "/";
    private final DoRequestUC useCase;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void request(@Valid final RequestInfo requestInfo) {
        useCase.execute(requestInfo);
    }
}