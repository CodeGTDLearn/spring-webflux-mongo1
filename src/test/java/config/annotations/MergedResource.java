package config.annotations;

import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Testcontainers
@ResourceConfig
@GlobalConfig
public @interface MergedResource {
}