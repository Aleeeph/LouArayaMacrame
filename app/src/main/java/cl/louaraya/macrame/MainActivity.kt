package cl.louaraya.macrame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.louaraya.macrame.ui.navigation.AppNavHost
import cl.louaraya.macrame.ui.theme.MacrameTheme
import cl.louaraya.macrame.viewmodel.SessionViewModel
import cl.louaraya.macrame.viewmodel.appViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionVm: SessionViewModel = appViewModel { SessionViewModel(it.prefs) }
            val darkPref by sessionVm.darkMode.collectAsStateWithLifecycle()
            MacrameTheme(darkTheme = darkPref ?: isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(sessionVm)
                }
            }
        }
    }
}
