package sp.financialytics.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DatabaseTest {
  Database test;

  private List<Transaction> createTestTransactionList() {
    ArrayList<Transaction> transactions = new ArrayList<>();
    transactions.add(new Transaction("id", LocalDate.now(), "description", "category", 1L, ""));
    return transactions;
  }

  private Warning[] createTestWarningConfig() {
    return new Warning[] { new Warning("hide_monthly_budget", true, false) };
  }

  private List<Target> createTestTargets() {
    return new ArrayList<>(List.of(new Target(0, "income", 200000L, false), new Target(1, "food", 20000L, true),
            new Target(2, "living", 20001L, true), new Target(3, "entertainment", 20002L, true),
            new Target(4, "supplies", 20003L, true), new Target(5, "education", 20004L, true),
            new Target(6, "other", 20005L, true)));
  }

  private User createTestUser() {
    return new User(1, "dev", "adminDev", "example@gmail.com", LeniencyLevel.NORMAL, createTestTransactionList(),
            createTestWarningConfig(), createTestTargets());
  }

  @Test
  void getCurrentUser() {
    try {
      test = new Database(List.of(createTestUser()));

      User result = test.getCurrentUser();

      assertEquals(createTestUser(), result);
    } catch (RuntimeException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void getCurrentUserEmptyUsers() {
    test = new Database(List.of());

    try {
      test.getCurrentUser();
      fail("Should have thrown an exception.");
    } catch (RuntimeException e) {
      assertEquals(0, test.getUsers().size());
    }
  }

  @Test
  void getCurrentUserNullUsers() {
    test = new Database(null);

    try {
      test.getCurrentUser();
      fail("Should have thrown an exception.");
    } catch (RuntimeException e) {
      assertNull(test.getUsers());
    }
  }

  private ObjectMapper setupObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setDateFormat(new SimpleDateFormat("dd-MMM-yyyy"));

    return mapper;
  }

  @Test
  void update() {
    test = new Database(List.of(createTestUser()));

    try {
      File writeFile = new File("src/test/resources/write-db.json");
      test.update(writeFile);

      ObjectMapper mapper = setupObjectMapper();
      Database result = mapper.readValue(writeFile, Database.class);

      assertEquals(test, result);
      if (!writeFile.delete())
        System.out.println("Failed to delete file: " + writeFile.getAbsolutePath());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void load() {
    try {
      test = Database.load(new File("src/test/resources/working-test-db.json"));

      assertEquals("Test DB", test.getName());
      List<User> resultUsers = test.getUsers();
      assertThat(resultUsers).size().isEqualTo(1);
      User resultUser = resultUsers.get(0);
      assertEquals("dev", resultUser.getName());
      assertEquals("adminDev", resultUser.getPassword());
      assertEquals("example@gmail.com", resultUser.getEmail());
      assertEquals(1, resultUser.getId());
      assertThat(resultUser.getTransactions()).size().isEqualTo(3);
      assertEquals(2, resultUser.getWarningConfig().length);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void loadException() {
    try {
      Database.load(new File("src/test/resources/does-not-exist-db.json"));
      fail("Should have thrown an exception.");
    } catch (IOException e) {
      assertEquals(
              "src\\test\\resources\\does-not-exist-db.json (The system cannot find the file specified)",
              e.getMessage()
      );
    }
  }
}