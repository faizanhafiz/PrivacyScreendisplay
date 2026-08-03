package com.app.privacyscreendisplay.core.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.app.privacyscreendisplay.core.ui.components.FullProtectionOverlay
import com.app.privacyscreendisplay.home.domain.model.OverlayStyle

/**
 * System-wide WindowManager overlay controller.
 * Displays a TYPE_APPLICATION_OVERLAY window with Jetpack Compose FullProtectionOverlay
 * covering the entire device screen when shoulder surfing is detected over a protected app.
 */
class SystemOverlayManager(
    private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: FrameLayout? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    @Volatile private var isOverlayShowing = false

    fun showOverlay(
        overlayStyle: OverlayStyle = OverlayStyle.BLUR,
        onDismiss: () -> Unit
    ) {
        if (isOverlayShowing) return

        mainHandler.post {
            if (isOverlayShowing || overlayView != null) return@post

            try {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    },
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                        setBlurBehindRadius(80)
                    }
                }

                val composeView = ComposeView(context).apply {
                    setContent {
                        FullProtectionOverlay(
                            isVisible = true,
                            overlayStyle = overlayStyle,
                            onDismiss = {
                                hideOverlay()
                                onDismiss()
                            }
                        )
                    }
                }

                // Set required composition owners for ComposeView in WindowManager
                val lifecycleOwner = OverlayLifecycleOwner()
                lifecycleOwner.performRestore(null)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                overlayLifecycleOwner = lifecycleOwner

                composeView.setViewTreeLifecycleOwner(lifecycleOwner)
                composeView.setViewTreeViewModelStoreOwner(lifecycleOwner)
                composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                val containerView = object : FrameLayout(context) {
                    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                            hideOverlay()
                            onDismiss()
                            return true
                        }
                        return super.dispatchKeyEvent(event)
                    }
                }.apply {
                    setViewTreeLifecycleOwner(lifecycleOwner)
                    setViewTreeViewModelStoreOwner(lifecycleOwner)
                    setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                    addView(
                        composeView,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    )
                }

                windowManager.addView(containerView, params)
                overlayView = containerView
                isOverlayShowing = true
            } catch (e: Exception) {
                e.printStackTrace()
                isOverlayShowing = false
            }
        }
    }

    fun hideOverlay() {
        if (!isOverlayShowing && overlayView == null) return

        mainHandler.post {
            val viewToRemove = overlayView ?: return@post
            try {
                overlayLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                overlayLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                overlayLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                windowManager.removeView(viewToRemove)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                overlayView = null
                overlayLifecycleOwner = null
                isOverlayShowing = false
            }
        }
    }

    fun isShowing(): Boolean = isOverlayShowing
}

/**
 * Lightweight SavedStateRegistryOwner & ViewModelStoreOwner implementation required
 * for rendering Jetpack Compose views inside system WindowManager windows.
 */
private class OverlayLifecycleOwner :
    androidx.lifecycle.LifecycleOwner,
    androidx.lifecycle.ViewModelStoreOwner,
    androidx.savedstate.SavedStateRegistryOwner {

    private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
    private val savedStateRegistryController = androidx.savedstate.SavedStateRegistryController.create(this)
    private val viewModelStoreObj = androidx.lifecycle.ViewModelStore()

    override val lifecycle: androidx.lifecycle.Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: androidx.lifecycle.ViewModelStore
        get() = viewModelStoreObj

    override val savedStateRegistry: androidx.savedstate.SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun performRestore(savedState: android.os.Bundle?) {
        savedStateRegistryController.performRestore(savedState)
    }

    fun handleLifecycleEvent(event: androidx.lifecycle.Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}
