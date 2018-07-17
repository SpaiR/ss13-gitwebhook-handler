package io.github.spair.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@SuppressWarnings("AroundAdviceStyleInspection")
public class StartHandlerInThreadAspect {

    @Around("within(io.github.spair.handler.Handler+) && execution(void handle(..))")
    public void around(final ProceedingJoinPoint joinPoint) {
        new Thread(() -> {
            try {
                joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
