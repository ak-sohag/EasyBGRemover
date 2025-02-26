package com.AKSohag.easybgremover

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
import com.AKSohag.easybgremover.Screens.EditorScreen
import com.AKSohag.easybgremover.Screens.HomeScreen

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "picker") {
                composable("picker") {
                    HomeScreen { selectedUri ->
                        // Pass the selected URI as a string (encoded to ensure safe navigation)
                        navController.navigate("editor/${Uri.encode(selectedUri.toString())}")
                    }
                }
                composable(
                    route = "editor/{imageUri}",
                    arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    // Retrieve the URI string and decode it
                    val imageUriString = backStackEntry.arguments?.getString("imageUri") ?: ""
                    EditorScreen(uriString = imageUriString){
                        navController.popBackStack()
                    }
                }
            }
        }
    }


}









