package ir.bigz.springbootreal.commons.generallog;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bigz.springbootreal.commons.util.Utils;
import ir.bigz.springbootreal.exception.AppException;
import ir.bigz.springbootreal.exception.HttpExceptionModel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class AppLogAspect {

    private final Logger LOG = LoggerFactory.getLogger(AppLogAspect.class);

    private final HttpServletRequest request;

    public AppLogAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Before("ir.bigz.springbootreal.commons.generallog.CommonJoinPoint.controllerExecution()")
    public void beforeCallControllerMethod(JoinPoint joinPoint){
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String queryString = "";
        if(Utils.isNotNull(request.getQueryString())){
            queryString = request.getQueryString();
        }
        String reduce = Arrays.stream(args).reduce("", (s, s2) -> s + " " + s2).toString();
        LOG.info("before method: {} | argument: {} | queryString: {}",
                methodName, reduce, queryString);
    }

    @AfterReturning(value = "ir.bigz.springbootreal.commons.generallog.CommonJoinPoint.controllerExecution()",
            returning = "obj")
    public void afterReturningResponseOfControllerMethod(JoinPoint joinPoint, Object obj){
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String reduce = Arrays.stream(args).reduce("", (s, s2) -> s + " " + s2).toString();
        LOG.info("after method: {} | argument: {} | result: {}", methodName, reduce, ((ResponseEntity) obj).getBody());
    }

    @AfterThrowing(value = "ir.bigz.springbootreal.commons.generallog.CommonJoinPoint.serviceExecution()", throwing = "exception")
    public void logAfterThrowException(JoinPoint joinPoint, AppException exception){
        String methodName = joinPoint.getSignature().getName();
        LOG.info("exception method: {} | errorCode: {} | message: {}",
                methodName, exception.getHttpErrorCode(), exception.getDetail());
    }

    @AfterReturning(value = "execution(* ir.bigz.springbootreal.exception.validation.ErrorController.*(..))", returning = "object")
    public void logAfterThrowValidationException(JoinPoint joinPoint, Object object){
        HttpExceptionModel httpExceptionModel = (HttpExceptionModel) ((ResponseEntity) object).getBody();
        LOG.info("validation exception path: {} | errorCode: {} | errors: {}",
                httpExceptionModel.getValidationError().getPath(),
                httpExceptionModel.getHttpErrorCode(),
                httpExceptionModel.getValidationError().getErrors());
    }
}
