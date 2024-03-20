package penna.config.yaml.parser;

import org.junitpioneer.jupiter.cartesian.CartesianArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@CartesianArgumentsSource(ParserTestCasesProvider.class)
public @interface ParserTestCases {}
