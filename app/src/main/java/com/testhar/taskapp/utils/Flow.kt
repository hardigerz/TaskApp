/**
 *  Copyright (c) 2025 PT. Bank Danamon Indonesia Tbk Private License.
 *
 *  Permission is hereby granted to employees and affiliates of PT. Bank Danamon Indonesia Tbk
 *  (the enterprise) to use the DBank Pro source code within the enterprise's private repositories
 *  for internal business purposes only.
 *
 *  Redistribution of the source code outside of the enterprise's private repositories is
 *  prohibited. The source code may not be shared, sold, or transferred to any third party.
 *
 *  The source code may be modified for internal use within the enterprise. All modified
 *  versions of the source code remain the property of the enterprise.
 *
 *  The source code may include open-source third-party components, each of which is subject
 *  to its respective open-source license. The terms of each open-source license apply to the
 *  corresponding component and prevail over this license with respect to that component.
 *
 *  All proprietary rights, including intellectual property rights, remain with the enterprise.
 *  Use of the enterprise's name, logo, or trademarks is not permitted.
 *
 *  The source code is provided "as is," without warranty of any kind. The enterprise shall not
 *  be liable for any damages arising from the use of the source code.
 *
 *  This license is effective until terminated. The enterprise reserves the right to terminate
 *  this license if its terms are violated.
 *
 *  By using this source code, you acknowledge that you have read, understood, and agree to be
 *  bound by the terms and conditions of this license.
 *
 *
 *  Created by PT. Bank Danamon Indonesia Tbk on 2025-06-14.
 */
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