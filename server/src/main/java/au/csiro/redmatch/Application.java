package au.csiro.redmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;

@SpringBootApplication
@EnableScheduling
public class Application implements WebMvcConfigurer {

  /**
   * Created here as a bean because it is expensive to create and we only need one instance that 
   * can be shared.
   *
   * @return FHIRContext The FHIR context for the entire application.
   */
  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }
  
  @Bean
  public Gson gson() {
    return new Gson();
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
