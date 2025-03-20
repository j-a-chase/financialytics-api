package sp.financialytics.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sp.financialytics.common.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
            new Transaction("1-0", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 117L, ""),
            new Transaction("1-1", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 118L, ""),
            new Transaction("1-2", LocalDate.of(2025, 2, 24), "No Description Set.", "income", 500L, "")
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
    return new User(1, "example@gmail.com", "adminDev", "dev", LeniencyLevel.NORMAL, createTestTransactions(), createTestWarningConfig(),
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

  @Test
  void editBudget() {
    ResponseEntity<String> result = test.editTarget(1, Map.of("income", 500L));

    verify(database).getCurrentUser();
    assertNotNull(result.getBody());
    assertEquals("Targets successfully edited!", result.getBody());
  }

  @Test
  void editBudgetFailedToFindCategoryToEdit() {
    ResponseEntity<String> result = test.editTarget(1, Map.of("randomCategory", 500L));

    verify(database).getCurrentUser();
    assertEquals(ResponseEntity.badRequest().body("Some targets were invalid!"), result);
  }

  @Test
  void editBudgetNotLoggedIn() {
    ResponseEntity<String> result = test.editTarget(2, Map.of("category", 500L));

    verify(database).getCurrentUser();
    assertEquals(ResponseEntity.internalServerError().build(), result);
  }

  @Test
  void editBudgetRuntimeException() {
    when(database.getCurrentUser()).thenThrow(RuntimeException.class);

    ResponseEntity<String> result = test.editTarget(1, Map.of("category", 500L));

    verify(database).getCurrentUser();
    assertEquals(ResponseEntity.internalServerError().build(), result);
  }
}