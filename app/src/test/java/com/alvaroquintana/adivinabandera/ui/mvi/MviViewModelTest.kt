package com.alvaroquintana.adivinabandera.ui.mvi

import app.cash.turbine.test
import com.alvaroquintana.adivinabandera.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the [MviViewModel] base class. Locks down the contract
 * every screen ViewModel relies on:
 * - state starts at the initial value passed to the constructor
 * - dispatch() routes through handleIntent
 * - updateState produces a new immutable snapshot
 * - emit() reaches subscribed collectors
 * - currentState always sees the latest value
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private data class TestState(val counter: Int = 0, val label: String = "")

    private sealed class TestIntent {
        object Increment : TestIntent()
        data class SetLabel(val label: String) : TestIntent()
        data class Notify(val message: String) : TestIntent()
    }

    private sealed class TestEvent {
        data class Toast(val message: String) : TestEvent()
    }

    private class FakeViewModel : MviViewModel<TestState, TestIntent, TestEvent>(TestState()) {
        var lastSeenSnapshot: TestState? = null
            private set

        override suspend fun handleIntent(intent: TestIntent) {
            lastSeenSnapshot = currentState
            when (intent) {
                TestIntent.Increment -> updateState { it.copy(counter = it.counter + 1) }
                is TestIntent.SetLabel -> updateState { it.copy(label = intent.label) }
                is TestIntent.Notify -> emit(TestEvent.Toast(intent.message))
            }
        }
    }

    @Test
    fun `state starts at the initial value passed to the constructor`() {
        val vm = FakeViewModel()
        assertEquals(TestState(counter = 0, label = ""), vm.state.value)
    }

    @Test
    fun `dispatch routes through handleIntent and updateState publishes a new snapshot`() = runTest {
        val vm = FakeViewModel()

        vm.dispatch(TestIntent.Increment)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.counter)
    }

    @Test
    fun `updateState preserves immutability — each emission is a new instance`() = runTest {
        val vm = FakeViewModel()
        val first = vm.state.value
        vm.dispatch(TestIntent.SetLabel("hello"))
        advanceUntilIdle()
        val second = vm.state.value

        assertNotEquals(first, second)
        assertEquals("hello", second.label)
        assertEquals("", first.label) // first snapshot must not be mutated
    }

    @Test
    fun `emit reaches subscribed collectors`() = runTest {
        val vm = FakeViewModel()

        vm.events.test {
            vm.dispatch(TestIntent.Notify("ping"))
            assertEquals(TestEvent.Toast("ping"), awaitItem())
        }
    }

    @Test
    fun `currentState exposed to handleIntent reflects the latest update`() = runTest {
        val vm = FakeViewModel()

        vm.dispatch(TestIntent.Increment)
        vm.dispatch(TestIntent.Increment)
        vm.dispatch(TestIntent.Increment)
        advanceUntilIdle()

        // After 3 Increments the snapshot the third handler saw should be 2 (just before its own update).
        assertEquals(2, vm.lastSeenSnapshot?.counter)
        assertEquals(3, vm.state.value.counter)
    }

    @Test
    fun `multiple intents accumulate state changes in dispatch order`() = runTest {
        val vm = FakeViewModel()

        vm.dispatch(TestIntent.Increment)
        vm.dispatch(TestIntent.SetLabel("a"))
        vm.dispatch(TestIntent.Increment)
        vm.dispatch(TestIntent.SetLabel("b"))
        advanceUntilIdle()

        assertEquals(TestState(counter = 2, label = "b"), vm.state.value)
    }
}
