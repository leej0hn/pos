package io.communet.pos.web.controller;

import io.communet.pos.common.exception.ServiceException;
import io.communet.pos.common.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>function:
 * <p>User: LeeJohn
 * <p>Date: 2017/01/11
 * <p>Version: 1.0
 */
@Slf4j
@RestController
public class TestController {


    @RequestMapping(value = "/api/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response index() {
        return Response.ok("test");
    }

    @GetMapping("/page/test")
    public ModelAndView testPage() {
        String test = "Test Page , hello world ";
        return new ModelAndView("testPage","test",test);
    }

    @GetMapping("/api/exception")
    public Response testExcption(){
        throw new ServiceException("testException msg");
    }

}
