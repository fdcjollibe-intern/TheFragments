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

class RegisterFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            requireActivity(),
            (requireActivity() as AuthActivity).let {
                val db         = com.apollo.thefragments.data.db.AppDatabase.getDatabase(requireContext())
                val repository = com.apollo.thefragments.repository.AuthRepository(db.userDao(), db.sessionDao())
                AuthViewModelFactory(repository)
            }
        )[AuthViewModel::class.java]

        val etEmail     = view.findViewById<EditText>(R.id.et_reg_email)
        val etPassword  = view.findViewById<EditText>(R.id.et_reg_password)
        val etConfirm   = view.findViewById<EditText>(R.id.et_reg_confirm)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_reg)

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnRegister.isEnabled  = !loading
        }

        btnRegister.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm  = etConfirm.text.toString().trim()

            // Basic validation
            if (email.isEmpty())    { etEmail.error    = "Required"; return@setOnClickListener }
            if (password.isEmpty()) { etPassword.error = "Required"; return@setOnClickListener }
            if (password.length < 6) { etPassword.error = "Min 6 characters"; return@setOnClickListener }
            if (password != confirm) { etConfirm.error = "Passwords do not match"; return@setOnClickListener }

            viewModel.register(email, password)
        }
    }
}
