package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Target and its needed information for the project (aka category)
 * Stores the target name, budget amount for that target, and whether it should be included in the monthly budget or not
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Target implements Comparable<Target>, Cloneable {
  Integer id;
  String name;
  Long amount;
  Boolean included;

  @Override
  public int compareTo(Target target) {
    return this.name.compareTo(target.name);
  }

  @Override
  public Target clone() throws CloneNotSupportedException {
    Target clone = (Target) super.clone();

    clone.id = id;
    clone.name = name;
    clone.amount = amount;
    clone.included = included;

    return clone;
  }
}
