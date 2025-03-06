package com.akSohag.easybgremover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = SplashRoute
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
                            navController.navigate(
                                EditorRoute(
                                    uriString = selectedUri.toString()
                                )
                            )
                        }
                    }
                    composable<EditorRoute> { backStackEntry ->
                        val editorRoute: EditorRoute = backStackEntry.toRoute()
                        EditorScreen(editorRoute.uriString) {
                            navController.navigateUp()
                        }
                    }
                }
            }

        }
    }


}









