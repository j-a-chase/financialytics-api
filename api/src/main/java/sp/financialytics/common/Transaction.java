package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Transaction {
    private Integer amount;
    private Date date;
    private String description;
    private String category;

    public Transaction() {
        amount = 117;
        date = new Date();
        description = "description";
        category = "category";
    }
}
