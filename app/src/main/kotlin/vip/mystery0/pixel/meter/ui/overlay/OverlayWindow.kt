package vip.mystery0.pixel.meter.ui.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = initialX
                y = initialY
            }

            val composeView = ComposeView(context)
            composeView.setViewTreeLifecycleOwner(this@OverlayWindow)
            composeView.setViewTreeViewModelStoreOwner(this@OverlayWindow)
            composeView.setViewTreeSavedStateRegistryOwner(this@OverlayWindow)

            composeView.setContent {
                PixelPulseTheme {
                    val isLocked by repository.isOverlayLocked.collectAsState()
                    OverlayContent(
                        speed = speedState,
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
            windowManager.removeView(view)
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
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
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
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "â–?${NetworkRepository.formatSpeedLine(speed.downloadSpeed)}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = "â–?${NetworkRepository.formatSpeedLine(speed.uploadSpeed)}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
