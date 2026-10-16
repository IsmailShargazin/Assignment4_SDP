public class Customer {

    private final String name;
    private final String email;
    private final String address;

    public Customer(String name, String email, String address) {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || address == null || address.isBlank()) {

            throw new IllegalArgumentException(
                    "Name, email and address are required."
            );
        }

        this.name = name;
        this.email = email;
        this.address = address;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String address() {
        return address;
    }
}