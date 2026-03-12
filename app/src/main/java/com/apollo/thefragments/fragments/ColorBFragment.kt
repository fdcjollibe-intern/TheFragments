package com.apollo.thefragments.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apollo.thefragments.R

class ColorBFragment : Fragment() {

    private val tag = "🟦"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(requireContext(), "$tag → onCreate: ColorB created", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Toast.makeText(requireContext(), "$tag → onCreateView: Inflating ColorB", Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.fragment_color_b, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(requireContext(), "$tag → onViewCreated: ColorB ready", Toast.LENGTH_SHORT).show()
    }

}
