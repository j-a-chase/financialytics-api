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
  void description() {
    test.setDescription("description");

    assertEquals("description", test.getDescription());
  }

  @Test
  void category() {
    test.setCategory("category");

    assertEquals("category", test.getCategory());
  }

  @Test
  void amount() {
    test.setAmount(100L);

    assertEquals(100L, test.getAmount());
  }

  @Test
  void notes() {
    test.setNotes("notes");

    assertEquals("notes", test.getNotes());
  }

  @Test
  void compareToDateDifference() {
    test = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category", 100L, "");
    Transaction other = new Transaction("1-0", LocalDate.of(2025, 1, 2), "description", "category", 100L, "");

    assertEquals(-1, test.compareTo(other));
    assertEquals(1, other.compareTo(test));
  }

  @Test
  void compareToCategoryDifference() {
    test = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category", 100L, "");
    Transaction other = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category!", 100L, "");

    assertEquals(-1, test.compareTo(other));
    assertEquals(1, other.compareTo(test));
  }

  @Test
  void compareToAmountDifference() {
    test = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category", 100L, "");
    Transaction other = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category", 101L, "");

    assertEquals(-1, test.compareTo(other));
    assertEquals(1, other.compareTo(test));
  }

  @Test
  void compareToDescriptionDifference() {
    test = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category", 100L, "");
    Transaction other = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description!", "category", 100L, "");

    assertEquals(-1, test.compareTo(other));
    assertEquals(1, other.compareTo(test));
  }

  @Test
  void compareToEqual() {
    test = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category", 100L, "");
    Transaction other = new Transaction("1-0", LocalDate.of(2025, 1, 1), "description", "category", 100L, "");

    assertEquals(0, test.compareTo(other));
    assertEquals(0, other.compareTo(test));
  }
}