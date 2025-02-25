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
            new Transaction("1-1", LocalDate.of(2025, 2, 19), "No Description Set.", "Not yet implemented.", 118),
            new Transaction("1-2", LocalDate.of(2025, 2, 24), "No Description Set.", "income", 500)
    );
  }

  @BeforeEach
  void setUp() {
    try {
      database = spy(Database.load(new File("src/test/resources/working-test-db.json")));
    } catch (IOException e) {
      fail(e.getMessage());
    }
    test = new TransactionController(database, mock()); // we don't want to actually edit test files
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
  void getAllTransactionsServerError() {
    ResponseEntity<List<Transaction>> result = test.getAllTransactions(2);

    assertEquals(ResponseEntity.internalServerError().build(), result);
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
  void getMoneyInTransactionsServerError() {
    ResponseEntity<List<Transaction>> result = test.getMoneyInTransactions(2);

    assertEquals(ResponseEntity.internalServerError().build(), result);
  }

  @Test
  void addTransaction() {
    try {
      Transaction addedTransaction = new Transaction(null, LocalDate.now(), "description", "category", 100);
      doNothing().when(database).update(any(File.class)); // prevents error from targeting main db in test

      ResponseEntity<String> result = test.addTransaction(addedTransaction);

      verify(database).update(any(File.class));
      assertEquals("Transaction #1-3 added successfully!", result.getBody());
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

  @Test
  void editTransaction() {
    try {
      Transaction transactionToEdit = createTestTransactions().get(0);
      transactionToEdit.setAmount(500);
      doNothing().when(database).update(any(File.class));

      ResponseEntity<String> result = test.editTransaction(transactionToEdit);

      verify(database).update(any(File.class));
      assertEquals("Transaction #1-0 edited successfully!", result.getBody());
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
    transactionToEdit.setAmount(500);
    transactionToEdit.setId("1-3");

    ResponseEntity<String> result = test.editTransaction(transactionToEdit);

    assertEquals("Transaction does not exist.", result.getBody());
  }

  @Test
  void editTransactionEmptyTransactionList() {
    database.getCurrentUser().setTransactions(new ArrayList<>());
    Transaction transactionToEdit = createTestTransactions().get(0);
    transactionToEdit.setAmount(500);

    ResponseEntity<String> result = test.editTransaction(transactionToEdit);

    assertEquals("Transaction list is empty.", result.getBody());
  }

  @Test
  void editTransactionNullId() {
    Transaction testTransaction = createTestTransactions().get(0);
    testTransaction.setId(null);

    ResponseEntity<String> result = test.editTransaction(testTransaction);

    assertEquals("Transaction object is null.", result.getBody());
  }

  @Test
  void editTransactionNull() {
    ResponseEntity<String> result = test.editTransaction(null);

    assertEquals("Transaction object is null.", result.getBody());
  }
}