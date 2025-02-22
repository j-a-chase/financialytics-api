package sp.financialytics.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sp.financialytics.common.Database;
import sp.financialytics.common.Transaction;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionControllerTest {
  TransactionController test;

  Database database;

  private List<Transaction> createTestTransactions() {
    return List.of(
            new Transaction("1-0", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 117),
            new Transaction("1-1", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 118)
    );
  }

  @BeforeEach
  void setUp() {
    try {
      database = spy(Database.load(new File("src/test/resources/working-test-db.json")));
    } catch (IOException e) {
      fail(e.getMessage());
    }
    test = new TransactionController(database);
  }

  @Test
  void getAllTransactions() {
    ResponseEntity<List<Transaction>> result = test.getAllTransactions(1);

    List<Transaction> body = result.getBody();
    assertNotNull(body);
    assertThat(body).size().isEqualTo(2);
    assertEquals(createTestTransactions().get(0), body.get(0));
    assertEquals(createTestTransactions().get(1), body.get(1));
  }

  @Test
  void getAllTransactionsServerError() {
    ResponseEntity<List<Transaction>> result = test.getAllTransactions(2);

    assertEquals(ResponseEntity.internalServerError().build(), result);
  }

  @Test
  void addTransaction() {
    try {
      Transaction addedTransaction = new Transaction(null, LocalDate.now(), "description", "category", 100);
      doNothing().when(database).update(any(File.class)); // prevents error from targeting main db in test

      ResponseEntity<String> result = test.addTransaction(addedTransaction);

      verify(database).update(any(File.class));
      assertEquals("Transaction #1-2 added successfully!", result.getBody());
      List<Transaction> expected = database.getCurrentUser().getTransactions();
      assertThat(expected).size().isEqualTo(3);
      assertEquals(expected.get(0), createTestTransactions().get(0));
      assertEquals(expected.get(1), createTestTransactions().get(1));
      addedTransaction.setId("1-2");
      assertEquals(expected.get(2), addedTransaction);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void addTransactionEmptyTransactionList() {
    try {
      database.getUsers().get(0).setTransactions(new ArrayList<>());
      Transaction addedTransaction = new Transaction(null, LocalDate.now(), "description", "category", 100);
      doNothing().when(database).update(any(File.class)); // prevents error from targeting main db in test

      ResponseEntity<String> result = test.addTransaction(addedTransaction);

      verify(database).update(any(File.class));
      assertEquals("Transaction #1-0 added successfully!", result.getBody());
      List<Transaction> expected = database.getCurrentUser().getTransactions();
      assertThat(expected).size().isEqualTo(1);
      addedTransaction.setId("1-0");
      assertEquals(expected.get(0), addedTransaction);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void addTransactionNullTransaction() {
    ResponseEntity<String> result = test.addTransaction(null);

    assertEquals("Transaction object is null.", result.getBody());
  }

  @Test
  void addTransactionNullDate() {
    ResponseEntity<String> result = test.addTransaction(new Transaction(null, null, "description", "category", 100));

    assertEquals("Transaction object is null.", result.getBody());
  }

  @Test
  void addTransactionNullAmount() {
    ResponseEntity<String> result = test.addTransaction(new Transaction(null, LocalDate.now(), "test", "test", null));

    assertEquals("Transaction object is null.", result.getBody());
  }
}