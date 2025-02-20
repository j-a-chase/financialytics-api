package sp.financialytics.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.User;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("user")
public class UserController {
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  private final ObjectMapper objectMapper;
  private final File database;

  public UserController(@Autowired File database) {
    this.objectMapper = new ObjectMapper();
    this.database = database;
  }

  @GetMapping("initialize")
  public User initialize(@RequestParam("uid") Integer uid) {
    LOG.info("Initializing user with uid: {}", uid);
    User user = User.createDefault();

    try {
      JsonNode db = objectMapper.readTree(database);
      user = User.readFromJsonNode(db.get("users").get(0));
      if (!user.getId().equals(uid)) {
        throw new RuntimeException("Requested user not found within database!");
      }
    } catch (IOException e) {
      LOG.warn("IOException while reading database, defaulting user.");
      LOG.error(e.getMessage());
    } catch (RuntimeException e) {
      LOG.error("Exception while reading database: ", e);
      throw e;
    }

    LOG.info("User initialized!");
    return user;
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
