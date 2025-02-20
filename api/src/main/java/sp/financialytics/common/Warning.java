package sp.financialytics.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Warning {
  private String message;
  private boolean enabled;
  private boolean dismissed;
}
