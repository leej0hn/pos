package io.communet.pos.web.exception;

import io.communet.pos.common.vo.Response;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>function:
 * <p>User: LeeJohn
 * <p>Date: 2017/3/15
 * <p>Version: 1.0
 */
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler( Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Response exception(Exception exception) {
        return Response.fail(exception.getMessage());
    }
}
