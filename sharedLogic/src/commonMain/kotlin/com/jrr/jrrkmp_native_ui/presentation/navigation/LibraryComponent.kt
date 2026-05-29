package com.jrr.jrrkmp_native_ui.presentation.navigation

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popWhile
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.AlbumDetailViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel
import kotlinx.serialization.Serializable

/**
 * Library tab: a two-entry stack [List] → [Detail], replacing the old
 * `selectedAlbum != null` overlay flag (`MainActivity.kt:304`,
 * `ContentView.swift` `LibraryTabContainerView`).
 *
 * `Album` is `@Serializable`, so the selected album rides inside the [Config]
 * and survives process death for free.
 */
class LibraryComponent(
    componentContext: ComponentContext,
    private val deps: AppDeps,
) : ComponentContext by componentContext {

    private val log = Logger.withTag("vm:LibraryNav")

    @Serializable
    private sealed interface Config {
        @Serializable
        data object List : Config

        @Serializable
        data class Detail(val album: Album) : Config
    }

    private val nav = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = nav,
            serializer = Config.serializer(),
            initialConfiguration = Config.List,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, ctx: ComponentContext): Child =
        when (config) {
            Config.List -> Child.List(ctx, deps)
            is Config.Detail -> Child.Detail(ctx, deps, config.album)
        }

    /**
     * Library children. Each holds its child [ComponentContext] and builds its VM
     * lazily via [retainedViewModel] on first access — so merely routing to a
     * child does not construct (or leak) a ViewModel, and navigation stays
     * unit-testable with throwing factories.
     */
    sealed interface Child {
        class List internal constructor(
            ctx: ComponentContext,
            deps: AppDeps,
        ) : Child {
            val vm: LibraryViewModel by lazy {
                ctx.retainedViewModel("library") { deps.libraryViewModel() }
            }
        }

        class Detail internal constructor(
            ctx: ComponentContext,
            deps: AppDeps,
            val album: Album,
        ) : Child {
            val vm: AlbumDetailViewModel by lazy {
                ctx.retainedViewModel("albumDetail:${album.albumGroupId}") {
                    deps.albumDetailViewModel(album)
                }
            }
        }
    }

    /** Open an album's detail view. Ports `selectAlbum(album)`. */
    @OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class)
    fun openAlbum(album: Album) {
        log.d { "openAlbum(${album.name})" }
        nav.push(Config.Detail(album))
    }

    /** Back from detail to list. Ports `selectAlbum(null)`. */
    fun back() {
        log.d { "back()" }
        nav.pop()
    }

    /** Tap-active-tab idiom: collapse the inner stack to the list root. */
    fun popToRoot() {
        log.d { "popToRoot()" }
        nav.popWhile { it !is Config.List }
    }
}
