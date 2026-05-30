package com.jrr.jrrkmp_native_ui.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate

/**
 * Bridges an AndroidX [ViewModel] into Essenty's [InstanceKeeper] so the VM is
 * retained across configuration changes (and, with a wired StateKeeper, process
 * death) and is deterministically `onCleared()`-ed when the owning component
 * leaves the navigation tree.
 *
 * This replaces the old `remember { … }` (Android) / `@State` (iOS) creation:
 * those leaked the VM's `viewModelScope` because `onCleared()` was never called.
 * Holding the VM in a [ViewModelStore] and clearing the store on
 * [InstanceKeeper.Instance.onDestroy] gives us a single, correct teardown path.
 */
internal class VmStoreInstance : InstanceKeeper.Instance {
    val viewModelStore = ViewModelStore()
    override fun onDestroy() {
        viewModelStore.clear()
    }
}

/**
 * Retrieve (or lazily create + retain) a feature [ViewModel] tied to this
 * component's lifecycle. Idempotent: repeated calls with the same [key] return
 * the same instance for as long as the component is alive.
 *
 * @param key stable identity for the VM (e.g. `"library"`, `"albumDetail:<id>"`).
 * @param factory builds the VM the first time it is needed.
 */
internal inline fun <reified VM : ViewModel> InstanceKeeperOwner.retainedViewModel(
    key: String,
    crossinline factory: () -> VM,
): VM {
    val holder = instanceKeeper.getOrCreate("vmstore:$key") { VmStoreInstance() }
    val provider = ViewModelProvider.create(
        store = holder.viewModelStore,
        factory = viewModelFactory { initializer { factory() } },
    )
    return provider[key, VM::class]
}
