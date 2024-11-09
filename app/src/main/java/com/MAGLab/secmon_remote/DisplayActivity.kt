package com.MAGLab.secmon_remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

public class DisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NumberInputApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberInputApp() {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var inputCount by remember { mutableStateOf("") }
    var textFieldsCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dynamic Text Fields") },
                navigationIcon = {
                    IconButton(onClick = { onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = inputCount,
                onValueChange = { newText ->
                    inputCount = newText.filter { it.isDigit() } // Allow only numeric input
                },
                label = { Text("Requested Text Fields:") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                textFieldsCount = inputCount.toIntOrNull() ?: 0 // Set text field count
            }) {
                Text("Generate Text Fields")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the requested number of text fields
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                repeat(textFieldsCount) { index ->
                    var text by remember { mutableStateOf("") }
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        label =  { Text(index.toString()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White), // Set a background if desired
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Add space between text fields
                }
                if (textFieldsCount != 0){
                Button(onClick = {
                    // TODO
                }) {
                    Text ("Send URLs")
                }}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNumberInputApp() {
    NumberInputApp()
}
