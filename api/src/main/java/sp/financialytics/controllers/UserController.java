package sp.financialytics.controllers;

import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.User;

@RestController
@RequestMapping("user")
public class UserController {
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
