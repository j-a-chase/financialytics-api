package sp.financialytics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Database;
import sp.financialytics.common.Transaction;
import sp.financialytics.common.User;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;

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

  @GetMapping("all")
  public ResponseEntity<List<Transaction>> getAllTransactions(@RequestParam("uid") Integer uid) {
    ResponseEntity<List<Transaction>> response = ResponseEntity.internalServerError().build();
    LOG.info("Retrieving transactions for user: {}", uid);

    if (currentUser.getId().equals(uid)) {
      response = ResponseEntity.ok(currentUser.getTransactions());
      LOG.info("Transactions retrieved!");
    }

    return response;
  }

  @GetMapping("detail")
  public ResponseEntity<Transaction> getTransactionDetails(@RequestParam("tid") String tid) {
    ResponseEntity<Transaction> response = ResponseEntity.internalServerError().build();
    LOG.info("Retrieving {} for user: {}", tid, currentUser.getId());

    if (currentUser.getId().equals(Integer.parseInt(tid.split("-")[0]))) {
      response = ResponseEntity.ok(currentUser.getTransactions().get(Integer.parseInt(tid.split("-")[1])));
      LOG.info("Transaction details retrieved!");
    }

    return response;
  }

  @GetMapping("money-in")
  public ResponseEntity<List<Transaction>> getMoneyInTransactions(@RequestParam("uid") Integer uid) {
    ResponseEntity<List<Transaction>> response = ResponseEntity.internalServerError().build();
    LOG.info("Retrieving money-in transactions for user: {}", uid);

    if (currentUser.getId().equals(uid)) {
      response = ResponseEntity.ok(currentUser.getTransactions()
              .stream().filter(transaction -> transaction.getCategory().equals("income"))
              .toList());
      LOG.info("Money-in transactions retrieved!");
    }

    return response;
  }

  private boolean isNullTransaction(Transaction transaction) {
    return isNull(transaction) || isNull(transaction.getDate()) || isNull(transaction.getAmount());
  }

  private String setNextTid(List<Transaction> userTransactions) {
    if (userTransactions.isEmpty()) {
      return "1-0";
    }

    String previousTid = userTransactions.get(userTransactions.size() - 1).getId();
    Integer tid = Integer.parseInt(previousTid.substring(2)) + 1;
    return String.format("%s-%s", currentUser.getId(), tid);
  }

  @PostMapping("add")
  public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) {
    ResponseEntity<String> response;
    LOG.info("Adding transaction: {}", transaction);

    try {
      if (isNullTransaction(transaction)) {
        throw new IOException("Transaction object is null.");
      }

      // set the transaction id
      List<Transaction> userTransactions = currentUser.getTransactions();
      transaction.setId(setNextTid(userTransactions));

      // save to database
      userTransactions.add(transaction);
      database.update(databaseFile);
      String responseText = String.format("Transaction #%s added successfully!", transaction.getId());
      response = ResponseEntity.ok(responseText);
      LOG.info("Transaction #{} added successfully!", transaction.getId());
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Exception while updating database!", e);
    }

    return response;
  }

  @PostMapping("edit")
  public ResponseEntity<String> editTransaction(@RequestBody Transaction transaction) {
    ResponseEntity<String> response;
    LOG.info("Editing transaction: {}", transaction);

    try {
      if (isNull(transaction) || isNull(transaction.getId())) {
        throw new IOException("Transaction object is null.");
      }

      List<Transaction> userTransactions = currentUser.getTransactions();
      if (userTransactions.isEmpty()) {
        throw new RuntimeException("Transaction list is empty.");
      }

      boolean edited = false;
      for (int i = 0; i < userTransactions.size(); i++) {
        if (!edited && userTransactions.get(i).getId().equals(transaction.getId())) {
          userTransactions.set(i, transaction);
          edited = true;
        }
      }

      if (!edited) {
        throw new RuntimeException("Transaction does not exist.");
      }

      database.update(databaseFile);
      response = ResponseEntity.ok(String.format("Transaction #%s edited successfully!", transaction.getId()));
      LOG.info("Transaction #{} edited successfully!", transaction.getId());
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("IOException while updating database!", e);
    } catch (RuntimeException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("RuntimeException while updating database!", e);
    }

    return response;
  }
}
