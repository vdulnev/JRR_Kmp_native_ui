package com.jrr.jrrkmp_native_ui.presentation.navigation

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.MainShellSettings

/**
 * Top-level navigation: the five tabs as a `ChildStack` manipulated with
 * [bringToFront]. This gives the tab-bar idiom — switching tabs keeps each
 * tab's sub-state alive at the bottom of the stack and re-orders rather than
 * recreating, mirroring the old behaviour where leaving Library and returning
 * preserved the selected album (`MainShellViewModel.kt:165-185`).
 *
 * Scope: navigation only. Auto-connect / toast / connection business logic
 * stays in `MainShellViewModel` for now (relocated in a later phase); its sole
 * navigation side-effect — "flip to Player on connect" — is expressed here via
 * [onConnectSuccess] / [onAutoConnected].
 */
class RootComponent(
    componentContext: ComponentContext,
    private val deps: AppDeps,
    initialConfig: RootConfig,
) : ComponentContext by componentContext {

    private val log = Logger.withTag("vm:RootNav")

    private val nav = StackNavigation<RootConfig>()

    val stack: Value<ChildStack<RootConfig, RootChild>> =
        childStack(
            source = nav,
            serializer = RootConfig.serializer(),
            initialConfiguration = initialConfig,
            handleBackButton = true,
            childFactory = ::child,
        )

    init {
        log.d { "init initialConfig=$initialConfig" }
    }

    sealed interface RootChild {
        class Library(val component: LibraryComponent) : RootChild
        class Server(val component: ServerComponent) : RootChild
        class Player(val component: PlayerComponent) : RootChild
        class Zones(val component: ZonesComponent) : RootChild
        class Settings(val component: SettingsComponent) : RootChild
    }

    private fun child(config: RootConfig, ctx: ComponentContext): RootChild =
        when (config) {
            RootConfig.Library -> RootChild.Library(LibraryComponent(ctx, deps))
            RootConfig.Server -> RootChild.Server(ServerComponent(ctx))
            RootConfig.Player -> RootChild.Player(PlayerComponent(ctx, deps))
            RootConfig.Zones -> RootChild.Zones(ZonesComponent(ctx, deps))
            RootConfig.Settings -> RootChild.Settings(SettingsComponent(ctx, deps))
        }

    /**
     * Switch active tab. Ports `MainShellViewModel.selectTab` including the
     * "tap the already-active Library tab => pop its inner stack to root"
     * gesture. Switching away from a tab and back preserves that tab's
     * sub-stack because [bringToFront] re-orders rather than recreating.
     */
    fun selectTab(config: RootConfig) {
        val active = stack.value.active.configuration
        log.d { "selectTab($config) active=$active" }
        if (config == RootConfig.Library && active == RootConfig.Library) {
            (stack.value.active.instance as? RootChild.Library)?.component?.popToRoot()
            return
        }
        nav.bringToFront(config)
    }

    /** ServerManager connect success → Player tab. */
    fun onConnectSuccess() {
        log.d { "onConnectSuccess()" }
        selectTab(RootConfig.Player)
    }

    /** Auto-connect success → Player tab. */
    fun onAutoConnected() {
        log.d { "onAutoConnected()" }
        selectTab(RootConfig.Player)
    }

    /** Disconnect → Player/offline tab. Business teardown (facade) stays in the host. */
    fun onDisconnect() {
        log.d { "onDisconnect()" }
        selectTab(RootConfig.Player)
    }

    companion object {
        /**
         * Always start on the Player tab (offline or connected). The Server
         * screen is only entered explicitly via Settings → Connect, so the user
         * never lands on a "login wall" on cold start or after logout.
         */
        @Suppress("UNUSED_PARAMETER")
        fun initialConfig(settings: MainShellSettings): RootConfig = RootConfig.Player
    }
}
