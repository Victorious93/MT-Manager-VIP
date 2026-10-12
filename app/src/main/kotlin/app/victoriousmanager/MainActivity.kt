package app.victoriousmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.victoriousmanager.ui.VictoriousManagerTheme
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VictoriousManagerTheme {
                Text("Victorious-Manager")
            }
        }
    }
}
