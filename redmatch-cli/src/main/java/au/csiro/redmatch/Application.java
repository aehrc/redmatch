package au.csiro.redmatch;

import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.TerminologyService;
import ca.uhn.fhir.context.FhirContext;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }

  @Bean
  public Gson gson() {
    return new Gson();
  }

  @Bean
  public TerminologyService terminologyService() {
    return new TerminologyService(ctx, gson);
  }

  @Bean
  public RedmatchCompiler compiler() {
    return new RedmatchCompiler(gson, terminologyService,
      new VersionedFhirPackage(defaultFhirPackage, defaultFhirPackageVersion));
  }

  @Autowired
  private Gson gson;

  @Autowired
  private RedmatchApi api;

  @Autowired
  private FhirContext ctx;

  @Autowired
  private TerminologyService terminologyService;

  @Value("${redmatch.fhir.package-name}")
  private String defaultFhirPackage;

  @Value("${redmatch.fhir.package-version}")
  private String defaultFhirPackageVersion;

  /**
   * Main method.
   * 
   * @param args Arguments.
   */
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Application.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.run(args);
  }

  @Override
  public void run(String... args) {
    CommandLineInterface cli = new CommandLineInterface();
    cli.run(args, api);
  }

}
