package edu.zu.jordantouristguide.repository

import edu.zu.jordantouristguide.testutil.FakeLocalUserDao
import edu.zu.jordantouristguide.testutil.FakeUserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class LocalAuthRepositoryTest {

    private val repo = LocalAuthRepository(FakeLocalUserDao(), FakeUserPreferences())

    @Test
    fun `register signs the user in`() = runTest {
        val user = repo.register("Ahmed", "Ahmed@Example.com ", "secret1")

        assertEquals("ahmed@example.com", user.email)
        assertEquals(user, repo.currentUser.first())
    }

    @Test
    fun `login works with correct password and any email case`() = runTest {
        repo.register("Khalid", "khalid@example.com", "secret1")
        repo.logout()
        assertNull(repo.currentUser.first())

        val user = repo.login("KHALID@example.com", "secret1")
        assertEquals("Khalid", user.name)
    }

    @Test
    fun `wrong password is rejected`() = runTest {
        repo.register("Khalid", "khalid@example.com", "secret1")
        expectError(AuthError.INVALID_CREDENTIALS) { repo.login("khalid@example.com", "wrong-pass") }
    }

    @Test
    fun `duplicate email is rejected`() = runTest {
        repo.register("A", "same@example.com", "secret1")
        expectError(AuthError.EMAIL_IN_USE) { repo.register("B", "same@example.com", "secret2") }
    }

    @Test
    fun `short password is rejected`() = runTest {
        expectError(AuthError.WEAK_PASSWORD) { repo.register("A", "a@example.com", "123") }
    }

    private suspend fun expectError(expected: AuthError, block: suspend () -> Unit) {
        try {
            block()
            fail("Expected $expected")
        } catch (e: AuthException) {
            assertEquals(expected, e.error)
        }
    }
}
