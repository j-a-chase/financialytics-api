package sp.financialytics.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Transaction {
  private String id;
  @JsonFormat(pattern="dd-MMM-yyyy")
  private LocalDate date;
  private String description;
  private String category;
  private Integer amount;
}
