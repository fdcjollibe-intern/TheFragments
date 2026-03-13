package com.apollo.thefragments.repository

import com.apollo.thefragments.data.db.SessionDao
import com.apollo.thefragments.data.db.UserDao
import com.apollo.thefragments.data.model.Session
import com.apollo.thefragments.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

// Repository is the single source of truth for auth.
// It talks to Firebase Auth AND Room DB.
// ViewModel never touches Firebase or Room directly — always through here.
class AuthRepository(
    private val userDao: UserDao,
    private val sessionDao: SessionDao
) {
    private val firebaseAuth = FirebaseAuth.getInstance()

    // ─── SESSION ────────────────────────────────────────────────

    // SplashActivity calls this to decide where to go
    suspend fun isLoggedIn(): Boolean {
        return sessionDao.getSession()?.isLoggedIn == true
    }

    // Saves isLoggedIn = true to Room after any successful login
    private suspend fun saveSession(provider: String) {
        sessionDao.saveSession(Session(isLoggedIn = true, provider = provider))
    }

    // Called on logout — clears both Room tables
    suspend fun logout() {
        firebaseAuth.signOut()
        sessionDao.clearSession()
        userDao.clearAll()
    }

    // ─── EMAIL / PASSWORD ───────────────────────────────────────

    // Returns Result.success with a message, or Result.failure with the error
    suspend fun registerWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!
            // Save user info to Room
            userDao.insertUser(
                User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", displayName = "")
            )
            saveSession("email")
            Result.success("Registered successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!
            userDao.insertUser(
                User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", displayName = firebaseUser.displayName ?: "")
            )
            saveSession("email")
            Result.success("Login successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── GOOGLE SIGN-IN ─────────────────────────────────────────

    // Called after Google Sign-In returns an account — exchanges it for a Firebase credential
    suspend fun loginWithGoogle(account: GoogleSignInAccount): Result<String> {
        return try {
            // Convert the Google account token into a Firebase credential
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user!!
            userDao.insertUser(
                User(
                    uid         = firebaseUser.uid,
                    email       = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: ""
                )
            )
            saveSession("google")
            Result.success("Google login successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
