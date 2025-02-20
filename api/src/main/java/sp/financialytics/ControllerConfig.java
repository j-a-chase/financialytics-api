package sp.financialytics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sp.financialytics.common.Database;

import java.io.File;
import java.io.IOException;

@Configuration
public class ControllerConfig {
  @Bean
  public File databaseFile() {
    return new File("api/src/main/resources/db.json");
  }

  @Bean
  public Database database() throws IOException {
    return Database.load(databaseFile());
  }
}
