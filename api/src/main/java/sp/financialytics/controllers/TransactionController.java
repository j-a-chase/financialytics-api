package sp.financialytics.controllers;

import org.springframework.web.bind.annotation.*;
import sp.financialytics.common.Transaction;

import java.util.List;

@RestController
@RequestMapping("transaction")
public class TransactionController {
    @GetMapping("recent")
    public List<Transaction> getRecentTransactions(@RequestParam Integer userId) {
        return List.of(new Transaction(), new Transaction(), new Transaction());
    }

    @GetMapping("all")
    public List<Transaction> getAllTransactions(@RequestParam Integer userId) {
        return List.of(new Transaction(), new Transaction(), new Transaction(), new Transaction(), new Transaction(),
                new Transaction(), new Transaction(), new Transaction(), new Transaction(), new Transaction(),
                new Transaction(), new Transaction(), new Transaction(), new Transaction(), new Transaction());
    }

    @PostMapping("update")
    public String updateTransaction(@RequestParam Integer userId, @RequestParam Integer transactionId,
                                    @RequestBody Transaction transaction) {
        return "Updated transaction!";
    }
}
