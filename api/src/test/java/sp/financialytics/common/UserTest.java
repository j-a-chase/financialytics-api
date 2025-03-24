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
  void budgetLeniency() {
    test.setBudgetLeniency(LeniencyLevel.NORMAL);

    assertEquals(LeniencyLevel.NORMAL, test.getBudgetLeniency());
  }

  @Test
  void transactions() {
    test.setTransactions(List.of(new Transaction("id", LocalDate.now(), "description", "category", 100L, "")));

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
    test.setTargets(List.of(new Target(0, "target", 100L, true)));

    assertEquals(1, test.getTargets().size());
    assertEquals(0, test.getTargets().get(0).getId());
    assertEquals("target", test.getTargets().get(0).getName());
    assertEquals(100L, test.getTargets().get(0).getAmount());
    assertTrue(test.getTargets().get(0).getIncluded());
  }
}