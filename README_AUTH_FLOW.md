Auth flow trace — files and exact lines

Purpose

This document traces the runtime flow from app launch through Register/Login (including Google Sign-In) until the app shows the Home screen. Each step lists the file, path and the exact line(s) that run in sequence.

Quick plan / Checklist

- [x] Confirm app entry (manifest → launcher activity)
- [x] Trace Splash → session check (Room) → decide destination
- [x] Trace Auth UI wiring: `AuthActivity` → `AuthViewModel` → `AuthRepository`
- [x] Trace UI events in `LoginFragment` and `RegisterFragment` that call ViewModel methods (with lines)
- [x] Trace repository calls to Firebase and Room, and session saving
- [x] Trace post-auth navigation to `MainActivity` and Home fragment creation

Important files read (paths inside project)

- `app/src/main/AndroidManifest.xml` (launcher activity)
- `app/src/main/java/com/apollo/thefragments/ui/splash/SplashActivity.kt`
- `app/src/main/java/com/apollo/thefragments/ui/auth/AuthActivity.kt`
- `app/src/main/java/com/apollo/thefragments/ui/auth/AuthViewModel.kt`
- `app/src/main/java/com/apollo/thefragments/ui/auth/LoginFragment.kt`
- `app/src/main/java/com/apollo/thefragments/ui/auth/RegisterFragment.kt`
- `app/src/main/java/com/apollo/thefragments/repository/AuthRepository.kt`
- `app/src/main/java/com/apollo/thefragments/data/db/AppDatabase.kt`
- `app/src/main/java/com/apollo/thefragments/data/db/SessionDao.kt`
- `app/src/main/java/com/apollo/thefragments/data/model/Session.kt`
- `app/src/main/java/com/apollo/thefragments/MainActivity.kt`
- `app/src/main/java/com/apollo/thefragments/fragments/HomeFragment.kt`

Trace: App start → Splash → (auth decision) → AuthActivity → Login/Register → Repository → MainActivity → Home

1) App entry (launcher)

- `app/src/main/AndroidManifest.xml` lines 15-22 — launcher activity is `com.apollo.thefragments.ui.splash.SplashActivity`.
  - File: `app/src/main/AndroidManifest.xml`
  - Lines: 15-22

2) `SplashActivity` (decides destination based on Room session)

- `app/src/main/java/com/apollo/thefragments/ui/splash/SplashActivity.kt`
  - Lines 21-26: onCreate sets content view and creates DB + repository
    - 25: val db         = AppDatabase.getDatabase(this)
    - 26: val repository = AuthRepository(db.userDao(), db.sessionDao())
  - Lines 28-45: lifecycleScope.launch that delays, then calls repository.isLoggedIn() and starts the destination activity
    - 30: delay(1500)
    - 32-37: val destination = if (repository.isLoggedIn()) Intent(...MainActivity) else Intent(...AuthActivity)
    - 42-44: destination.flags = FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK; startActivity(destination); finish()

- The check calls `AuthRepository.isLoggedIn()` which reads the session from Room:
  - `app/src/main/java/com/apollo/thefragments/repository/AuthRepository.kt` lines 24-26
    - 24-26: suspend fun isLoggedIn(): Boolean { return sessionDao.getSession()?.isLoggedIn == true }
  - `app/src/main/java/com/apollo/thefragments/data/db/SessionDao.kt` lines 16-17 — `getSession()` returns the single-row session (or null)
    - 16-17: @Query("SELECT * FROM session WHERE id = 1 LIMIT 1") suspend fun getSession(): Session?
  - `app/src/main/java/com/apollo/thefragments/data/model/Session.kt` lines 9-15 — Session entity fields (id=1, isLoggedIn, provider)
    - 9-15: @Entity(tableName = "session") data class Session(...)

If not logged in, `SplashActivity` starts `AuthActivity`. If logged in, it starts `MainActivity`.

3) `AuthActivity` onCreate — ViewModel + Google Sign-In + ViewPager

