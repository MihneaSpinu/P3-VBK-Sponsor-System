package p3project.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TypeTest {
    @Test
    public void testTypeCreation(){
        Type type = new Type("null");

        type.setName("name");

        assertEquals("name", type.getName());
    }
}
