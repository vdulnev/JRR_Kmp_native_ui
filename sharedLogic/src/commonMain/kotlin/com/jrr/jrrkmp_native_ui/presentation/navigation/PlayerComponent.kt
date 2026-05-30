package com.jrr.jrrkmp_native_ui.presentation.navigation

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.NowPlayingViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.QueueViewModel
import kotlinx.serialization.Serializable

/**
 * Player tab: a two-entry stack [NowPlaying] → [Queue], replacing the old
 * `showQueue` boolean overlay (`MainActivity.kt:346`, `ContentView.swift`
 * `PlayerTabContainerView`).
 */
class PlayerComponent(
    componentContext: ComponentContext,
    private val deps: AppDeps,
) : ComponentContext by componentContext {

    private val log = Logger.withTag("vm:PlayerNav")

    @Serializable
    private sealed interface Config {
        @Serializable
        data object NowPlaying : Config

        @Serializable
        data object Queue : Config
    }

    private val nav = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = nav,
            serializer = Config.serializer(),
            initialConfiguration = Config.NowPlaying,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, ctx: ComponentContext): Child =
        when (config) {
            Config.NowPlaying -> Child.NowPlaying(ctx, deps)
            Config.Queue -> Child.Queue(ctx, deps)
        }

    sealed interface Child {
        class NowPlaying internal constructor(
            ctx: ComponentContext,
            deps: AppDeps,
        ) : Child {
            val vm: NowPlayingViewModel by lazy {
                ctx.retainedViewModel("nowPlaying") { deps.nowPlayingViewModel() }
            }
        }

        class Queue internal constructor(
            ctx: ComponentContext,
            deps: AppDeps,
        ) : Child {
            val vm: QueueViewModel by lazy {
                ctx.retainedViewModel("queue") { deps.queueViewModel() }
            }
        }
    }

    /** Show the queue. Ports `setShowQueue(true)`. */
    @OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class)
    fun openQueue() {
        log.d { "openQueue()" }
        nav.push(Config.Queue)
    }

    /** Back to now-playing. Ports `setShowQueue(false)`. */
    fun closeQueue() {
        log.d { "closeQueue()" }
        nav.pop()
    }
}
