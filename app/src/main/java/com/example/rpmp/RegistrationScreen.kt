package com.example.rpmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun RegistrationScreen(onSignUp: (String, String) -> Unit) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var usernameErrorState by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var passwordErrorState by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append("R")
                }
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("eg")
                }
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append("is")
                }
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("ter")
                }
            },
            fontSize = 30.sp
        )

        Spacer(Modifier.size(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Enter username*") }
        )
        if (usernameErrorState) {
            Text(text = "Required", color = Color.Red)
        }

        Spacer(Modifier.size(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Enter Password*") },
            visualTransformation = PasswordVisualTransformation()
        )
        if (passwordErrorState) {
            Text(text = "Required", color = Color.Red)
        }

        Spacer(Modifier.size(16.dp))

        // Login button
        Button(
            onClick = {
                when {
                    username.text.isEmpty() -> {
                        usernameErrorState = true
                    }
                    password.text.isEmpty() -> {
                        passwordErrorState = true
                    }
                    else -> {
                        onSignUp(username.text, password.text)
                    }
                }
            },
            content = { Text(text = "Sign up", color = Color.White) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.size(16.dp))
    }
}