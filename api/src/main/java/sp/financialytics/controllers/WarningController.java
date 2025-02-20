package sp.financialytics.controllers;

import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Warning;

import java.util.List;

@RestController
@RequestMapping("warnings")
public class WarningController {
  @PostMapping("initialize")
  public String initializeUserWarningConfig(@RequestParam Integer userId) {
    return "Successfully initialized user warning config.";
  }

  @PostMapping("update")
  public String updateWarningSettings(@RequestParam Integer userId, @RequestBody Warning warning) {
    return "Successfully updated user warning configuration.";
  }

  @GetMapping("retrieve")
  public List<Warning> getUserWarnings(@RequestParam Integer userId) {
    return List.of(Warning.builder().build());
  }
}
