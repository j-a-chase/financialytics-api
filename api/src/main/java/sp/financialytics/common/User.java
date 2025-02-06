package sp.financialytics.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private Integer id;
    private String email;
    private String password;
    private String name;

    public User() {
        id = 1;
        email = "example@gmail.com";
        password = "password";
        name = "John Doe";
    }
}
