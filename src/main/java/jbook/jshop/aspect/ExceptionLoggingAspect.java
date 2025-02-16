package jbook.jshop.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jbook.jshop.service.DynamoDbExceptionLoggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ExceptionLoggingAspect {

    @Autowired
    private DynamoDbExceptionLoggerService exceptionLoggerService;

    @AfterThrowing(pointcut = "execution(* jbook.jshop.controller..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        // 1) RequestContextHolder에서 HttpServletRequest 가져오기
        ServletRequestAttributes sra =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String apiUrl = "unknown";
        if (sra != null) {
            HttpServletRequest request = sra.getRequest();
            apiUrl = request.getRequestURI();
        }

        // 2) Throwable -> Exception 변환 가능 시 DynamoDB 로깅
        if (ex instanceof Exception) {
            Exception exception = (Exception) ex;
            // 3) 클래스명, 메서드명 등
            String signature = joinPoint.getSignature().toShortString();
            // ex.getStackTrace()에서 라인 번호 등 상세 추출 가능 (Logger 내부에서도 처리)

//            exceptionLoggerService.logException(exception, apiUrl, signature);
//            exceptionLoggerService.logException(exception, apiUrl);
        }
        // 예외는 다시 호출자에게 전달됨 (throw)
    }
}
