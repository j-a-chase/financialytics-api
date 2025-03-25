package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is not yet used within the project.
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warning {
  private String message;
  private boolean enabled;
  private boolean dismissed;
}
