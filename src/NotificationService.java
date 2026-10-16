import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private final List<String> messages = new ArrayList<>();

    public void send(Customer customer, String message) {
        String notification =
                "To " + customer.email() + ": " + message;

        messages.add(notification);

        System.out.println(
                "Notification: " + notification
        );
    }

    public List<String> getMessages() {
        return List.copyOf(messages);
    }
}