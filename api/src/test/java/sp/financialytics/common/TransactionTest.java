package sp.financialytics.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTest {
  Transaction test;

  @BeforeEach
  void setUp() {
    test = new Transaction();
  }

  @Test
  void id() {
    test.setId("id");

    assertEquals("id", test.getId());
  }

  @Test
  void date() {
    test.setDate(LocalDate.now());

    assertEquals(LocalDate.now(), test.getDate());
  }

  @Test
  void amount() {
    test.setAmount(100);

    assertEquals(100, test.getAmount());
  }

  @Test
  void description() {
    test.setDescription("description");

    assertEquals("description", test.getDescription());
  }

  @Test
  void category() {
    test.setCategory("category");

    assertEquals("category", test.getCategory());
  }
}