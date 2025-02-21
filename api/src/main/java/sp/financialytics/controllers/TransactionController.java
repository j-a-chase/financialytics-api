package sp.financialytics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Database;
import sp.financialytics.common.Transaction;
import sp.financialytics.common.User;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("transaction")
public class TransactionController {
  private static final Logger LOG = LoggerFactory.getLogger(TransactionController.class);

  private final Database database;
  private final User currentUser;

  public TransactionController(@Autowired Database database) {
    this.database = database;
    this.currentUser = database.getCurrentUser();
  }

  @GetMapping("all")
  public ResponseEntity<List<Transaction>> getAllTransactions(@RequestParam("uid") Integer uid) {
    ResponseEntity<List<Transaction>> response = ResponseEntity.internalServerError().build();
    if (currentUser.getId().equals(uid)) {
      response = ResponseEntity.ok(currentUser.getTransactions());
    }
    return response;
  }

  private boolean isNullTransaction(Transaction transaction) {
    return isNull(transaction) || (isNull(transaction.getDate()) && isNull(transaction.getAmount()));
  }

  @PostMapping("add")
  public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) {
    ResponseEntity<String> response = ResponseEntity.internalServerError().body("Error adding transaction.");
    LOG.info("Adding transaction: {}", transaction);

    try {
      if (isNullTransaction(transaction)) {
        throw new IOException("Transaction object is null.");
      }

      // set the transaction id
      List<Transaction> userTransactions = currentUser.getTransactions();
      String previousTid = userTransactions.get(userTransactions.size() - 1).getId();
      Integer tid = Integer.parseInt(previousTid.substring(2)) + 1;
      transaction.setId(String.format("%s-%s", currentUser.getId(), tid));

      // save to database
      userTransactions.add(transaction);
      database.update();
      String responseText = String.format("Transaction #%s added successfully!", transaction.getId());
      response = ResponseEntity.ok(responseText);
    } catch (IOException e) {
      LOG.error("Exception while updating database!", e);
    }

    return response;
  }

  @PostMapping("update")
  public String updateTransaction(@RequestParam("uid") Integer uid, @RequestParam("tid") Integer transactionId,
                                  @RequestBody Transaction transaction) {
    return "Updated transaction!";
  }
}
