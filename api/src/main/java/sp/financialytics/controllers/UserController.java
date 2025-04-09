package sp.financialytics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Database;
import sp.financialytics.common.LeniencyLevel;
import sp.financialytics.common.Target;
import sp.financialytics.common.User;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * All needed endpoints for working with the user and the user's information
 */
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
  // As of right now: returns the current user (which is the only user)
  @GetMapping("initialize")
  public ResponseEntity<User> initialize(@RequestParam("uid") Integer uid) {
    LOG.info("Retrieving user: {}", uid);

    return ResponseEntity.ok(currentUser);
  }

  // retrieves the targets for the given uid
  @GetMapping("targets")
  public ResponseEntity<List<Target>> getTargets(@RequestParam("uid") Integer uid) {
    ResponseEntity<List<Target>> response;
    LOG.info("Retrieving targets for user #{}", uid);

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      response = ResponseEntity.ok(currentUser.getTargets());
      LOG.info("Targets retrieved for user #{}", uid);
    } catch (DataIntegrityViolationException e) {
      response = ResponseEntity.badRequest().body(List.of());
    }

    return response;
  }

  // deep copies a list of Targets
  private List<Target> backupTargetList(List<Target> targets) throws CloneNotSupportedException {
    List<Target> backupTargets = new ArrayList<>();
    for (Target target : targets) {
      backupTargets.add(target.clone());
    }

    return backupTargets;
  }

  // edits provided targets within the target list
  @PostMapping("target")
  public ResponseEntity<String> editTarget(@RequestParam("uid") Integer uid, @RequestBody List<Target> newTargets) {
    ResponseEntity<String> response;
    List<Target> previousTargets;
    try {
      previousTargets = backupTargetList(currentUser.getTargets());
      LOG.info("Editing targets: {}", newTargets);
    } catch (CloneNotSupportedException e) {
      LOG.error(e.getMessage());
      return ResponseEntity.internalServerError().body(e.getMessage());
    }

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      List<Target> targets = currentUser.getTargets();
      // creates a set of the current target names
      Set<String> currentCategories = Set.of(targets.stream().map(Target::getName).toArray(String[]::new));

      newTargets.forEach(target -> {
        if (!currentCategories.contains(target.getName())) {
          LOG.warn("Category does not exist!");
          throw new DataIntegrityViolationException("Target edit mismatch!");
        }

        targets.get(target.getId()).setAmount(target.getAmount());
      });

      // both editTarget and editLeniencyLevel are called at the same time, resulting in this being needed
      synchronized (database) {
        database.update(databaseFile);
      }

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

  // updates entire target list, allowing for addition, removal, or simple editing of targets
  @PostMapping("targets/update")
  public ResponseEntity<String> updateTargets(@RequestParam("uid") Integer uid,
                                              @RequestBody List<Target> newTargets) {
    ResponseEntity<String> response;
    List<Target> previousTargets;
    try {
      previousTargets = backupTargetList(currentUser.getTargets());
      LOG.info("Updating targets: {}", newTargets);
    } catch (CloneNotSupportedException e) {
      LOG.error(e.getMessage());
      return ResponseEntity.internalServerError().body(e.getMessage());
    }

    try {
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch!");
      }

      currentUser.setTargets(newTargets);
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

  // changes the leniency level based on the user selection
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
      // both editTarget and editLeniencyLevel are called at the same time, resulting in this being needed
      synchronized (database) {
        database.update(databaseFile);
      }

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
