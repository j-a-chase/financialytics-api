package sp.financialytics.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Comparable<Transaction> {
  private String id;
  @JsonFormat(pattern="dd-MMM-yyyy")
  private LocalDate date;
  private String description;
  private String category;
  private Long amount;
  private String notes;

  @Override
  public int compareTo(Transaction other) {
    if (!this.date.isEqual(other.date))
      return this.date.compareTo(other.date);

    if (!this.category.equals(other.category))
      return this.category.compareTo(other.category);

    if (!this.amount.equals(other.amount))
      return this.amount.compareTo(other.amount);

    return this.description.compareTo(other.description);
  }
}
