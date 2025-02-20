package sp.financialytics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Database;
import sp.financialytics.common.User;

@RestController
@RequestMapping("user")
public class UserController {
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  private final Database database;

  public UserController(@Autowired Database database) {
    this.database = database;
  }

  @GetMapping("initialize")
  public User initialize(@RequestParam("uid") Integer uid) {
    LOG.info("Retrieving user: {}", uid);
    return database.getCurrentUser();
  }

  @PostMapping("save")
  public String saveUser(@RequestBody User user) {
    return "User saved!";
  }

  @PostMapping("update")
  public String updateUser(@RequestBody User user) {
    return "User updated!";
  }

  @GetMapping("validate")
  public boolean validateUser(@RequestBody User user) {
    return false;
  }
}
