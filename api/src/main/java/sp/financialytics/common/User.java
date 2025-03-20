package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private Integer id;
  private String name;
  private String password;
  private String email;
  private LeniencyLevel budgetLeniency;
  private List<Transaction> transactions;
  private Warning[] warningConfig;
  private Map<String, Long> targets;
}