- `app/src/main/java/com/apollo/thefragments/ui/auth/AuthActivity.kt`
  - Lines 29-37 — setContentView and MVVM wiring
    - 34: val db         = AppDatabase.getDatabase(this)
    - 35: val repository = AuthRepository(db.userDao(), db.sessionDao())
    - 36: val factory    = AuthViewModelFactory(repository)
    - 37: viewModel      = ViewModelProvider(this, factory)[AuthViewModel::class.java]
  - Lines 41-45 — GoogleSignInOptions build and client creation
    - 41-44: val gso = GoogleSignInOptions.Builder(...).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
    - 45: googleSignInClient = GoogleSignIn.getClient(this, gso)
  - Lines 48-53 — ViewPager2 + TabLayout setup (Login / Register tabs)
    - 48-51: viewPager.adapter = AuthPagerAdapter(this)
    - 51-53: TabLayoutMediator(...) { tab.text = if (position==0) "Login" else "Register" }.attach()
  - Lines 55-67 — observe `viewModel.authResult` and navigate on success
    - 57: viewModel.authResult.observe(this) { result ->
    - 58-63: if (result == "SUCCESS") { Toast("Welcome!"); val intent = Intent(this, MainActivity::class.java); intent.flags = NEW_TASK|CLEAR_TASK; startActivity(intent); finish() }
    - 64-66: else -> Toast(result)
  - Lines 70-74 — `launchGoogleSignIn()` which starts Google sign-in intent
    - 72-73: startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
  - Lines 78-86 — `onActivityResult` handles Google sign-in return and passes account to ViewModel
    - 81: val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    - 83: val account = task.getResult(ApiException::class.java)
    - 85: viewModel.loginWithGoogle(account)

4) `AuthViewModel` — exposes `authResult` and routes calls to `AuthRepository`

- `app/src/main/java/com/apollo/thefragments/ui/auth/AuthViewModel.kt`
  - Lines 11-21 define LiveData: `_authResult`, `authResult`, `_isLoading`, `isLoading`.
  - `register(email,password)` lines 23-34
    - 24-26: viewModelScope.launch { _isLoading.postValue(true); val result = repository.registerWithEmail(email,password)
    - 27-30: _isLoading.postValue(false); if (result.isSuccess) _authResult.postValue("SUCCESS") else post error message
  - `login(email,password)` lines 37-47 — similar flow calling `repository.loginWithEmail(...)` (line 40)
  - `loginWithGoogle(account)` lines 50-61 — calls `repository.loginWithGoogle(account)` (line 53)

5) `LoginFragment` UI → ViewModel

- `app/src/main/java/com/apollo/thefragments/ui/auth/LoginFragment.kt`
  - Lines 28-36: ViewModelProvider wired to activity scope (so Activity and fragments share same ViewModel instance)
    - 29-36: viewModel = ViewModelProvider(requireActivity(), (requireActivity() as AuthActivity).let { ... AuthViewModelFactory(repository) })[AuthViewModel::class.java]
  - Lines 38-42: findViewById for email/password buttons and progress bar
  - Lines 44-49 (observe isLoading) — updates UI
  - Lines 51-60: btnLogin.setOnClickListener { ... viewModel.login(email, password) }
    - 59: viewModel.login(email, password)
  - Lines 62-65: btnGoogle.setOnClickListener { (requireActivity() as AuthActivity).launchGoogleSignIn() }
    - 64: this delegates the Google sign-in launch to `AuthActivity.launchGoogleSignIn()`

6) `RegisterFragment` UI → ViewModel

- `app/src/main/java/com/apollo/thefragments/ui/auth/RegisterFragment.kt`
  - Lines 25-32: ViewModel wiring (same pattern as LoginFragment)
  - Lines 34-38: findViewById for register fields
  - Lines 40-43: observe isLoading
  - Lines 45-57: btnRegister.setOnClickListener with validation and `viewModel.register(email, password)`
    - 56: viewModel.register(email, password)

7) `AuthRepository` — Firebase calls + Room writes + session save

- `app/src/main/java/com/apollo/thefragments/repository/AuthRepository.kt`
  - Lines 19: private val firebaseAuth = FirebaseAuth.getInstance()

- Session helpers
  - Lines 24-26: `suspend fun isLoggedIn()` (used by SplashActivity) — returns sessionDao.getSession()?.isLoggedIn == true
  - Lines 29-31: `private suspend fun saveSession(provider: String)` calls sessionDao.saveSession(Session(...))
  - Lines 34-38: `suspend fun logout()` signs out firebase and clears Room tables

