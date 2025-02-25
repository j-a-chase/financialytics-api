package sp.financialytics.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sp.financialytics.common.Database;
import sp.financialytics.common.Transaction;
import sp.financialytics.common.User;
import sp.financialytics.common.Warning;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class UserControllerTest {
  UserController test;
  Database database;

  @BeforeEach
  void setUp() {
    try {
      database = spy(Database.load(new File("src/test/resources/working-test-db.json")));
    } catch (IOException e) {
      fail(e.getMessage());
    }

    test = new UserController(database);
  }

  private List<Transaction> createTestTransactions() {
    return List.of(
            new Transaction("1-0", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 117),
            new Transaction("1-1", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 118),
            new Transaction("1-2", LocalDate.of(2025, 2, 24), "No Description Set.", "income", 500)
    );
  }

  private Warning[] createTestWarningConfig() {
    return new Warning[] {
            new Warning("hide_monthly_budget", true, false),
            new Warning("hide_category_budget", true, false)
    };
  }

  private Map<String, Long> createTestTargetsMap() {
    return Map.of("income", 200000L, "food", 20000L, "living", 20001L, "entertainment", 20002L, "supplies", 20003L,
            "education", 20004L, "other", 20005L);
  }

  private User createTestUser() {
    return new User(1, "example@gmail.com", "adminDev", "dev", createTestTransactions(), createTestWarningConfig(),
            createTestTargetsMap());
  }

  @Test
  void initialize() {
    ResponseEntity<User> result = test.initialize(1);

    assertNotNull(result.getBody());
    assertEquals(createTestUser(), result.getBody());
  }

  @Test
  void initializeNoUsers() {
    when(database.getCurrentUser()).thenThrow(IndexOutOfBoundsException.class);

    ResponseEntity<User> result = test.initialize(1);

    assertEquals(ResponseEntity.internalServerError().build(), result);
  }
}