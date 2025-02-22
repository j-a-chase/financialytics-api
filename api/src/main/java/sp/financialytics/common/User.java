package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private Integer id;
  private String email;
  private String password;
  private String name;
  private List<Transaction> transactions;
  private Warning[] warningConfig;
}