- Email register
  - Lines 43-55: `registerWithEmail(email,password)`
    - 45: firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    - 46: val firebaseUser = result.user!!
    - 48-50: userDao.insertUser(User(...))
    - 51: saveSession("email")
    - 52: Result.success("Registered successfully")

- Email login
  - Lines 58-69: `loginWithEmail(email,password)`
    - 60: firebaseAuth.signInWithEmailAndPassword(email, password).await()
    - 61: val firebaseUser = result.user!!
    - 62-64: userDao.insertUser(User(...))
    - 65: saveSession("email")
    - 66: Result.success("Login successful")

- Google sign-in
  - Lines 75-93: `loginWithGoogle(account)`
    - 78: val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    - 79: firebaseAuth.signInWithCredential(credential).await()
    - 80: val firebaseUser = result.user!!
    - 81-87: userDao.insertUser(User(...))
    - 88: saveSession("google")
    - 89: Result.success("Google login successful")

8) `saveSession` details (Room)

- `sessionDao.saveSession(session)` is defined in `SessionDao.kt` lines 12-13
  - 12-13: @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSession(session: Session)
- `Session` entity fields are in `Session.kt` lines 9-15 (id = 1, isLoggedIn flag)

Effect: after any successful register/login, the repository writes user row(s) into `users` table and writes a single-row `session` (id=1) with `isLoggedIn = true`. On next cold start `SplashActivity` will detect this and skip `AuthActivity`.

9) Post-auth navigation → `MainActivity` and `HomeFragment`

- `AuthActivity` observes `viewModel.authResult` and when it sees "SUCCESS" it starts `MainActivity` and finishes itself
  - `AuthActivity.kt` lines 57-63
    - 58-63: if (result == "SUCCESS") { Toast; startActivity(Intent(this, MainActivity)); finish() }

- `MainActivity` onCreate sets up the bottom navigation and fragments
  - `app/src/main/java/com/apollo/thefragments/MainActivity.kt` lines 29-35: initial fragment transaction adds `settingsFragment` (hidden), `profileFragment` (hidden), and `homeFragment` (shown)
    - 29-34: .add(R.id.fragment_container, settingsFragment).hide(settingsFragment) ... .add(R.id.fragment_container, homeFragment)

- `HomeFragment` lifecycle and UI are in `app/src/main/java/com/apollo/thefragments/fragments/HomeFragment.kt`
  - Lines 25-33: onCreateView inflates `fragment_home` and shows a toast
  - Lines 35-47: onViewCreated sets up a button that replaces an inner container with `Step1Fragment`
    - 43-46: childFragmentManager.beginTransaction().replace(R.id.home_inner_container, Step1Fragment()).addToBackStack("step1").commit()

Summary sequences (ordered with exact line references)

A) Register flow (user taps Register → ends at Home)

1. App launched → `AndroidManifest.xml` lines 15-22 choose `SplashActivity` as LAUNCHER.
2. `SplashActivity.onCreate` (SplashActivity.kt lines 21-24, 25-26) creates DB and repository.
3. `SplashActivity` coroutine (lines 28-45) waits 1500ms (line 30) then calls `AuthRepository.isLoggedIn()` (AuthRepository.kt lines 24-26).
4. If not logged in → `SplashActivity` starts `AuthActivity` (SplashActivity.kt lines 36-38, 42-44).
5. `AuthActivity.onCreate` (AuthActivity.kt lines 29-37) wires ViewModel with AuthViewModelFactory.
6. User selects "Register" tab (ViewPager) — `RegisterFragment` inflated (RegisterFragment.kt lines 18-20) and `onViewCreated` runs (lines 22-32) wiring the same ViewModel.
7. User taps Register button → `btnRegister.setOnClickListener` (RegisterFragment.kt lines 45-57): after validation it calls `viewModel.register(email,password)` (line 56).
8. `AuthViewModel.register` (AuthViewModel.kt lines 23-34) launches coroutine and calls `repository.registerWithEmail(email,password)` (line 26).
9. `AuthRepository.registerWithEmail` (AuthRepository.kt lines 43-55): line 45 calls `firebaseAuth.createUserWithEmailAndPassword(...).await()`; on success insert user into Room (lines 48-50) and call `saveSession("email")` (line 51). `saveSession` writes session via `SessionDao.saveSession` (SessionDao.kt lines 12-13) with `isLoggedIn=true`.
10. `registerWithEmail` returns Result.success; `AuthViewModel` posts `_authResult.postValue("SUCCESS")` (AuthViewModel.kt line 29).
11. `AuthActivity` observes `authResult` (AuthActivity.kt lines 55-67). On result == "SUCCESS" (lines 58-63) it starts `MainActivity` and finishes.
12. `MainActivity.onCreate` (MainActivity.kt lines 17-35) adds `homeFragment` and shows it. `HomeFragment.onCreateView` + `onViewCreated` run (HomeFragment.kt lines 25-47) displaying the Home screen.

