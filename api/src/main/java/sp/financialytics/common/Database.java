package sp.financialytics.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

// Represents the program database, allows for modification, read/write for the db.json file
@Data
public class Database {
  private static final Logger LOG = LoggerFactory.getLogger(Database.class);

  private String name;
  private List<User> users;

  private Database() { }

  @JsonIgnore
  public User getCurrentUser() {
    return users.get(0);
  }

  public static Database load(File databaseFile) throws IOException {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.setDateFormat(new SimpleDateFormat("dd-MMM-yyyy"));
      JsonNode root = mapper.readTree(databaseFile);
      return mapper.convertValue(root, Database.class);
    } catch (IOException e) {
      LOG.error("Critical failure: ", e);
      throw e;
    }
  }

  public void update() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setDateFormat(new SimpleDateFormat("dd-MMM-yyyy"));
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    mapper.writeValue(new File("api/src/main/resources/db.json"), this);
  }
}
