package com.alvaroquintana.adivinabandera.application.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

/**
 * Concrete [MetroViewModelFactory] wired into [AppGraph] via the [ViewModelGraph]
 * interface. Receives the multi-binding maps populated by every
 * `@ContributesIntoMap @ViewModelKey` ViewModel in the codebase.
 *
 * Also bound as a [ViewModelProvider.Factory] so it can be installed in
 * Compose's `LocalMetroViewModelFactory` and used directly as the default
 * factory if needed.
 */
@ContributesBinding(AppScope::class)
@ContributesBinding(AppScope::class, binding<ViewModelProvider.Factory>())
@SingleIn(AppScope::class)
@Inject
class AdivinaViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>,
) : MetroViewModelFactory()
