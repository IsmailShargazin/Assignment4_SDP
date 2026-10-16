import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {

    private static class Charge {

        private final String email;
        private final long amountTenge;

        public Charge(String email, long amountTenge) {
            this.email = email;
            this.amountTenge = amountTenge;
        }

        public String email() {
            return email;
        }

        public long amountTenge() {
            return amountTenge;
        }
    }

    private final Map<String, Long> balances = new HashMap<>();

    private final Map<String, Charge> charges = new HashMap<>();

    public PaymentProcessor(Map<String, Long> initialBalances) {
        for (Map.Entry<String, Long> account
                : initialBalances.entrySet()) {

            String email = account.getKey();
            long amount = account.getValue();

            if (email == null || email.isBlank() || amount < 0) {
                throw new IllegalArgumentException(
                        "Invalid account."
                );
            }

            balances.put(email, amount);
        }
    }

    public long balance(String email) {
        return balances.getOrDefault(email, 0L);
    }

    public void charge(
            String orderId,
            Customer customer,
            long amountTenge
    ) {
        if (amountTenge <= 0 || charges.containsKey(orderId)) {
            throw new OrderException(
                    "Invalid amount or duplicate payment."
            );
        }

        if (!balances.containsKey(customer.email())
                || balance(customer.email()) < amountTenge) {

            throw new OrderException(
                    "Payment declined: insufficient balance."
            );
        }

        balances.put(
                customer.email(),
                balance(customer.email()) - amountTenge
        );

        charges.put(
                orderId,
                new Charge(customer.email(), amountTenge)
        );

        System.out.println(
                "Payment: charged " + amountTenge
                        + " KZT for " + orderId
        );
    }

    public void refund(String orderId) {
        Charge charge = charges.remove(orderId);

        if (charge == null) {
            return;
        }

        balances.put(
                charge.email(),
                balance(charge.email()) + charge.amountTenge()
        );

        System.out.println(
                "Payment: refunded " + charge.amountTenge()
                        + " KZT for " + orderId
        );
    }
}