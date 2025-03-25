package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  public Target clone() {
    try {
      Target clone = (Target) super.clone();

      clone.id = id;
      clone.name = name;
      clone.amount = amount;
      clone.included = included;

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
