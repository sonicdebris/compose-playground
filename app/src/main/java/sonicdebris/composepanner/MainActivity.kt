package sonicdebris.composepanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import sonicdebris.composepanner.ui.ComposePannerTheme
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {

    private val panner = Panner(lifecycleScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposePannerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    PannerView(panner)
                }
            }
        }
    }
}