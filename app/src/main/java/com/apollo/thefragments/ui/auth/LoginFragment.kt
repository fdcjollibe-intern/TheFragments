package com.apollo.thefragments.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.apollo.thefragments.R

class LoginFragment : Fragment() {

    // Fragments inside ViewPager2 share the Activity's ViewModel —
    // so both LoginFragment and RegisterFragment post to the same authResult.
    // The Activity observes it and handles navigation.
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use requireActivity() so we get the SAME ViewModel instance as AuthActivity
        viewModel = ViewModelProvider(
            requireActivity(),
            (requireActivity() as AuthActivity).let {
                val db         = com.apollo.thefragments.data.db.AppDatabase.getDatabase(requireContext())
                val repository = com.apollo.thefragments.repository.AuthRepository(db.userDao(), db.sessionDao())
                AuthViewModelFactory(repository)
            }
        )[AuthViewModel::class.java]

        val etEmail    = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnLogin   = view.findViewById<Button>(R.id.btn_login)
        val btnGoogle  = view.findViewById<Button>(R.id.btn_google)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)

        // Show/hide progress bar based on loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnLogin.isEnabled  = !loading
            btnGoogle.isEnabled = !loading
        }

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                etEmail.error    = if (email.isEmpty()) "Required" else null
                etPassword.error = if (password.isEmpty()) "Required" else null
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        // Delegate Google Sign-In launch to the Activity (it owns googleSignInClient)
        btnGoogle.setOnClickListener {
            (requireActivity() as AuthActivity).launchGoogleSignIn()
        }
    }
}
