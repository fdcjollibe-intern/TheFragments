package com.apollo.thefragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class HomeFragment : Fragment() {

    private val tag = "🏠"
    private var localCount = 0
    private lateinit var viewModel: CounterViewModel

    // This Handler + ticker only drives the LEFT and RIGHT counters.
    // It lives in the Fragment so it starts/stops with the Fragment's visibility.
    // → started in onStart (fragment visible)
    // → stopped in onStop  (fragment off-screen)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvLeft: TextView
    private lateinit var tvRight: TextView
    private lateinit var tvNonstop: TextView

    private val ticker = object : Runnable {
        override fun run() {
            // LEFT: increments the fragment-local variable.
            // This will reset to 0 next time the Fragment is recreated.
            localCount++
            tvLeft.text = localCount.toString()

            // RIGHT: increments the ViewModel variable.
            // When Fragment is destroyed, this value stays in the ViewModel.
            // When Fragment comes back, it shows the saved value and resumes.
            viewModel.survivingCount++
            tvRight.text = viewModel.survivingCount.toString()

            handler.postDelayed(this, 1000)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Toast.makeText(context, "$tag → onAttach: Fragment attached to Activity", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localCount = 0

        // RIGHT + BOTTOM: get the ViewModel from the Activity.
        // First visit  → Android creates a new CounterViewModel (nonstop ticker starts in init{})
        // Return visit → Android returns the SAME ViewModel already running
        viewModel = ViewModelProvider(requireActivity())[CounterViewModel::class.java]

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

        tvLeft    = view.findViewById(R.id.tv_counter_left)
        tvRight   = view.findViewById(R.id.tv_counter_right)
        tvNonstop = view.findViewById(R.id.tv_counter_nonstop)

        // LEFT: always 0 on arrival — fragment was just recreated.
        tvLeft.text = localCount.toString()

        // RIGHT: shows the real saved value from ViewModel — picks up where it left off.
        tvRight.text = viewModel.survivingCount.toString()

        // BOTTOM: show current nonstop count immediately on arrival.
        tvNonstop.text = viewModel.nonstopCount.toString()

        // Give the ViewModel a callback so its internal ticker can push
        // updates to our TextView while this Fragment is alive and visible.
        // When the Fragment is destroyed, we set this to null (in onDestroyView)
        // so the ViewModel doesn't try to update a dead view.
        viewModel.onNonstopTick = { newValue ->
            tvNonstop.text = newValue.toString()
        }

        Toast.makeText(requireContext(), "$tag → onViewCreated: View is ready", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        handler.post(ticker)
        Toast.makeText(requireContext(), "$tag → onStart: Fragment visible — left/right ticker started", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(requireContext(), "$tag → onResume: Fragment interactive", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(requireContext(), "$tag → onPause: Fragment losing focus", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(ticker)
        Toast.makeText(requireContext(), "$tag → onStop: Fragment stopped — left/right ticker paused", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onNonstopTick = null
        Toast.makeText(requireContext(), "$tag → onDestroyView: View destroyed — nonstop callback disconnected", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(requireContext(), "$tag → onDestroy: Fragment destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onDetach() {
        super.onDetach()
        Toast.makeText(requireActivity().applicationContext, "$tag → onDetach: Detached from Activity", Toast.LENGTH_SHORT).show()
    }
}