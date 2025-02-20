package sp.financialytics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ControllerConfig {
  @Bean
  public File databaseFile() {
    return new File("api/src/main/resources/db.json");
  }
}
