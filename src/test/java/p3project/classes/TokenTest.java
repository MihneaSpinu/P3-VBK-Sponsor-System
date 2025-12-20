package p3project.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Token class
 * Tests token signing, hashing, and verification
 */
public class TokenTest {

    @Test
    public void testTokenSignCreatesToken() {
        // Arrange
        String userId = "123";
        
        // Act
        Token token = Token.sign(userId);
        
        // Assert
        assertNotNull(token);
        assertNotNull(token.getHash());
    }

    @Test
    public void testTokenHashIsNotEmpty() {
        // Arrange
        String userId = "456";
        
        // Act
        Token token = Token.sign(userId);
        
        // Assert
        assertTrue(token.getHash().length() > 0);
    }

    @Test
    public void testTokenHashIs64CharactersLong() {
        // Arrange
        String userId = "789";
        
        // Act
        Token token = Token.sign(userId);
        
        // Assert
        assertEquals(64, token.getHash().length());
    }

    @Test
    public void testTokenVerifyWithCorrectHash() {
        // Arrange
        String userId = "100";
        Token token = Token.sign(userId);
        String correctHash = token.getHash();
        
        // Act
        boolean isValid = token.verify(correctHash);
        
        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testTokenVerifyWithIncorrectHash() {
        // Arrange
        String userId = "200";
        Token token = Token.sign(userId);
        String incorrectHash = "wronghash123";
        
        // Act
        boolean isValid = token.verify(incorrectHash);
        
        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testTokensWithSameIdProduceDifferentHashesDueToSecret() {

        // Arrange
        String userId = "300";
        
        // Act
        Token token1 = Token.sign(userId);
        Token token2 = Token.sign(userId);
        
        // Assert
        assertEquals(token1.getHash(), token2.getHash());
    }

    @Test
    public void testTokensWithDifferentIdsProduceDifferentHashes() {
        // Arrange
        String userId1 = "400";
        String userId2 = "500";
        
        // Act
        Token token1 = Token.sign(userId1);
        Token token2 = Token.sign(userId2);
        
        // Assert
        assertFalse(token1.getHash().equals(token2.getHash()));
    }

    @Test
    public void testSetHashChangesHash() {
        // Arrange
        Token token = Token.sign("600");
        String originalHash = token.getHash();
        String newHash = "newhashvalue";
        
        // Act
        token.setHash(newHash);
        
        // Assert
        assertEquals(newHash, token.getHash());
        assertFalse(originalHash.equals(token.getHash()));
    }
}
