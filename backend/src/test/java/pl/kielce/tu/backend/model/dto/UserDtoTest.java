package pl.kielce.tu.backend.model.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class UserDtoTest {

    @Test
    void allArgsConstructorAndGetters() {
        UserDto user = UserDto.builder()
                .nickname("nick")
                .password("pass")
                .build();
        assertEquals("nick", user.getNickname());
        assertEquals("pass", user.getPassword());
    }

    @Test
    void builderAndSetters() {
        UserDto user = UserDto.builder()
                .nickname("n")
                .password("p")
                .build();

        assertEquals("n", user.getNickname());
        assertEquals("p", user.getPassword());

        user.setNickname("n2");
        user.setPassword("p2");

        assertEquals("n2", user.getNickname());
        assertEquals("p2", user.getPassword());
    }

    @Test
    void equalsAndHashCode() {
        UserDto a = UserDto.builder().nickname("x").password("y").build();
        UserDto b = UserDto.builder().nickname("x").password("y").build();
        UserDto c = UserDto.builder().nickname("other").password("y").build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toStringContainsFields() {
        UserDto user = UserDto.builder()
                .nickname("nick")
                .password("pass")
                .build();
        String s = user.toString();
        assertTrue(s.contains("nick"));
        assertTrue(s.contains("pass"));
    }

    @Test
    void isSerializable() throws IOException, ClassNotFoundException {
        UserDto original = UserDto.builder()
                .nickname("sN")
                .password("sP")
                .build();

        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
            oos.flush();
            bytes = baos.toByteArray();
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            assertTrue(obj instanceof UserDto);
            UserDto deserialized = (UserDto) obj;
            assertEquals(original, deserialized);
        }
    }
}
