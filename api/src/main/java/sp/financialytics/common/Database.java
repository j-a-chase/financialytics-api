package sp.financialytics.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import static java.util.Objects.isNull;

// Represents the program database, allows for modification, read/write for the db.json file
@Data
public class Database {
  private static final Logger LOG = LoggerFactory.getLogger(Database.class);

  private String name;
  private List<User> users;

  @SuppressWarnings("unused")
  private Database() { }

  Database(List<User> users) {
    this.name = "Test DB";
    this.users = users;
  }

  @JsonIgnore
  public User getCurrentUser() throws RuntimeException {
    if (isNull(users) || users.isEmpty()) {
      throw new RuntimeException("No users found.");
    }

    return users.get(0);
  }

  // ensures the ObjectMapper is configured for LocalDate properly
  private static ObjectMapper configureObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setDateFormat(new SimpleDateFormat("dd-MMM-yyyy"));
    return mapper;
  }

  // reads in the db JSON file and gives us a Database object to work with
  public static Database load(File databaseFile) throws IOException {
    try {
      return configureObjectMapper().readValue(databaseFile, Database.class);
    } catch (IOException e) {
      LOG.error("Critical failure: ", e);
      throw e;
    }
  }

  // uses the Database object to update the JSON file with any changes
  public void update(File writeFile) throws IOException {
    ObjectMapper mapper = configureObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    // sort transactions prior to updating the database
    List<Transaction> transactions = getCurrentUser().getTransactions();
    transactions.sort(Transaction::compareTo);

    // update transaction ids after sorting
    for (int i = 0; i < transactions.size(); i++) {
      String id = String.format("%s-%s", getCurrentUser().getId(), i);
      transactions.get(i).setId(id);
    }

    // ensure new/edited targets have the proper ids
    List<Target> targets = getCurrentUser().getTargets();
    for (int i = 0; i < targets.size(); i++) {
      targets.get(i).setId(i);
    }

    mapper.writeValue(writeFile, this);
  }
}
