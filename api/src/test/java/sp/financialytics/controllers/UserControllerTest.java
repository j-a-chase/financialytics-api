package sp.financialytics.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sp.financialytics.common.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
  private final int UID = 1;

  UserController test;
  Database database;

  @BeforeEach
  void setUp() {
    try {
      database = spy(Database.load(new File("src/test/resources/working-test-db.json")));
    } catch (IOException e) {
      fail(e.getMessage());
    }

    test = new UserController(database, mock()); // we don't actually want to modify the test db file
  }

  private List<Transaction> createTestTransactions() {
    return List.of(
            new Transaction("1-0", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 117L,
                            "No notes."),
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
    return new User(UID, "dev", "adminDev", "example@gmail.com", LeniencyLevel.NORMAL, createTestTransactions(),
                    createTestWarningConfig(), createTestTargetsMap());
  }

  @Test
  void constructor() {
    try {
      Database d = mock();
      when(d.getCurrentUser()).thenThrow(new RuntimeException("Runtime error!"));

      test = new UserController(d, mock());

      fail("Runtime error did not rethrow.");
    } catch (RuntimeException e) {
      assertEquals("Runtime error!", e.getMessage());
    }
  }

  @Test
  void initialize() {
    ResponseEntity<User> result = test.initialize(UID);

    assertNotNull(result.getBody());
    assertEquals(createTestUser(), result.getBody());
  }

  @Test
  void getTargets() {
    ResponseEntity<Map<String, Long>> result = test.getTargets(UID);

    assertNotNull(result.getBody());
    assertEquals(createTestTargetsMap(), result.getBody());
  }

  @Test
  void getTargetsNotLoggedIn() {
    ResponseEntity<Map<String, Long>> result = test.getTargets(2);

    assertEquals(ResponseEntity.badRequest().body(new HashMap<>()), result);
  }

  @Test
  void editTarget() {
    try {
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.editTarget(UID, Map.of("income", 500L));

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Targets successfully edited!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTargetFailedToFindCategoryToEdit() {
    try {
      ResponseEntity<String> result = test.editTarget(UID, Map.of("randomCategory", 500L));

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.badRequest().body("Target edit mismatch!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTargetNotLoggedIn() {
    try {
      ResponseEntity<String> result = test.editTarget(2, Map.of("category", 500L));

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.badRequest().body("User id mismatch!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTargetIOException() {
    try {
      doThrow(new IOException("ioexception")).when(database).update(any(File.class));

      ResponseEntity<String> result = test.editTarget(UID, Map.of("income", 500L));

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(createTestTargetsMap(), database.getCurrentUser().getTargets());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void updateTargets() {
    try {
      Map<String, Long> targetsMap = Map.of("category", 500L);
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.updateTargets(UID, targetsMap);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Targets successfully updated!"), result);
      assertEquals(targetsMap, database.getCurrentUser().getTargets());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void updateTargetsNotLoggedIn() {
    try {
      ResponseEntity<String> result = test.updateTargets(2, Map.of("category", 500L));

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.badRequest().body("User id mismatch!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void updateTargetsIOException() {
    try {
      doThrow(new IOException("ioexception")).when(database).update(any(File.class));

      ResponseEntity<String> result = test.updateTargets(UID, Map.of("category", 500L));

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(createTestTargetsMap(), database.getCurrentUser().getTargets());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editLeniencyLevel() {
    try {
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.editLeniencyLevel(UID, LeniencyLevel.LENIENT);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Leniency level successfully updated!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editLeniencyLevelNotLoggedIn() {
    try {
      ResponseEntity<String> result = test.editLeniencyLevel(2, LeniencyLevel.LENIENT);

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.badRequest().body("User id mismatch!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editLeniencyLevelIOException() {
    try {
      doThrow(new IOException("ioexception")).when(database).update(any(File.class));

      ResponseEntity<String> result = test.editLeniencyLevel(UID, LeniencyLevel.LENIENT);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(LeniencyLevel.NORMAL, database.getCurrentUser().getBudgetLeniency());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}