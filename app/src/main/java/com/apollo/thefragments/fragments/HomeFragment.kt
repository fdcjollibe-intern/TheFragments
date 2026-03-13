package com.apollo.thefragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apollo.thefragments.fragments.Step1Fragment

class HomeFragment : Fragment() {

    private val tag = "🏠"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Toast.makeText(context, "$tag → onAttach: Fragment attached to Activity", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(requireContext(), "$tag → onCreate: Fragment created", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(requireContext(), "$tag → onCreateView: Inflating layout", Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_go_step1).setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(R.id.home_inner_container, Step1Fragment())
                // addToBackStack saves this transaction so back press can undo it
                .addToBackStack("step1")
                .commit()
        }

        Toast.makeText(requireContext(), "$tag → onViewCreated: View is ready", Toast.LENGTH_SHORT).show()
    }

    // ─────────────────────────────────────────────────────────────
    // Called by MainActivity's onBackPressed().
    // Returns true  → we consumed the back press (popped an inner fragment)
    // Returns false → nothing to pop, MainActivity should handle it (show exit dialog)
    // ─────────────────────────────────────────────────────────────
    fun handleBackPress(): Boolean {
        return if (childFragmentManager.backStackEntryCount > 0) {
            // There are inner fragments on the stack (Step1, Step2, Step3)
            // Pop the top one and go back one level
            childFragmentManager.popBackStack()
            true
        } else {
            // Inner stack is empty — we are at HomeFragment's own content (the button screen)
            // Tell MainActivity to show the exit dialog
            false
        }
    }
}
