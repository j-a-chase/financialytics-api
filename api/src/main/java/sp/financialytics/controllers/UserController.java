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

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  private final Database database;

  public UserController(@Autowired Database database) {
    this.database = database;
  }

  @GetMapping("initialize")
  public ResponseEntity<User> initialize(@RequestParam("uid") Integer uid) {
    ResponseEntity<User> response = ResponseEntity.internalServerError().build();
    LOG.info("Retrieving user: {}", uid);
    try {
      response = ResponseEntity.ok(database.getCurrentUser());
    } catch (IndexOutOfBoundsException e) {
      LOG.error("Database not properly configured: ", e);
    }

    return response;
  }

  @PostMapping("target")
  public ResponseEntity<String> editTarget(@RequestParam("uid") Integer uid, @RequestBody Map<String, Long> categories) {
    ResponseEntity<String> response = ResponseEntity.internalServerError().build();
    LOG.info("Editing targets: {}", categories);

    try {
      User currentUser = database.getCurrentUser();
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch");
      }

      Map<String, Long> targets = currentUser.getTargets();
      for (String category : categories.keySet()) {
        if (!targets.containsKey(category)) {
          LOG.warn("Category does not exist!");
          response = ResponseEntity.badRequest().body("Some targets were invalid!");
          throw new DataIntegrityViolationException("Target edit mismatch!");
        }

        targets.put(category, categories.get(category));
      }

      LOG.info("Targets successfully edited!");
      response = ResponseEntity.ok("Targets successfully edited!");
    } catch (DataIntegrityViolationException e) {
      LOG.error("", e);
    } catch (RuntimeException e) {
      LOG.error("Error editing targets for: {}", uid, e);
    }

    return response;
  }

  @PostMapping("leniency")
  public ResponseEntity<String> editLeniencyLevel(@RequestParam("uid") Integer uid,
                                                  @RequestBody LeniencyLevel leniency) {
    ResponseEntity<String> response = ResponseEntity.internalServerError().build();
    LOG.info("Editing leniency level: {}", leniency);

    try {
      User currentUser = database.getCurrentUser();
      if (!currentUser.getId().equals(uid)) {
        throw new DataIntegrityViolationException("User id mismatch");
      }

      currentUser.setBudgetLeniency(leniency);
      LOG.info("Leniency level updated!");
      response = ResponseEntity.ok("Leniency level successfully updated!");
    } catch (DataIntegrityViolationException e) {
      LOG.error("", e);
    }

    return response;
  }
}
