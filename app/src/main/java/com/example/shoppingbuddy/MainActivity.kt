package com.example.shoppingbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.shoppingbuddy.ui.theme.ShoppingBuddyTheme
import com.example.shoppingbuddy.ui.theme.ShoppingList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShoppingBuddyTheme {
        Navigation()
    }
}

@Composable
fun Navigation(modifier: Modifier = Modifier)
{
    val navController = rememberNavController()
    val viewModel: LocationViewModel = viewModel()
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    NavHost(navController = navController, startDestination = "shoppinglistscreen")
    {
        composable("shoppinglistscreen")
        {
            ShoppingList(locationUtils = locationUtils, viewModel = viewModel, navController = navController, context = context, address = viewModel.address.value.firstOrNull()?.formatted_address ?: "No Address Found")
        }


        dialog("locationscreen") {backstack->
            viewModel.location.value?.let {it1 ->
                LocationSelectionScreen(location = it1, onLocationSelected = {locationdata->
                    viewModel.fetchAddress("${locationdata.latitude},${locationdata.longitude}")
                    navController.popBackStack()
                })
            }
        }
    }

}