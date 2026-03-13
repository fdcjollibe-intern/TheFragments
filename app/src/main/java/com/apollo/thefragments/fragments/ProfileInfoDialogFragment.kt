package com.apollo.thefragments.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.apollo.thefragments.R

class ProfileInfoDialogFragment : DialogFragment() {

    private val tag2 = "\uD83D\uDCAC"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Toast.makeText(context, "$tag2 → onAttach: Dialog attached", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(requireContext(), "$tag2 → onCreate: Dialog created", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(requireContext(), "$tag2 → onCreateView: Inflating dialog layout", Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.fragment_profile_info_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(
            requireContext(),
            "$tag2 → onViewCreated: Dialog view ready",
            Toast.LENGTH_SHORT
        ).show()


        view.findViewById<Button>(R.id.btn_close_dialog).setOnClickListener {
            Toast.makeText(
                requireContext(),
                "$tag2 → Removing via fragmentManager.commit remove()",
                Toast.LENGTH_SHORT
            ).show()

            // Using parentFragmentManager (ProfileFragment's manager) to REMOVE this dialog
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }
    }
}