B) Login flow (email/password)

1-5: same as steps 1-5 above to reach `AuthActivity`.
6. Login tab active → `LoginFragment` inflated and `onViewCreated` runs (LoginFragment.kt lines 21-36).
7. User taps Login → `btnLogin.setOnClickListener` (LoginFragment.kt lines 51-60) validates inputs and calls `viewModel.login(email,password)` (line 59).
8. `AuthViewModel.login` (AuthViewModel.kt lines 37-47) calls `repository.loginWithEmail(email,password)` (line 40).
9. `AuthRepository.loginWithEmail` (AuthRepository.kt lines 58-69): line 60 calls `firebaseAuth.signInWithEmailAndPassword(...).await()`; on success insert user into Room (lines 62-64) and `saveSession("email")` (line 65).
10. Repository returns Result.success; `AuthViewModel` posts `"SUCCESS"` (AuthViewModel.kt line 43) and `AuthActivity` starts `MainActivity` (AuthActivity.kt lines 58-63).
11. `MainActivity` displays `HomeFragment` (MainActivity.kt lines 29-34) and the Home UI is ready (HomeFragment.kt lines 25-47).

C) Google Sign-In flow (from LoginFragment -> Activity -> ViewModel -> Repo)

1-5: same initial steps to open `AuthActivity`.
2. In `LoginFragment`, user taps Google button: `btnGoogle.setOnClickListener` (LoginFragment.kt lines 62-65) calls `(requireActivity() as AuthActivity).launchGoogleSignIn()` (LoginFragment.kt line 64).
3. `AuthActivity.launchGoogleSignIn()` (AuthActivity.kt lines 70-74) starts Google sign-in via `startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)` (line 73).
4. Result returns to `AuthActivity.onActivityResult` (AuthActivity.kt lines 78-86): line 81 `GoogleSignIn.getSignedInAccountFromIntent(data)`; line 83 `val account = task.getResult(ApiException::class.java)`; line 85 `viewModel.loginWithGoogle(account)`.
5. `AuthViewModel.loginWithGoogle` (AuthViewModel.kt lines 50-61) calls `repository.loginWithGoogle(account)` (line 53).
6. `AuthRepository.loginWithGoogle` (AuthRepository.kt lines 75-93): line 78 creates credential; line 79 `firebaseAuth.signInWithCredential(credential).await()`; on success insert user into Room (lines 81-87) and save session with `saveSession("google")` (line 88).
7. `AuthViewModel` posts `"SUCCESS"` and `AuthActivity` starts `MainActivity` (AuthActivity.kt lines 58-63).

Notes and observations

- Navigation here is NOT using the Navigation component; it uses explicit `Intent` from `AuthActivity` → `MainActivity` and `FragmentManager` transactions inside `MainActivity` to show `HomeFragment`.
- Session persistence is based solely on Room's `session` table (id = 1). `SplashActivity` trusts Room (`AuthRepository.isLoggedIn()`) when deciding whether to skip auth.
- Error messages returned by Firebase are surfaced to the user via `authResult` LiveData as the string returned by `exception.message` in `AuthViewModel`.
- Google Sign-In uses `startActivityForResult` legacy API — result handling occurs in `AuthActivity.onActivityResult` where the Google account is passed to the ViewModel.

If you want, I can:
- Add an ASCII sequence diagram showing the exact function calls and file:line references.
- Generate a simplified flowchart or Mermaid diagram.
- Extend this README to include screenshots of the XML layouts and the view IDs used (so testers know which UI element triggers which code line).

---
Generated by scanning project files and mapping call sites to concrete lines. If you'd like this written elsewhere (e.g., `docs/AUTH_FLOW.md`) or include additional modules (logout flow, profile population), tell me where to write it and I'll update the file.
