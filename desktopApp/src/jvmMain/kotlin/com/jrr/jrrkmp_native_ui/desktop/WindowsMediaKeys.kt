package com.jrr.jrrkmp_native_ui.desktop

import co.touchlab.kermit.Logger
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import java.awt.EventQueue

/**
 * Global hardware media-key support for the Windows desktop host: the keyboard's
 * Play/Pause, Next Track and Previous Track keys drive [com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade].
 *
 * Why a global low-level hook and not Compose `onKeyEvent`: Windows delivers the
 * dedicated media keys as `WM_APPCOMMAND`, which the JVM/AWT never translates into
 * `KeyEvent`s — so a focused Compose window simply never sees them. A
 * `WH_KEYBOARD_LL` hook (installed via the JNA bindings VLCJ already puts on the
 * classpath — see [DesktopPlayerEngine]) is the supported way to observe these
 * virtual keys, and being global it works whether or not the app window is
 * focused, matching how media keys behave everywhere else.
 *
 * The hook is the desktop analogue of the iOS `MPRemoteCommandCenter`
 * registration (`NowPlayingCoordinator`): both forward the hardware transport
 * keys to the same shared facade.
 *
 * Threading: a low-level hook is serviced by the message queue of the thread that
 * installed it, so the hook lives on a dedicated daemon thread running a Win32
 * `GetMessage` loop. Hook callbacks must return fast (Windows drops a hook that
 * exceeds `LowLevelHooksTimeout`), so the playback action is marshalled onto the
 * AWT event-dispatch thread — where the rest of the desktop app drives the
 * facade — and the callback returns immediately.
 *
 * No-op on non-Windows hosts (macOS/Linux desktop would need their own
 * mechanism), so it is safe to construct and [start] unconditionally.
 */
class WindowsMediaKeys {
    private val log = Logger.withTag("ui:Desktop:MediaKeys")

    private val isWindows: Boolean =
        System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)

    private var hookThread: Thread? = null
    private var hHook: WinUser.HHOOK? = null
    private var win32ThreadId: Int = 0

    // Strong reference to the JNA callback: without it the proc can be GC'd while
    // Windows still holds the native pointer, which crashes on the next key.
    private var keyboardProc: WinUser.LowLevelKeyboardProc? = null

    @Volatile private var onPlayPause: () -> Unit = {}
    @Volatile private var onNext: () -> Unit = {}
    @Volatile private var onPrevious: () -> Unit = {}

    /**
     * Install the global media-key hook. Idempotent; no-op on non-Windows hosts.
     * The three callbacks are invoked on the AWT EDT.
     */
    fun start(
        onPlayPause: () -> Unit,
        onNext: () -> Unit,
        onPrevious: () -> Unit,
    ) {
        if (!isWindows) {
            log.d { "media-key hook skipped (non-Windows host)" }
            return
        }
        if (hookThread != null) {
            log.w { "media-key hook already installed" }
            return
        }
        this.onPlayPause = onPlayPause
        this.onNext = onNext
        this.onPrevious = onPrevious

        val thread = Thread({ runHookLoop() }, "win-media-keys").apply { isDaemon = true }
        hookThread = thread
        thread.start()
    }

    /** Remove the hook and stop the message loop. Idempotent; no-op on non-Windows. */
    fun stop() {
        if (!isWindows) return
        hHook?.let {
            User32.INSTANCE.UnhookWindowsHookEx(it)
            hHook = null
            log.i { "media-key hook removed" }
        }
        if (win32ThreadId != 0) {
            // Break the blocking GetMessage loop on the hook thread.
            User32.INSTANCE.PostThreadMessage(win32ThreadId, WM_QUIT, WinDef.WPARAM(0), WinDef.LPARAM(0))
            win32ThreadId = 0
        }
        hookThread = null
        keyboardProc = null
    }

    private fun runHookLoop() {
        win32ThreadId = Kernel32.INSTANCE.GetCurrentThreadId()

        val proc = object : WinUser.LowLevelKeyboardProc {
            override fun callback(
                nCode: Int,
                wParam: WinDef.WPARAM,
                info: WinUser.KBDLLHOOKSTRUCT,
            ): WinDef.LRESULT {
                if (nCode >= 0) {
                    val message = wParam.toInt()
                    if (message == WM_KEYDOWN || message == WM_SYSKEYDOWN) {
                        when (info.vkCode) {
                            VK_MEDIA_PLAY_PAUSE -> dispatch("PLAY_PAUSE", onPlayPause)
                            VK_MEDIA_NEXT_TRACK -> dispatch("NEXT", onNext)
                            VK_MEDIA_PREV_TRACK -> dispatch("PREVIOUS", onPrevious)
                        }
                    }
                }
                // Always pass the event on so other apps still receive media keys.
                return User32.INSTANCE.CallNextHookEx(
                    null,
                    nCode,
                    wParam,
                    WinDef.LPARAM(Pointer.nativeValue(info.pointer)),
                )
            }
        }
        keyboardProc = proc

        // hMod is null: for WH_KEYBOARD_LL the proc is identified by address within
        // this process, so no owning-module handle is required.
        val hook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, proc, null, 0)
        if (hook == null) {
            log.e { "SetWindowsHookEx(WH_KEYBOARD_LL) failed — media keys unavailable" }
            keyboardProc = null
            return
        }
        hHook = hook
        log.i { "global media-key hook installed" }

        // Servicing the hook requires pumping this thread's message queue.
        // GetMessage returns 0 on WM_QUIT and -1 on error; either ends the loop.
        val msg = WinUser.MSG()
        while (User32.INSTANCE.GetMessage(msg, null, 0, 0) > 0) {
            User32.INSTANCE.TranslateMessage(msg)
            User32.INSTANCE.DispatchMessage(msg)
        }
        log.d { "media-key message loop exited" }
    }

    private fun dispatch(name: String, action: () -> Unit) {
        log.d { "media key: $name" }
        EventQueue.invokeLater {
            try {
                action()
            } catch (t: Throwable) {
                log.e(t) { "media-key action failed: $name" }
            }
        }
    }

    private companion object {
        // Win32 window messages.
        const val WM_KEYDOWN = 0x0100
        const val WM_SYSKEYDOWN = 0x0104
        const val WM_QUIT = 0x0012

        // Media-transport virtual-key codes (winuser.h).
        const val VK_MEDIA_NEXT_TRACK = 0xB0
        const val VK_MEDIA_PREV_TRACK = 0xB1
        const val VK_MEDIA_PLAY_PAUSE = 0xB3
    }
}
