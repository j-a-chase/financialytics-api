package sp.financialytics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Database;
import sp.financialytics.common.Transaction;
import sp.financialytics.common.User;

import java.io.IOException;
import java.util.List;

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
  public List<Transaction> getAllTransactions(@RequestParam("uid") Integer uid) {
    if (!currentUser.getId().equals(uid)) {
      throw new RuntimeException("Error! User not logged in.");
    }
    return currentUser.getTransactions();
  }

  @PostMapping("add")
  public String addTransaction(@RequestBody Transaction transaction) {
    LOG.info("Adding transaction: {}", transaction);
    try {
      currentUser.getTransactions().add(transaction);
      database.update();
    } catch (IOException e) {
      LOG.error("Exception while updating database!", e);
    }

    return "Success!";
  }

  @PostMapping("update")
  public String updateTransaction(@RequestParam("uid") Integer uid, @RequestParam("tid") Integer transactionId,
                                  @RequestBody Transaction transaction) {
    return "Updated transaction!";
  }
}
