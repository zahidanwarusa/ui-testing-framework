package com.umr.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as executable keywords in the test framework.
 * This annotation is used by the KeywordExecutor to identify and execute methods
 * corresponding to keywords specified in the test flow.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Keyword {
    /**
     * The name of the keyword as it appears in the test flow.
     *
     * @return The keyword name
     */
    String value();

    /**
     * Optional description of the keyword's purpose and behavior.
     *
     * @return The keyword description
     */
    String description() default "";

    /**
     * Whether the keyword is mandatory for test execution.
     * If true, test execution will fail if this keyword fails.
     *
     * @return True if the keyword is mandatory, false otherwise
     */
    boolean mandatory() default true;
}