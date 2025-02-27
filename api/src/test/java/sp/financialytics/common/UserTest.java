package sp.financialytics.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
  User test;

  @BeforeEach
  void setUp() {
    test = new User();
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
  void email() {
    test.setEmail("test@test.com");

    assertEquals("test@test.com", test.getEmail());
  }

  @Test
  void password() {
    test.setPassword("test");

    assertEquals("test", test.getPassword());
  }

  @Test
  void transactions() {
    test.setTransactions(List.of(new Transaction("id", LocalDate.now(), "description", "category", 100L)));

    assertThat(test.getTransactions()).size().isEqualTo(1);
    assertEquals("id", test.getTransactions().get(0).getId());
    assertEquals(LocalDate.now(), test.getTransactions().get(0).getDate());
    assertEquals("description", test.getTransactions().get(0).getDescription());
    assertEquals("category", test.getTransactions().get(0).getCategory());
    assertEquals(100L, test.getTransactions().get(0).getAmount());
  }

  @Test
  void warningConfig() {
    test.setWarningConfig(new Warning[] { new Warning("message", true, false) });

    assertEquals(1, test.getWarningConfig().length);
    assertEquals("message", test.getWarningConfig()[0].getMessage());
    assertTrue(test.getWarningConfig()[0].isEnabled());
    assertFalse(test.getWarningConfig()[0].isDismissed());
  }

  @Test
  void targets() {
    test.setTargets(
            Map.of("income", 200000L, "food", 20000L, "living", 20001L, "entertainment", 20002L, "supplies",
                    20003L, "education", 20004L, "other", 20005L)
    );

    assertEquals(7, test.getTargets().size());
    assertEquals(200000L, test.getTargets().get("income"));
    assertEquals(20000L, test.getTargets().get("food"));
    assertEquals(20001L, test.getTargets().get("living"));
    assertEquals(20002L, test.getTargets().get("entertainment"));
    assertEquals(20003L, test.getTargets().get("supplies"));
    assertEquals(20004L, test.getTargets().get("education"));
    assertEquals(20005L, test.getTargets().get("other"));
  }
}