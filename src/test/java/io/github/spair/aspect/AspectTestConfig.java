package io.github.spair.aspect;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@Profile("aspect-test")
@SpringBootApplication(scanBasePackages = "io.github.spair.aspect")
public class AspectTestConfig {
}
