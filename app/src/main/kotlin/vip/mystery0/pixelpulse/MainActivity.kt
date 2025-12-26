package vip.mystery0.pixelpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import vip.mystery0.pixelpulse.ui.home.HomeScreen
import vip.mystery0.pixelpulse.ui.theme.PixelPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Shizuku provider listener registration is handled in Repository? 
        // Or we should add listener here? Repository init block handles it.

        setContent {
            PixelPulseTheme {
                HomeScreen()
            }
        }
    }
}
