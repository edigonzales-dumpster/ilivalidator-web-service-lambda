package ch.so.agi.ilivalidator.lambda;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
//import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.function.context.FunctionRegistration;
//import org.springframework.cloud.function.context.FunctionalSpringApplication;
//import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
//import org.springframework.context.support.GenericApplicationContext;
//import org.springframework.cloud.function.context.FunctionType;

@SpringBootApplication
//@SpringBootConfiguration
//public class IlivalidatorLambdaApplication implements ApplicationContextInitializer<GenericApplicationContext> {
public class IlivalidatorLambdaApplication {

	public static void main(String[] args) {
        SpringApplication.run(IlivalidatorLambdaApplication.class, args);
        //FunctionalSpringApplication.run(IlivalidatorLambdaApplication.class, args);
	}
	
    @Bean
    public Function<String, Boolean> containsCloud() {
        return value -> value.contains("cloud");
    }

//    @Override
//    public void initialize(GenericApplicationContext context) {
//        context.registerBean("containsCloud", FunctionRegistration.class,
//                () -> new FunctionRegistration<>(containsCloud())
//                        .type(FunctionType.from(String.class).to(Boolean.class)));
//    }
}
