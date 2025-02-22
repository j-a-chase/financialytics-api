package sp.financialytics.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningTest {
  Warning test;

  @BeforeEach
  void setUp() {
    test = new Warning("null", false, false);
  }

  @Test
  void message() {
    test.setMessage("message");

    assertEquals("message", test.getMessage());
  }

  @Test
  void enabled() {
    test.setEnabled(true);

    assertTrue(test.isEnabled());
  }

  @Test
  void dismissed() {
    test.setDismissed(true);

    assertTrue(test.isDismissed());
  }
}
