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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

    }
}
