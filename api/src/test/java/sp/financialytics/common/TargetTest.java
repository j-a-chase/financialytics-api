package sp.financialytics.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TargetTest {
  Target test;

  @BeforeEach
  void setUp() {
    test = new Target();
  }

  @Test
  void id() {
    test.setId(1);

    assertEquals(1, test.getId());
  }

  @Test
  void name() {
    test.setName("test");

    assertEquals("test", test.getName());
  }

  @Test
  void amount() {
    test.setAmount(100L);

    assertEquals(100L, test.getAmount());
  }

  @Test
  void included() {
    test.setIncluded(true);

    assertEquals(true, test.getIncluded());
  }

  @Test
  void compareToEqual() {
    test.setName("test");
    Target other = new Target(2, "test", 100L, true);

    assertEquals(0, test.compareTo(other));
    assertEquals(0, other.compareTo(test));
  }

  @Test
  void compareToNotEqual() {
    test.setName("test");
    Target other = new Target(2, "unknown", 100L, true);

    assertEquals(-1, test.compareTo(other));
    assertEquals(1, other.compareTo(test));
  }
}
