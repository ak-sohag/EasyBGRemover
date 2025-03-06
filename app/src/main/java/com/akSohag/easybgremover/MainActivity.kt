package com.akSohag.easybgremover

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.akSohag.easybgremover.screens.EditorScreen
import com.akSohag.easybgremover.screens.HomeScreen
import com.akSohag.easybgremover.ui.theme.AppTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                HomeScreen {

                }
            }

        }
    }



}









