package p3project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class HelloWorldTest {
    @Test
    void helloWorld() {
        String message = "Hello, World!";
        Assertions.assertEquals("Hello, World!", message);
    }
}