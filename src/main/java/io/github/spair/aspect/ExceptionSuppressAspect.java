package io.github.spair.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@SuppressWarnings("AroundAdviceStyleInspection")
public class ExceptionSuppressAspect {

    @Around("within(io.github.spair.handler.command.HandlerCommand+) && execution(void execute(..))")
    public void around(final ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            joinPoint.proceed();
        } catch (Exception e) {
            final Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
            logger.error("Suppressed exception in {}", joinPoint.getSignature(), e);
        }
    }
}
