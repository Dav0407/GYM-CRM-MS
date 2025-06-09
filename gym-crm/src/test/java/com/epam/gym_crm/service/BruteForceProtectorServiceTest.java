package com.epam.gym_crm.service;

import com.epam.gym_crm.service.impl.BruteForceProtectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectorServiceTest {

    private BruteForceProtectorService bruteForceProtectorService;

    private static final String TEST_USERNAME = "testuser";

    @Mock
    private Map<String, Integer> mockAttempts;

    @Mock
    private Map<String, Long> mockBlockTime;

    @BeforeEach
    void setUp() {
        bruteForceProtectorService = new BruteForceProtectorService();
    }

    @Test
    void testLoginSucceeded_ShouldRemoveUserFromMaps() {
        // Setup - add the user to attempts and blockTime
        bruteForceProtectorService.loginFailed(TEST_USERNAME);

        // Execute
        bruteForceProtectorService.loginSucceeded(TEST_USERNAME);

        // Verify
        assertFalse(bruteForceProtectorService.isBlocked(TEST_USERNAME));

        // Check internal state
        try {
            Field attemptsField = BruteForceProtectorService.class.getDeclaredField("attempts");
            attemptsField.setAccessible(true);
            Map<String, Integer> attempts = (Map<String, Integer>) attemptsField.get(bruteForceProtectorService);

            Field blockTimeField = BruteForceProtectorService.class.getDeclaredField("blockTime");
            blockTimeField.setAccessible(true);
            Map<String, Long> blockTime = (Map<String, Long>) blockTimeField.get(bruteForceProtectorService);

            assertFalse(attempts.containsKey(TEST_USERNAME));
            assertFalse(blockTime.containsKey(TEST_USERNAME));
        } catch (Exception e) {
            fail("Failed to access internal fields: " + e.getMessage());
        }
    }

    @Test
    void testLoginFailed_ShouldIncrementAttempts() {
        // Execute
        bruteForceProtectorService.loginFailed(TEST_USERNAME);
        bruteForceProtectorService.loginFailed(TEST_USERNAME);

        // Verify - not blocked yet (need 3 attempts)
        assertFalse(bruteForceProtectorService.isBlocked(TEST_USERNAME));

        // Check internal state
        try {
            Field attemptsField = BruteForceProtectorService.class.getDeclaredField("attempts");
            attemptsField.setAccessible(true);
            Map<String, Integer> attempts = (Map<String, Integer>) attemptsField.get(bruteForceProtectorService);

            assertEquals(2, attempts.get(TEST_USERNAME));
        } catch (Exception e) {
            fail("Failed to access internal fields: " + e.getMessage());
        }
    }

    @Test
    void testLoginFailed_ShouldBlockAfterMaxAttempts() {
        // Execute - 3 failed attempts
        bruteForceProtectorService.loginFailed(TEST_USERNAME);
        bruteForceProtectorService.loginFailed(TEST_USERNAME);
        bruteForceProtectorService.loginFailed(TEST_USERNAME);

        // Verify
        assertTrue(bruteForceProtectorService.isBlocked(TEST_USERNAME));

        // Check internal state
        try {
            Field attemptsField = BruteForceProtectorService.class.getDeclaredField("attempts");
            attemptsField.setAccessible(true);
            Map<String, Integer> attempts = (Map<String, Integer>) attemptsField.get(bruteForceProtectorService);

            Field blockTimeField = BruteForceProtectorService.class.getDeclaredField("blockTime");
            blockTimeField.setAccessible(true);
            Map<String, Long> blockTime = (Map<String, Long>) blockTimeField.get(bruteForceProtectorService);

            assertEquals(3, attempts.get(TEST_USERNAME));
            assertTrue(blockTime.containsKey(TEST_USERNAME));

            // Block time should be in the future
            long currentTime = System.currentTimeMillis();
            assertTrue(blockTime.get(TEST_USERNAME) > currentTime);
        } catch (Exception e) {
            fail("Failed to access internal fields: " + e.getMessage());
        }
    }

    @Test
    void testIsBlocked_WhenUserNotBlocked() {
        // No setup needed - new user should not be blocked

        // Execute & Verify
        assertFalse(bruteForceProtectorService.isBlocked(TEST_USERNAME));
    }

    @Test
    void testIsBlocked_WhenUserIsBlocked() {
        // Setup - block the user
        bruteForceProtectorService.loginFailed(TEST_USERNAME);
        bruteForceProtectorService.loginFailed(TEST_USERNAME);
        bruteForceProtectorService.loginFailed(TEST_USERNAME);

        // Execute & Verify
        assertTrue(bruteForceProtectorService.isBlocked(TEST_USERNAME));
    }

    @Test
    void testIsBlocked_WhenBlockTimeExpired() throws Exception {
        // Need to use reflection to manipulate block time
        Field blockTimeField = BruteForceProtectorService.class.getDeclaredField("blockTime");
        blockTimeField.setAccessible(true);
        Map<String, Long> blockTimeMap = (Map<String, Long>) blockTimeField.get(bruteForceProtectorService);

        // Set block time to past
        blockTimeMap.put(TEST_USERNAME, System.currentTimeMillis() - 1000);

        // Execute
        boolean isBlocked = bruteForceProtectorService.isBlocked(TEST_USERNAME);

        // Verify
        assertFalse(isBlocked);

        // Check internal state - should be removed
        Field attemptsField = BruteForceProtectorService.class.getDeclaredField("attempts");
        attemptsField.setAccessible(true);
        Map<String, Integer> attempts = (Map<String, Integer>) attemptsField.get(bruteForceProtectorService);

        assertFalse(attempts.containsKey(TEST_USERNAME));
        assertFalse(blockTimeMap.containsKey(TEST_USERNAME));
    }

    @Test
    void testMultipleUsers_IndependentBlocking() {
        // Setup - block first user
        String user1 = "user1";
        String user2 = "user2";

        bruteForceProtectorService.loginFailed(user1);
        bruteForceProtectorService.loginFailed(user1);
        bruteForceProtectorService.loginFailed(user1);

        // Only one failure for second user
        bruteForceProtectorService.loginFailed(user2);

        // Execute & Verify
        assertTrue(bruteForceProtectorService.isBlocked(user1));
        assertFalse(bruteForceProtectorService.isBlocked(user2));
    }

    @Test
    void testWithMockedInternals() throws Exception {
        // Create a test instance with mocked internals
        BruteForceProtectorService service = new BruteForceProtectorService();

        // Replace internal maps with mocks
        Field attemptsField = BruteForceProtectorService.class.getDeclaredField("attempts");
        attemptsField.setAccessible(true);
        attemptsField.set(service, mockAttempts);

        Field blockTimeField = BruteForceProtectorService.class.getDeclaredField("blockTime");
        blockTimeField.setAccessible(true);
        blockTimeField.set(service, mockBlockTime);

        // Setup mocks
        when(mockAttempts.getOrDefault(TEST_USERNAME, 0)).thenReturn(2);

        // Test loginFailed
        service.loginFailed(TEST_USERNAME);

        // Verify interactions
        verify(mockAttempts).getOrDefault(TEST_USERNAME, 0);
        verify(mockAttempts).put(TEST_USERNAME, 3); // Should increment to 3
        verify(mockBlockTime).put(eq(TEST_USERNAME), anyLong()); // Should set block time
    }
}