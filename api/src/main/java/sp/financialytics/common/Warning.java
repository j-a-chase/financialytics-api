package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Warning {
    private enum WarningType { INVALID, MONTHLY_CLOSE, MONTHLY_BUDGET, CATEGORY_CLOSE, CATEGORY_BUDGET }
    private WarningType warningType;
    private String message;
    private boolean dismissed;

    public Warning() {
        warningType = WarningType.INVALID;
        message = "warning message";
        dismissed = false;
    }
}
