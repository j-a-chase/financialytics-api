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

  @BeforeEach
  void setUp() {
    try {
      database = spy(Database.load(new File("src/test/resources/working-test-db.json")));
    } catch (IOException e) {
      fail(e.getMessage());
    }
    test = new TransactionController(database, mock()); // we don't want to actually edit test files
  }

  private List<Transaction> createTestTransactions() {
    return List.of(
            new Transaction("1-0", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 117L, "No notes."),
            new Transaction("1-1", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 118L, ""),
            new Transaction("1-2", LocalDate.of(2025, 2, 24), "No Description Set.", "income", 500L, "")
    );
  }

  @Test
  void getAllTransactions() {
    ResponseEntity<List<Transaction>> result = test.getAllTransactions(1);

    List<Transaction> body = result.getBody();
    assertNotNull(body);
    assertThat(body).size().isEqualTo(3);
    assertEquals(createTestTransactions().get(0), body.get(0));
    assertEquals(createTestTransactions().get(1), body.get(1));
    assertEquals(createTestTransactions().get(2), body.get(2));
  }

  @Test
  void getAllTransactionsNotLoggedIn() {
    ResponseEntity<List<Transaction>> result = test.getAllTransactions(2);

    assertEquals(ResponseEntity.badRequest().build(), result);
  }

  @Test
  void getTransactionDetails() {
    ResponseEntity<Transaction> result = test.getTransactionDetails("1-0");

    Transaction body = result.getBody();
    assertNotNull(body);
    assertEquals(createTestTransactions().get(0), body);
  }

  @Test
  void getTransactionDetailsNotLoggedIn() {
    ResponseEntity<Transaction> result = test.getTransactionDetails("2-0");

    assertEquals(ResponseEntity.badRequest().build(), result);
  }

  @Test
  void getMoneyInTransactions() {
    ResponseEntity<List<Transaction>> result = test.getMoneyInTransactions(1);

    List<Transaction> body = result.getBody();
    assertNotNull(body);
    assertThat(body).size().isEqualTo(1);
    assertEquals(createTestTransactions().get(2), body.get(0)); // this has the income transaction
  }

  @Test
  void getMoneyInTransactionsNotLoggedIn() {
    ResponseEntity<List<Transaction>> result = test.getMoneyInTransactions(2);

    assertEquals(ResponseEntity.badRequest().build(), result);
  }

  @Test
  void addTransaction() {
    try {
      Transaction addedTransaction = new Transaction(null, LocalDate.now(), "description", "category", 100L, "");
      doNothing().when(database).update(any(File.class)); // prevents error from targeting main db in test

      ResponseEntity<String> result = test.addTransaction(addedTransaction);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Transaction #1-3 added successfully!"), result);
      List<Transaction> expected = database.getCurrentUser().getTransactions();
      assertThat(expected).size().isEqualTo(4);
      assertEquals(expected.get(0), createTestTransactions().get(0));
      assertEquals(expected.get(1), createTestTransactions().get(1));
      assertEquals(expected.get(2), createTestTransactions().get(2));
      addedTransaction.setId("1-3");
      assertEquals(expected.get(3), addedTransaction);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void addTransactionEmptyTransactionList() {
    try {
      database.getCurrentUser().setTransactions(new ArrayList<>());
      Transaction addedTransaction = new Transaction(null, LocalDate.now(), "description", "category", 100L, "");
      doNothing().when(database).update(any(File.class)); // prevents error from targeting main db in test

      ResponseEntity<String> result = test.addTransaction(addedTransaction);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Transaction #1-0 added successfully!"), result);
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

    assertEquals(ResponseEntity.badRequest().body("Transaction object is null."), result);
  }

  @Test
  void addTransactionNullDate() {
    ResponseEntity<String> result = test.addTransaction(new Transaction(null, null, "description", "category", 100L, ""));

    assertEquals(ResponseEntity.badRequest().body("Transaction object is null."), result);
  }

  @Test
  void addTransactionNullAmount() {
    ResponseEntity<String> result = test.addTransaction(new Transaction(null, LocalDate.now(), "test", "test", null, ""));

    assertEquals(ResponseEntity.badRequest().body("Transaction object is null."), result);
  }

  @Test
  void addTransactionIOException() {
    try {
      Transaction addedTransaction = new Transaction(null, LocalDate.now(), "description", "category", 100L, "");
      doThrow(new IOException("ioexception")).when(database).update(any(File.class));

      ResponseEntity<String> result = test.addTransaction(addedTransaction);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(3, database.getCurrentUser().getTransactions().size());
      assertEquals(createTestTransactions(), database.getCurrentUser().getTransactions());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTransaction() {
    try {
      Transaction transactionToEdit = createTestTransactions().get(0);
      transactionToEdit.setAmount(500L);
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.editTransaction(transactionToEdit);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Transaction #1-0 edited successfully!"), result);
      Transaction resultantTransaction = database.getCurrentUser().getTransactions().get(0);
      assertEquals(transactionToEdit.getId(), resultantTransaction.getId());
      assertEquals(transactionToEdit.getAmount(), resultantTransaction.getAmount());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void editTransactionNotInList() {
    Transaction transactionToEdit = createTestTransactions().get(0);
    transactionToEdit.setAmount(500L);
    transactionToEdit.setId("1-3");

    ResponseEntity<String> result = test.editTransaction(transactionToEdit);

    assertEquals(ResponseEntity.badRequest().body("Index 3 out of bounds for length 3"), result);
  }

  @Test
  void editTransactionNullId() {
    Transaction testTransaction = createTestTransactions().get(0);
    testTransaction.setId(null);

    ResponseEntity<String> result = test.editTransaction(testTransaction);

    assertEquals(ResponseEntity.internalServerError().body("Transaction object is null."), result);
  }

  @Test
  void editTransactionNull() {
    ResponseEntity<String> result = test.editTransaction(null);

    assertEquals(ResponseEntity.internalServerError().body("Transaction object is null."), result);
  }

  @Test
  void editTransactionEmptyTransactionList() {
    database.getCurrentUser().setTransactions(new ArrayList<>());
    Transaction transactionToEdit = createTestTransactions().get(0);
    transactionToEdit.setAmount(500L);

    ResponseEntity<String> result = test.editTransaction(transactionToEdit);

    assertEquals(ResponseEntity.internalServerError().body("Transaction list is empty."), result);
  }

  @Test
  void editTransactionIOException() {
    try {
      Transaction transactionToEdit = createTestTransactions().get(0);
      transactionToEdit.setAmount(500L);
      doThrow(new IOException("ioexception")).when(database).update(any(File.class));

      ResponseEntity<String> result = test.editTransaction(transactionToEdit);

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(3, database.getCurrentUser().getTransactions().size());
      assertEquals(createTestTransactions(), database.getCurrentUser().getTransactions());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  private Transaction createTestTransactionForDelete() {
    return new Transaction("1-3", LocalDate.now(), "transaction to be deleted", "category", 100L, "notes");
  }

  @Test
  void deleteTransaction() {
    try {
      List<Transaction> userTransactions = database.getCurrentUser().getTransactions();
      userTransactions.add(createTestTransactionForDelete());
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.deleteTransaction("1-3");

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.ok("Transaction 1-3 deleted successfully!"), result);
      assertEquals(3, userTransactions.size());
      assertEquals(createTestTransactions(), userTransactions);
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  void deleteTransactionNotLoggedIn() {
    try {
      ResponseEntity<String> result = test.deleteTransaction("2-0");

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.badRequest().body("User id mismatch!"), result);
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  void deleteTransactionIndexOutOfBounds() {
    try {
      ResponseEntity<String> result = test.deleteTransaction("1-3");

      verify(database, times(0)).update(any(File.class));
      assertEquals(ResponseEntity.badRequest().body("Index 3 out of bounds for length 3"), result);
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  void deleteTransactionIOException() {
    try {
      List<Transaction> userTransactions = database.getCurrentUser().getTransactions();
      userTransactions.add(createTestTransactionForDelete());
      doThrow(new IOException("ioexception")).when(database).update(any(File.class));

      ResponseEntity<String> result = test.deleteTransaction("1-3");

      verify(database).update(any(File.class));
      assertEquals(ResponseEntity.internalServerError().body("ioexception"), result);
      assertEquals(4, database.getCurrentUser().getTransactions().size());
      List<Transaction> expected = new ArrayList<>(createTestTransactions());
      expected.add(createTestTransactionForDelete());
      assertEquals(expected, database.getCurrentUser().getTransactions());
    } catch (IOException e) {
      fail(e);
    }
  }
}