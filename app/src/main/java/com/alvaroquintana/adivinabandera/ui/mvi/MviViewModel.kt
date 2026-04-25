package com.alvaroquintana.adivinabandera.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pragmatic MVI base for AdivinaBandera ViewModels.
 *
 * **Three primitives:**
 * - [state] — the reactive [StateFlow] of the screen's `UiState`.
 * - [events] — a [SharedFlow] of one-shot side effects (navigation, toasts,
 *   dialog dismissals that need to leave the VM scope).
 * - [dispatch] — the single entry point for the UI to drive the VM.
 *
 * **Why pragmatic and not canonical:** in canonical MVI every input is an
 * [Intent], including parameterised initialisations (e.g. `Intent.Init(gameMode)`).
 * That forces a sealed `Init` variant per VM that exists only to forward typed
 * runtime parameters, which adds ceremony without changing semantics.
 * AdivinaBandera keeps **typed boot methods** (e.g. [com.alvaroquintana.adivinabandera.ui.result.ResultViewModel.initWithGameMode])
 * for those one-shot startup calls, and routes everything else through
 * [dispatch].
 *
 * **What the base does NOT impose:** there is no pure `reduce(state, intent)`
 * function. State updates and side effects (calls to use cases, coroutines,
 * timers) often need to share a transaction; forcing the reducer/effect split
 * for screens that don't need replay or time-travel debugging would only add
 * boilerplate. Subclasses use [updateState] for reductions and [emit] for
 * one-shots, both inside [handleIntent].
 *
 * @param S the immutable `UiState` data class for the screen.
 * @param I the sealed `Intent` (or `Action`) hierarchy the screen accepts.
 * @param E the sealed one-shot event hierarchy (typically `Navigation`,
 *          `Toast`, `Dialog`).
 */
abstract class MviViewModel<S : Any, I : Any, E : Any>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<E> = _events.asSharedFlow()

    /**
     * Single UI entry point. Dispatches inside [viewModelScope] so callers can
     * stay synchronous on the UI thread.
     */
    fun dispatch(intent: I) {
        viewModelScope.launch { handleIntent(intent) }
    }

    /**
     * Implement to map [intent]s to state updates ([updateState]) and / or
     * one-shot side effects ([emit]). Side effects that need their own
     * coroutine should use [viewModelScope.launch] inside this function.
     */
    protected abstract suspend fun handleIntent(intent: I)

    /** Reduce-and-publish atomically. Safe under concurrent dispatchers. */
    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    /** Current snapshot of state — useful when an intent's effect depends on the previous state. */
    protected val currentState: S get() = _state.value

    /** Emit a one-shot event. Drops the oldest if the consumer hasn't subscribed yet. */
    protected fun emit(event: E) {
        _events.tryEmit(event)
    }
}
