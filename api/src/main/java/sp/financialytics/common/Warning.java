package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warning {
  private String message;
  private boolean enabled;
  private boolean dismissed;
}
