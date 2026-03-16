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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_info_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_close_dialog).setOnClickListener {
            // Using parentFragmentManager (ProfileFragment's manager) to REMOVE this dialog
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }
    }
}