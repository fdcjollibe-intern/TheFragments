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

class ProfileFragment : Fragment() {

    private val tag = "👤"

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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(requireContext(), "$tag → onViewCreated: View is ready", Toast.LENGTH_SHORT).show()

        view.findViewById<Button>(R.id.btn_show_dialog).setOnClickListener {
            Toast.makeText(requireContext(), "$tag → ADD-ing ProfileInfoDialogFragment via childFragmentManager", Toast.LENGTH_SHORT).show()

            childFragmentManager.beginTransaction()
                .add(
                    R.id.dialog_container,
                    ProfileInfoDialogFragment(),
                    "ProfileInfoDialog"
                )
                .addToBackStack("show_dialog")
                .commit()

        }
    }
}