package io.communet.pos.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>function:
 * <p>User: LeeJohn
 * <p>Date: 2017/01/11
 * <p>Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/pos/api/")
public class TestController {


    @RequestMapping(value = "/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String index() {

        return "test";
    }


}
