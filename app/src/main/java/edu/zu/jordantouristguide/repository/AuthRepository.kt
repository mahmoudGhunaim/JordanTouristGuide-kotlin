package edu.zu.jordantouristguide.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import edu.zu.jordantouristguide.data.local.LocalUserDao
import edu.zu.jordantouristguide.data.local.LocalUserEntity
import edu.zu.jordantouristguide.data.local.toModel
import edu.zu.jordantouristguide.data.model.User
import edu.zu.jordantouristguide.data.preferences.UserPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Why a login/register attempt failed. The UI turns this into a translated message. */
enum class AuthError { INVALID_CREDENTIALS, EMAIL_IN_USE, WEAK_PASSWORD, NETWORK, UNKNOWN }

class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)

interface AuthRepository {
    /** Emits the signed-in user, or null for guests. */
    val currentUser: Flow<User?>

    suspend fun login(email: String, password: String): User
    suspend fun register(name: String, email: String, password: String): User
    suspend fun logout()
}

/** Firebase Authentication with email and password. */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): User = mapErrors {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        result.user?.toUser() ?: throw AuthException(AuthError.UNKNOWN)
    }

    override suspend fun register(name: String, email: String, password: String): User = mapErrors {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw AuthException(AuthError.UNKNOWN)
        user.updateProfile(userProfileChangeRequest { displayName = name.trim() }).await()
        user.toUser().copy(name = name.trim())
    }

    override suspend fun logout() = auth.signOut()

    private fun FirebaseUser.toUser() = User(
        id = uid,
        name = displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty().substringBefore('@'),
        email = email.orEmpty(),
    )

    private suspend fun <T> mapErrors(block: suspend () -> T): T = try {
        block()
    } catch (e: AuthException) {
        throw e
    } catch (e: FirebaseAuthWeakPasswordException) { // must be checked before its parent class
        throw AuthException(AuthError.WEAK_PASSWORD, e)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        throw AuthException(AuthError.INVALID_CREDENTIALS, e)
    } catch (e: FirebaseAuthInvalidUserException) {
        throw AuthException(AuthError.INVALID_CREDENTIALS, e)
    } catch (e: FirebaseAuthUserCollisionException) {
        throw AuthException(AuthError.EMAIL_IN_USE, e)
    } catch (e: FirebaseNetworkException) {
        throw AuthException(AuthError.NETWORK, e)
    } catch (e: Exception) {
        throw AuthException(AuthError.UNKNOWN, e)
    }
}

/**
 * Offline demo authentication, used until Firebase is connected.
 * Accounts are kept in Room and the session in DataStore, so the whole
 * login / register / logout flow can be demonstrated without internet.
 */
@Singleton
class LocalAuthRepository @Inject constructor(
    private val userDao: LocalUserDao,
    private val preferences: UserPreferences,
) : AuthRepository {

    override val currentUser: Flow<User?> = preferences.localSessionUserId.map { id ->
        id?.let { userDao.findById(it)?.toModel() }
    }

    override suspend fun login(email: String, password: String): User {
        val user = userDao.findByEmail(email.trim().lowercase())
        if (user == null || user.passwordHash != hash(email, password)) {
            throw AuthException(AuthError.INVALID_CREDENTIALS)
        }
        preferences.setLocalSessionUserId(user.id)
        return user.toModel()
    }

    override suspend fun register(name: String, email: String, password: String): User {
        val normalizedEmail = email.trim().lowercase()
        if (password.length < MIN_PASSWORD_LENGTH) throw AuthException(AuthError.WEAK_PASSWORD)
        if (userDao.findByEmail(normalizedEmail) != null) throw AuthException(AuthError.EMAIL_IN_USE)

        val entity = LocalUserEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            email = normalizedEmail,
            passwordHash = hash(normalizedEmail, password),
        )
        userDao.insert(entity)
        preferences.setLocalSessionUserId(entity.id)
        return entity.toModel()
    }

    override suspend fun logout() = preferences.setLocalSessionUserId(null)

    /** SHA-256 of email + password (the email acts as a simple salt). */
    private fun hash(email: String, password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((email.trim().lowercase() + ":" + password).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
