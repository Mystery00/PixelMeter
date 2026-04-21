package vip.mystery0.pixel.meter.ui.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch
import vip.mystery0.pixel.meter.data.repository.NetworkRepository
import vip.mystery0.pixel.meter.data.source.NetSpeedData
import vip.mystery0.pixel.meter.ui.theme.PixelPulseTheme
import kotlin.math.roundToInt

class OverlayWindow(
    private val context: Context,
    private val repository: NetworkRepository
) : LifecycleOwner, ViewModelStoreOwner,
    SavedStateRegistryOwner {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null
    private var params: WindowManager.LayoutParams? = null

    private var lifecycleRegistry = LifecycleRegistry(this)
    private var savedStateRegistryController = SavedStateRegistryController.create(this)
    private var store = ViewModelStore()

    private var speedState by mutableStateOf(NetSpeedData(0, 0))

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun show() {
        if (view != null) return

        lifecycleRegistry = LifecycleRegistry(this)
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(null)
        store = ViewModelStore()

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        lifecycleScope.launch {
            val (initialX, initialY) = repository.getOverlayPosition()

            val isShowOnStatusBarInitially = repository.isOverlayShowOnStatusBar.value
            val extraFlags =
                if (isShowOnStatusBarInitially) WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS else 0

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or extraFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = initialX
                y = initialY
                if (isShowOnStatusBarInitially && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            val composeView = ComposeView(context)
            composeView.setViewTreeLifecycleOwner(this@OverlayWindow)
            composeView.setViewTreeViewModelStoreOwner(this@OverlayWindow)
            composeView.setViewTreeSavedStateRegistryOwner(this@OverlayWindow)

            composeView.setContent {
                PixelPulseTheme {
                    val isLocked by repository.isOverlayLocked.collectAsState()
                    val isShowOnStatusBar by repository.isOverlayShowOnStatusBar.collectAsState()
                    val bgColor by repository.overlayBgColor.collectAsState()
                    val textColor by repository.overlayTextColor.collectAsState()
                    val cornerRadius by repository.overlayCornerRadius.collectAsState()
                    val textSize by repository.overlayTextSize.collectAsState()
                    val textUp by repository.overlayTextUp.collectAsState()
                    val textDown by repository.overlayTextDown.collectAsState()
                    val upFirst by repository.overlayOrderUpFirst.collectAsState()
                    val direction by repository.overlayDirection.collectAsState()
                    val alignment by repository.overlayAlignment.collectAsState()
                    val isOverlayUseDefaultColors by repository.isOverlayUseDefaultColors.collectAsState()
                    val speedUnit by repository.speedUnit.collectAsState()

                    LaunchedEffect(isShowOnStatusBar) {
                        params?.let { p ->
                            var changed = false
                            if (isShowOnStatusBar) {
                                if ((p.flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) == 0) {
                                    p.flags =
                                        p.flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                                    changed = true
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    if (p.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
                                        p.layoutInDisplayCutoutMode =
                                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                                        changed = true
                                    }
                                }
                            } else {
                                if ((p.flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0) {
                                    p.flags =
                                        p.flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS.inv()
                                    changed = true
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    if (p.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT) {
                                        p.layoutInDisplayCutoutMode =
                                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                                        changed = true
                                    }
                                }
                            }
                            if (changed) {
                                windowManager.updateViewLayout(composeView, p)
                            }
                        }
                    }

                    OverlayContent(
                        speed = speedState,
                        useDefaultColors = isOverlayUseDefaultColors,
                        bgColor = bgColor,
                        textColor = textColor,
                        cornerRadius = cornerRadius,
                        textSize = textSize,
                        textUp = textUp,
                        textDown = textDown,
                        upFirst = upFirst,
                        direction = direction,
                        alignment = alignment,
                        speedUnit = speedUnit,
                        onDrag = { x, y ->
                            if (!isLocked) {
                                params?.let { p ->
                                    p.x += x.roundToInt()
                                    p.y += y.roundToInt()
                                    windowManager.updateViewLayout(composeView, p)
                                }
                            }
                        },
                        onDragEnd = {
                            params?.let { p ->
                                repository.saveOverlayPosition(p.x, p.y)
                            }
                        }
                    )
                }
            }

            windowManager.addView(composeView, params)
            view = composeView
        }

    }

    fun update(speed: NetSpeedData) {
        speedState = speed
    }

    fun hide() {
        if (view != null) {
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                Log.w("OverlayWindow", "removeView failed", e)
            }
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
            view = null
        }
    }
}

@Composable
fun OverlayContent(
    speed: NetSpeedData,
    useDefaultColors: Boolean,
    bgColor: Int,
    textColor: Int,
    cornerRadius: Int,
    textSize: Float,
    textUp: String,
    textDown: String,
    upFirst: Boolean,
    direction: Int = 0,
    alignment: Int = 0,
    speedUnit: Int = 0,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(cornerRadius.dp),
        color = if (useDefaultColors) MaterialTheme.colorScheme.surface else Color(bgColor),
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                },
                onDragEnd = onDragEnd
            )
        }
    ) {
        val upText = "$textUp${NetworkRepository.formatSpeedLine(speed.uploadSpeed, speedUnit)}"
        val downText =
            "$textDown${NetworkRepository.formatSpeedLine(speed.downloadSpeed, speedUnit)}"

        val text1 = if (upFirst) upText else downText
        val text2 = if (upFirst) downText else upText

        if (direction == 0) {
            // Horizontal
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text1,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize.sp),
                    color = if (useDefaultColors) MaterialTheme.colorScheme.onSurface else Color(
                        textColor
                    )
                )
                Box(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = text2,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize.sp),
                    color = if (useDefaultColors) MaterialTheme.colorScheme.onSurface else Color(
                        textColor
                    )
                )
            }
        } else {
            // Vertical
            val horizontalAlignment = when (alignment) {
                1 -> Alignment.CenterHorizontally
                2 -> Alignment.End
                else -> Alignment.Start
            }
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = horizontalAlignment
            ) {
                Text(
                    text = text1,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize.sp),
                    color = if (useDefaultColors) MaterialTheme.colorScheme.onSurface else Color(
                        textColor
                    )
                )
                Text(
                    text = text2,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize.sp),
                    color = if (useDefaultColors) MaterialTheme.colorScheme.onSurface else Color(
                        textColor
                    )
                )
            }
        }
    }
}
