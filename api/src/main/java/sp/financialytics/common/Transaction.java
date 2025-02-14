package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Transaction {
    private Integer amount;
    private LocalDate date;
    private String description;
    private String category;

    public Transaction() {
        amount = 117;
        date = LocalDate.now();
        description = "description";
        category = "category";
    }
}
