package p3project.classes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TypeTest {
    @Test
    public void testTypeCreation(){
        Type type = new Type("null");

        type.setName("name");

        assertEquals("name", type.getName());
    }
}
