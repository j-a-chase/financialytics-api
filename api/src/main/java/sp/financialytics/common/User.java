package sp.financialytics.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class User {
  private Integer id;
  private String email;
  private String password;
  private String name;
  private List<Transaction> transactions;
  private Warning[] warnings;

  @SuppressWarnings("unused")
  private User() { throw new RuntimeException("User default constructor: disabled"); }

  public static User createDefault() {
    return User.builder()
            .id(-1)
            .name("Error User")
            .email("error@error.com")
            .password("errorPass0rd*")
            .build();
  }

  public static User readFromJsonNode(JsonNode userNode) {
    List<Transaction> transactions = new ArrayList<>();
    Warning[] warningConfig;
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.setDateFormat(new SimpleDateFormat("dd-MMM-yyyy"));

      ArrayNode transactionsNode = (ArrayNode) userNode.get("transactions");
      for (JsonNode transactionNode : transactionsNode) {
        transactions.add(mapper.convertValue(transactionNode, Transaction.class));
      }

      ArrayNode warningConfigNode = (ArrayNode) userNode.get("warningConfig");
      warningConfig = mapper.convertValue(warningConfigNode, Warning[].class);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Error reading user from JSON", e);
    }

    return User.builder()
            .id(userNode.get("userId").intValue())
            .email(userNode.get("email").textValue())
            .password(userNode.get("userPass").textValue())
            .name(userNode.get("user").textValue())
            .transactions(transactions)
            .warnings(warningConfig)
            .build();
  }
}
