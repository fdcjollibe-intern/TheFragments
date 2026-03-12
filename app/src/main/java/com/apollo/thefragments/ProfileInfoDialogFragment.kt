package com.apollo.thefragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment

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
        Toast.makeText(requireContext(), "$tag2 → onViewCreated: Dialog view ready", Toast.LENGTH_SHORT).show()


        view.findViewById<Button>(R.id.btn_close_dialog).setOnClickListener {
            Toast.makeText(requireContext(), "$tag2 → Removing via fragmentManager.commit remove()", Toast.LENGTH_SHORT).show()

            // Using parentFragmentManager (ProfileFragment's manager) to REMOVE this dialog
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(requireContext(), "$tag2 → onStart: Dialog visible", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(requireContext(), "$tag2 → onResume: Dialog interactive", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(requireContext(), "$tag2 → onPause: Dialog pausing", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        Toast.makeText(requireContext(), "$tag2 → onStop: Dialog stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Toast.makeText(requireContext(), "$tag2 → onDestroyView: Dialog view destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(requireContext(), "$tag2 → onDestroy: Dialog destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onDetach() {
        super.onDetach()
        Toast.makeText(requireActivity().applicationContext, "$tag2 → onDetach: Dialog detached", Toast.LENGTH_SHORT).show()
    }
}