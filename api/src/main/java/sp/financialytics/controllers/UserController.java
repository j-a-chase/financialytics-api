package sp.financialytics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Database;
import sp.financialytics.common.LeniencyLevel;
import sp.financialytics.common.User;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  private final Database database;
  private final File databaseFile;
  private final User currentUser;

  public UserController(@Autowired Database database, @Autowired File databaseFile) {
    this.database = database;
    this.databaseFile = databaseFile;

    try {
      this.currentUser = database.getCurrentUser();
    } catch (RuntimeException e) {
      LOG.error(e.getMessage());
      LOG.error("Critical Failure: Database not properly configured.");
      throw e;
    }
  }

  // This endpoint will need adjusting when scaling to handle more than a single local user.
  @GetMapping("initialize")
  public ResponseEntity<User> initialize(@RequestParam("uid") Integer uid) {
    LOG.info("Retrieving user: {}", uid);

    return ResponseEntity.ok(currentUser);
  }

  @PostMapping("target")
  public ResponseEntity<String> editTarget(@RequestParam("uid") Integer uid, @RequestBody Map<String, Long> categories) {
    ResponseEntity<String> response;
    Map<String, Long> previousTargets = new HashMap<>(currentUser.getTargets());
    LOG.info("Editing targets: {}", categories);

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      Map<String, Long> targets = currentUser.getTargets();
      for (String category : categories.keySet()) {
        if (!targets.containsKey(category)) {
          LOG.warn("Category does not exist!");
          throw new DataIntegrityViolationException("Target edit mismatch!");
        }

        targets.put(category, categories.get(category));
      }

      database.update(databaseFile);

      LOG.info("Targets successfully edited!");
      response = ResponseEntity.ok("Targets successfully edited!");
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().body(e.getMessage());
      LOG.error("Bad Request: ", e);
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Error editing targets for: {}", uid, e);
      currentUser.setTargets(previousTargets); // restore if database update failed
    }

    return response;
  }

  @PostMapping("targets/update")
  public ResponseEntity<String> updateTargets(@RequestParam("uid") Integer uid,
                                              @RequestBody Map<String, Long> categories) {
    ResponseEntity<String> response;
    Map<String, Long> previousTargets = new HashMap<>(currentUser.getTargets());
    LOG.info("Updating targets: {}", categories);

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      currentUser.setTargets(categories);
      database.update(databaseFile);

      LOG.info("Targets successfully updated!");
      response = ResponseEntity.ok("Targets successfully updated!");
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().body(e.getMessage());
      LOG.error("", e);
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Error updating targets for: {}", uid, e);
      currentUser.setTargets(previousTargets); // restore if database update failed
    }

    return response;
  }

  @PostMapping("leniency")
  public ResponseEntity<String> editLeniencyLevel(@RequestParam("uid") Integer uid,
                                                  @RequestBody LeniencyLevel leniency) {
    ResponseEntity<String> response;
    LeniencyLevel previousLeniency = currentUser.getBudgetLeniency();
    LOG.info("Editing leniency level: {}", leniency);

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      currentUser.setBudgetLeniency(leniency);
      database.update(databaseFile);

      LOG.info("Leniency level updated!");
      response = ResponseEntity.ok("Leniency level successfully updated!");
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().body(e.getMessage());
      LOG.error("", e);
    } catch (IOException e) {
      response = ResponseEntity.internalServerError().body(e.getMessage());
      LOG.error("Error editing leniency level for: {}", uid, e);
      currentUser.setBudgetLeniency(previousLeniency); // restore if database update failed
    }

    return response;
  }
}
