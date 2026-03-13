package com.apollo.thefragments.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apollo.thefragments.R

class Step1Fragment : Fragment() {

    private val tag = "1️⃣"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Toast.makeText(context, "$tag → onAttach: Step1 attached", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(requireContext(), "$tag → onCreate: Step1 created", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Toast.makeText(requireContext(), "$tag → onCreateView: Inflating Step1 layout", Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.fragment_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Push Step2 onto the SAME childFragmentManager that HomeFragment owns.
        // parentFragmentManager here is HomeFragment's childFragmentManager —
        // that is how we keep the whole stack inside HomeFragment.
        view.findViewById<Button>(R.id.btn_go_step2).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.home_inner_container, Step2Fragment())
                .addToBackStack("step2")
                .commit()
        }

        Toast.makeText(requireContext(), "$tag → onViewCreated: Step1 view ready", Toast.LENGTH_SHORT).show()
    }
}
