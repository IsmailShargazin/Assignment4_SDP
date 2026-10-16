# Assignment 4 — Facade Pattern

Java project demonstrating the Facade design pattern
through online order processing.

## Structure

- OrderFacade coordinates the order workflow.
- InventoryService manages stock and reservations.
- PaymentProcessor processes payments and refunds.
- ShippingService manages deliveries.
- NotificationService stores and displays notifications.

The client Main accesses the subsystem through OrderFacade.
Failed orders trigger rollback.

## Requirements

JDK 17.

## Run

Open the project in IntelliJ IDEA and run Main.java.

For automated checks, run OrderFacadeTest.java.