package io.communet.pos.web.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: xjch
 * Date: 14-4-17
 * Time: 上午10:35
 * Mail:xjch7703@163.com
 */
@Component
@Aspect
@Slf4j
public class WebLogAspect {

    /**
     * 调用启动服务
     * @param joinPoint
     */
    @Before("execution(* io.communet.pos.web.controller..*(..))")
    public void logBefore(JoinPoint joinPoint){
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        StringBuilder postData = new StringBuilder();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            postData.append(entry.getKey()+" : ");
            for (String value : entry.getValue()) {
                postData.append(value +" ");
            }
            postData.append(" ; ");
        }

        log.info("======================请求信息===before=======================");
        log.info("ContentType:{}/{}", request.getContentType(), request.getCharacterEncoding());
        log.info("method: {}", request.getMethod());
        log.info("sessionId: {}",request.getSession().getId());
        log.info("url: {}", request.getRequestURL() );
        log.info("params: {}", request.getQueryString());
        log.info("post params: {}", postData.toString());
    }

    /**
     * 服务结束时调用
     * @param joinPoint
     * @param reponse
     */
    @AfterReturning(value = "execution(* io.communet.pos.web.controller..*(..)) or execution(* io.communet.pos.web.exception..*(..))",returning = "reponse")
    public void logAfterRunning(JoinPoint joinPoint, Object reponse){
        log.info("------------------------after--------------------------------------");
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        log.info("sessionId: {}",request.getSession().getId());
        log.info("url : {}", request.getRequestURL());
        log.info("返回: {}", reponse.toString());
    }
}
