package com.example.shoppingbuddy.ui.theme

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.shoppingbuddy.LocationUtils
import com.example.shoppingbuddy.LocationViewModel
import com.example.shoppingbuddy.MainActivity

data class ShoppingItem(
    val id: Int,
    var name: String,
    var quantity: Int,
    var isEditing: Boolean = false,
    var address: String = ""
)

@Composable
fun ShoppingList(locationUtils: LocationUtils, viewModel: LocationViewModel, navController: NavController, context: Context, address: String) {
    var sItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var nextId by remember { mutableStateOf(1) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = {
            permissions ->
        if(permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        {
            locationUtils.requestLocationUpdates(viewModel)
        }
        else
        {
            val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity,Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity,Manifest.permission.ACCESS_COARSE_LOCATION)
            if(rationaleRequired)
            {
                Toast.makeText(context,"Location Permission is required for this feature to work",Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(context,"Go to settings and enable location permission",Toast.LENGTH_SHORT).show()
            }
        }
    })
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { showDialog = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Add Item")
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            items(sItems) { item ->
                if (item.isEditing) {
                    ShoppingItemEditor(item) { editedName, editedQuantity ->
                        sItems = sItems.map {
                            if (it.id == item.id) it.copy(name = editedName, quantity = editedQuantity, address = address ,isEditing = false)
                            else it.copy(isEditing = false)
                        }
                    }
                } else {
                    ShoppingListItem(
                        item,
                        onEditClicked = {
                            sItems = sItems.map {
                                it.copy(isEditing = it.id == item.id)
                            }
                        },
                        onDeleteClicked = {
                            sItems = sItems.filterNot { it.id == item.id }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        val quantity = itemQuantity.toIntOrNull() ?: 1
                        if (itemName.isNotBlank()) {
                            val newItem = ShoppingItem(id = nextId++, name = itemName, quantity = quantity,address=address)
                            sItems = sItems + newItem
                            showDialog = false
                            itemName = ""
                            itemQuantity = ""
                        }
                    }) {
                        Text("Add")
                    }
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            title = { Text("Add Shopping Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        label = { Text("Item Quantity") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                    Button(onClick = {
                        if(locationUtils.hasLocationPermission(context)) {
                            locationUtils.requestLocationUpdates(viewModel)
                            navController.navigate("locationSelectionScreen")
                            {
                               this.launchSingleTop
                            }
                        }
                        else
                        {
                            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    })
                    {
                        Text("Address")
                    }
                }
            }
        )
    }
}

@Composable
fun ShoppingItemEditor(item: ShoppingItem, onEditClicked: (String, Int) -> Unit) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.quantity.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF06B19D))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            )
            OutlinedTextField(
                value = editedQuantity,
                onValueChange = { editedQuantity = it },
                label = { Text("Quantity") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            )
        }
        Button(
            onClick = {
                val quantity = editedQuantity.toIntOrNull() ?: 1
                onEditClicked(editedName, quantity)
            },
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp)
        ) {
            Text("Save")
        }
    }
}

@Composable
fun ShoppingListItem(item: ShoppingItem, onEditClicked: () -> Unit, onDeleteClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color(0xFF07EEED)), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(8.dp)) {
            Row {
                Text(text = item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp))
                Text(text = "Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp))
            }
            Row(modifier = Modifier.fillMaxWidth())
            {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Text(text = item.address)
            }
        }
        Row(modifier = Modifier.padding(8.dp))
        {
            IconButton(onClick = onEditClicked) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDeleteClicked) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}