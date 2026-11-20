package fr.gravelinesgrimp.pocket.notification.config;

/** Where push messages leave from: virtual threads in the app, on the spot in the tests. */
@FunctionalInterface
public interface PushRunner {

    void run(Runnable task);
}
