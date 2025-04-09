package sp.financialytics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Database;
import sp.financialytics.common.Transaction;
import sp.financialytics.common.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * All needed endpoints for working with Transactions.
*/
@RestController
@RequestMapping("transaction")
public class TransactionController {
  private static final Logger LOG = LoggerFactory.getLogger(TransactionController.class);

  private final Database database;
  private final File databaseFile;
  private final User currentUser;

  public TransactionController(@Autowired Database database, @Autowired File databaseFile) {
    this.database = database;
    this.databaseFile = databaseFile;
    this.currentUser = database.getCurrentUser();
  }

  // retrieves all transactions for a specific user
  @GetMapping("all")
  public ResponseEntity<List<Transaction>> getAllTransactions(@RequestParam("uid") Integer uid) {
    ResponseEntity<List<Transaction>> response;
    LOG.info("Retrieving transactions for user: {}", uid);

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      response = ResponseEntity.ok(currentUser.getTransactions());
      LOG.info("Transactions retrieved!");
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().build();
      LOG.error("", e);
    }

    return response;
  }

  // retrieves transaction information for a single transaction
  @GetMapping("detail")
  public ResponseEntity<Transaction> getTransactionDetails(@RequestParam("tid") String tid) {
    ResponseEntity<Transaction> response;
    LOG.info("Retrieving {} for user: {}", tid, currentUser.getId());

    try {
      if (!currentUser.getId().equals(Integer.parseInt(tid.split("-")[0]))) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      response = ResponseEntity.ok(currentUser.getTransactions().get(Integer.parseInt(tid.split("-")[1])));
      LOG.info("Transaction details retrieved!");
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().build();
      LOG.error("", e);
    }

    return response;
  }

  // retrieves all transactions under the target 'income'
  @GetMapping("money-in")
  public ResponseEntity<List<Transaction>> getMoneyInTransactions(@RequestParam("uid") Integer uid) {
    ResponseEntity<List<Transaction>> response;
    LOG.info("Retrieving money-in transactions for user: {}", uid);

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      response = ResponseEntity.ok(currentUser.getTransactions()
              .stream()
              .filter(transaction -> transaction.getCategory().equals("income"))
              .toList());
      LOG.info("Money-in transactions retrieved!");
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().build();
      LOG.error("", e);
    }

    return response;
  }

  // calculates the next tid value and returns it
  private String setNextTid(List<Transaction> userTransactions) {
    if (userTransactions.isEmpty()) {
      return "1-0";
    }

    String previousTid = userTransactions.get(userTransactions.size() - 1).getId();
    Integer tid = Integer.parseInt(previousTid.split("-")[1]) + 1;
    return String.format("%s-%s", currentUser.getId(), tid);
  }

  // adds a transaction to the transaction list
  @PostMapping("add")
  public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) {
    ResponseEntity<String> response;
    List<Transaction> previousTransactions = new ArrayList<>(currentUser.getTransactions());
    LOG.info("Adding transaction: {}", transaction);

    try {
      // only scenarios where we're worried about a null value
      if (isNull(transaction) || isNull(transaction.getDate()) || isNull(transaction.getAmount())) {
        throw new DataIntegrityViolationException("Transaction object is null.");
      }

      // might want to add user validation here.

      // set the transaction id
      List<Transaction> userTransactions = currentUser.getTransactions();
      transaction.setId(setNextTid(userTransactions));

      // save to database
      userTransactions.add(transaction);
      database.update(databaseFile);

      response = ResponseEntity.ok(String.format("Transaction #%s added successfully!", transaction.getId()));
      LOG.info("Transaction #{} added successfully!", transaction.getId());
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().body(e.getMessage());
      LOG.error("Exception while updating database!", e);
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Exception while updating database!", e);
      currentUser.setTransactions(previousTransactions); // restore if database update failed
    }

    return response;
  }

  // edits a single transaction within the transaction list
  @PostMapping("edit")
  public ResponseEntity<String> editTransaction(@RequestBody Transaction transaction) {
    ResponseEntity<String> response;
    List<Transaction> previousTransactions = new ArrayList<>(currentUser.getTransactions());
    LOG.info("Editing transaction: {}", transaction);

    try {
      if (isNull(transaction) || isNull(transaction.getId())) {
        throw new IOException("Transaction object is null.");
      }

      List<Transaction> userTransactions = currentUser.getTransactions();
      if (userTransactions.isEmpty()) {
        throw new RuntimeException("Transaction list is empty.");
      }

      int tid = Integer.parseInt(transaction.getId().split("-")[1]);
      if (isNull(transaction.getNotes()) && !userTransactions.get(tid).getNotes().isEmpty()) {
        transaction.setNotes(userTransactions.get(tid).getNotes());
      }

      if (isNull(transaction.getNotes())) {
        transaction.setNotes("");
      }

      userTransactions.set(tid, transaction);
      database.update(databaseFile);

      response = ResponseEntity.ok(String.format("Transaction #%s edited successfully!", transaction.getId()));
      LOG.info("Transaction #{} edited successfully!", transaction.getId());
    } catch (DataIntegrityViolationException | IndexOutOfBoundsException e) {
      response = ResponseEntity.badRequest().body(e.getMessage());
      LOG.error("Exception while updating database: ", e);
    } catch (RuntimeException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Database exception while editing transaction: ", e);
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Database exception while editing transaction: ", e);
      currentUser.setTransactions(previousTransactions); // restore if database update failed
    }

    return response;
  }

  // removes a transaction from the transaction list
  @DeleteMapping("delete")
  public ResponseEntity<String> deleteTransaction(@RequestParam String tid) {
    ResponseEntity<String> response;
    List<Transaction> previousTransactions = new ArrayList<>(currentUser.getTransactions());
    LOG.info("Deleting transaction: {}", tid);

    try {
      if (!currentUser.getId().equals(Integer.parseInt(tid.split("-")[0]))) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      currentUser.getTransactions().remove(Integer.parseInt(tid.split("-")[1]));
      database.update(databaseFile);

      LOG.info("Transaction {} deleted successfully!", tid);
      response = ResponseEntity.ok(String.format("Transaction %s deleted successfully!", tid));
    } catch (IndexOutOfBoundsException | DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().body(e.getMessage());
      LOG.error("Exception while deleting transaction #{} from database: ", tid, e);
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Database exception while deleting transaction: ", e);
      currentUser.setTransactions(previousTransactions); // restore if database update failed
    }

    return response;
  }
}
