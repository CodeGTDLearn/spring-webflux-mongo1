package config.annotations;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@Retention(RUNTIME)
@Target(TYPE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
public @interface GlobalConfig {
}
