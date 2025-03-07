package com.akSohag.easybgremover

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.akSohag.easybgremover.screens.EditorRoute
import com.akSohag.easybgremover.screens.EditorScreen
import com.akSohag.easybgremover.screens.HomeRoute
import com.akSohag.easybgremover.screens.HomeScreen
import com.akSohag.easybgremover.screens.SplashRoute
import com.akSohag.easybgremover.screens.SplashScreen
import com.akSohag.easybgremover.ui.theme.AppTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        var sharedImageUri: Uri? = null

        // Handle received image from another app
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            sharedImageUri = handleSendImage(intent)
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = if (sharedImageUri != null) EditorRoute(sharedImageUri.toString()) else SplashRoute
                ) {
                    composable<SplashRoute> {
                        SplashScreen {
                            navController.navigate(HomeRoute) {
                                popUpTo(SplashRoute) { inclusive = true }
                            }
                        }
                    }

                    composable<HomeRoute> {
                        HomeScreen { selectedUri ->
                            navController.navigate(EditorRoute(uriString = selectedUri.toString()))
                        }
                    }

                    composable<EditorRoute> { backStackEntry ->
                        val editorRoute: EditorRoute = backStackEntry.toRoute()
                        EditorScreen(editorRoute.uriString) { // back button pressed

                            if (sharedImageUri != null){
                                this@MainActivity.finish()
                            }else navController.navigateUp()

                        }
                    }
                }
            }
        }
    }

    // Function to handle the received image
    private fun handleSendImage(intent: Intent): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        }
    }


}









