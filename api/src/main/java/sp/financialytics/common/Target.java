package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Target implements Comparable<Target> {
  Integer id;
  String name;
  Long amount;
  Boolean included;

  @Override
  public int compareTo(Target target) {
    return this.name.compareTo(target.name);
  }
}
