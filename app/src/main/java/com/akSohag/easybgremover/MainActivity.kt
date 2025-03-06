package com.akSohag.easybgremover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute
                ) {
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

    @Serializable
    data object HomeRoute

    @Serializable
    data class EditorRoute(val uriString: String)


}









