package jbook.jshop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AspectSample {

    //타겟 메서드 : jbook.jshop.service.FeatureToggleService.isEnabled

    @Pointcut("execution(* jbook.jshop.service.FeatureToggleService.isEnabled*(..))")
    public void isEnabledPointCut() { }

    @Before("isEnabledPointCut()")
    public void beforeEnable(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof String code) {
            log.info("■ @Before code : {}", code);
        }
    }

    @AfterReturning(pointcut="isEnabledPointCut()", returning = "returnObject")
    public void afterReturningEnable(JoinPoint jp, Object returnObject) {
        if(returnObject instanceof Boolean isFeatureOn) {
            log.info("■ @AfterReturning isFeatureOn : {}", isFeatureOn);
        }
    }

    @After("isEnabledPointCut()")
    public void afterEnable(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof String code) {
            log.info("■ @After code : {}", code);
        }
    }

}
