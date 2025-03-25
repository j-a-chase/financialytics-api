package sp.financialytics.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Stores everything we need to know about a financial Transaction:
 *  The id is made up of the following: "userId-transactionIndex"
 *    - This allows for more efficient handling of the transaction within the list of transactions
 *    - Easily links the user to their transactions
 *  The date the transaction was made
 *  A description of what the transaction was for
 *  The designated category(target) for the transaction (may end up making this a Target object)
 *  The amount of the transaction, stored lossless as a Long
 *  Any further notes about the transaction, which can be viewed on the details page within the application
*/
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
