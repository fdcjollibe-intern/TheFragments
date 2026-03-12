package com.apollo.thefragments.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apollo.thefragments.R

class Step3Fragment : Fragment() {

    private val tag = "3️⃣"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Toast.makeText(context, "$tag → onAttach: Step3 attached", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(requireContext(), "$tag → onCreate: Step3 created", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Toast.makeText(requireContext(), "$tag → onCreateView: Inflating Step3 layout", Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.fragment_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step3 hosts TWO child fragments side by side.
        // We use childFragmentManager (Step3's own manager) for these two.
        // They are children of Step3, not of HomeFragment.
        //
        // We only add them if this is the first time (savedInstanceState == null).
        // If the user switches tabs and comes back, Step3 is recreated but
        // savedInstanceState is NOT null — Android already restores the child
        // fragments automatically, so we must NOT add them again or they'd duplicate.
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.step3_container_left, ColorAFragment())
                .add(R.id.step3_container_right, ColorBFragment())
                .commit()
        }

        Toast.makeText(requireContext(), "$tag → onViewCreated: Step3 view ready — adding 2 child fragments", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(requireContext(), "$tag → onStart: Step3 visible", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(requireContext(), "$tag → onResume: Step3 interactive", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(requireContext(), "$tag → onPause: Step3 losing focus", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        Toast.makeText(requireContext(), "$tag → onStop: Step3 stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Toast.makeText(requireContext(), "$tag → onDestroyView: Step3 view destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(requireContext(), "$tag → onDestroy: Step3 destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onDetach() {
        super.onDetach()
        Toast.makeText(requireActivity().applicationContext, "$tag → onDetach: Step3 detached", Toast.LENGTH_SHORT).show()
    }
}
