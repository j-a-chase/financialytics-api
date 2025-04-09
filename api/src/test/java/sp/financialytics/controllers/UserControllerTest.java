package sp.financialytics.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sp.financialytics.common.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

  private List<Target> createTestTargets() {
    return List.of(new Target(0, "income", 200000L, false), new Target(1, "food", 20000L, true),
                   new Target(2, "living", 20001L, true), new Target(3, "entertainment", 20002L, true),
                   new Target(4, "supplies", 20003L, true), new Target(5, "education", 20004L, true),
                   new Target(6, "other", 20005L, true));
  }

  private User createTestUser() {
    return new User(UID, "dev", "adminDev", "example@gmail.com", LeniencyLevel.NORMAL, createTestTransactions(),
                    createTestWarningConfig(), createTestTargets());
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
    ResponseEntity<List<Target>> result = test.getTargets(UID);

    assertNotNull(result.getBody());
    assertEquals(createTestTargets(), result.getBody());
  }

  @Test
  void getTargetsNotLoggedIn() {
    ResponseEntity<List<Target>> result = test.getTargets(2);

    assertEquals(ResponseEntity.badRequest().body(List.of()), result);
  }

  private List<Target> createTestTarget(Integer id) {
    return List.of(new Target(id, "food", 100L, true));
  }

  @Test
  void editTarget() {
    try {
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.editTarget(UID, createTestTarget(0));

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Targets successfully edited!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTargetFailedToFindCategoryToEdit() {
    try {
      List<Target> testTarget = createTestTarget(0);
      testTarget.get(0).setName("random");

      ResponseEntity<String> result = test.editTarget(UID, testTarget);

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.badRequest().body("Target edit mismatch!"), result);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTargetBackupCloneException() {
    try {
      User mockUser = mock();
      Target mockTarget = mock();
      List<Target> testTarget = List.of(mockTarget);
      List<User> mockUsers = new ArrayList<>();
      mockUsers.add(mockUser);
      database.setUsers(mockUsers);
      test = new UserController(database, mock());
      when(mockUser.getTargets()).thenReturn(testTarget);
      when(mockTarget.clone()).thenThrow(new CloneNotSupportedException("Clone error!"));

      ResponseEntity<String> result = test.editTarget(UID, testTarget);

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("Clone error!"), result);
    } catch (CloneNotSupportedException | IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTargetNotLoggedIn() {
    try {
      ResponseEntity<String> result = test.editTarget(2, createTestTarget(0));

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

      ResponseEntity<String> result = test.editTarget(UID, createTestTarget(0));

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(createTestTargets(), database.getCurrentUser().getTargets());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void updateTargets() {
    try {
      List<Target> testTarget = createTestTarget(null);
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.updateTargets(UID, testTarget);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Targets successfully updated!"), result);
      assertEquals(testTarget, database.getCurrentUser().getTargets());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void updateTargetsBackupCloneException() {
    try {
      User mockUser = mock();
      Target mockTarget = mock();
      List<Target> testTarget = List.of(mockTarget);
      List<User> mockUsers = new ArrayList<>();
      mockUsers.add(mockUser);
      database.setUsers(mockUsers);
      test = new UserController(database, mock());
      when(mockUser.getTargets()).thenReturn(testTarget);
      when(mockTarget.clone()).thenThrow(new CloneNotSupportedException("Clone error!"));

      ResponseEntity<String> result = test.updateTargets(UID, testTarget);

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("Clone error!"), result);
    } catch (CloneNotSupportedException | IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void updateTargetsNotLoggedIn() {
    try {
      ResponseEntity<String> result = test.updateTargets(2, createTestTarget(null));

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

      ResponseEntity<String> result = test.updateTargets(UID, createTestTarget(null));

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(createTestTargets(), database.getCurrentUser().getTargets());
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