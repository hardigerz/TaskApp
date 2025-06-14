package com.testhar.taskapp.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Observes a [StateFlow] in a lifecycle-aware manner within a [LifecycleOwner],
 * typically used in Activities or Fragments.
 *
 * This function collects from the [StateFlow] only when the lifecycle is at least [Lifecycle.State.CREATED],
 * and automatically cancels the collection when the lifecycle is destroyed.
 *
 * @param viewLifecycleOwner The [LifecycleOwner] (usually Fragment or Activity) whose lifecycle should control the observation.
 * @param bloc The lambda function to be invoked with each emitted value from the StateFlow.
 *
 * @sample
 * ```
 * viewModel.someStateFlow.observe(viewLifecycleOwner) { state ->
 *     // React to new state
 * }
 * ```
 *
 * Internally, this uses a [DefaultLifecycleObserver] to manage collection and cancellation tied to the component's lifecycle.
 */
fun <T> StateFlow<T>.observe(
    viewLifecycleOwner: LifecycleOwner,
    bloc: (T) -> Unit
) {
    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        var job: Job? = null

        override fun onCreate(owner: LifecycleOwner) {
            job = viewLifecycleOwner.lifecycleScope.launch {
                this@observe.collect {
                    ensureActive()
                    bloc(it)
                }
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            job?.cancel()
            viewLifecycleOwner.lifecycle.removeObserver(this)
        }
    })
}