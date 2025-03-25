package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The User class holds all the information about a particular user of the application
 *  The id, name, password, email are all pretty straightforward authentication necessities, existing to plan for the
 *    future as they are not currently used aside from the id
 *  The budgetLeniency is where the LeniencyLevel for their budget is stored, being used on the home page of the
 *    application
 *  The transactions list holds all transactions for the user
 *  The warningConfig is not yet used within the project, but is there for future use
 *  The targets list contains the various Targets the user has set up for their monthly budget
*/
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
  private List<Target> targets;
}
