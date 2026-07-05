package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.data.User
import com.example.data.WalletRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    repository: WalletRepository,
    initialRole: String = "Personal",
    onSignupSuccess: (Int) -> Unit,
    onBack: () -> Unit
) {
    var accountType by remember { mutableStateOf(initialRole) }
    var currency by remember { mutableStateOf("BDT") }
    var accountTypeExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var bkash by remember { mutableStateOf("") }
    var nagad by remember { mutableStateOf("") }
    var rocket by remember { mutableStateOf("") }
    var withdrawPin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val allConfigs by repository.getAllConfigsFlow().collectAsState(initial = emptyList())
    val rawLinksStr = allConfigs.find { it.key == "telegram_support_links" }?.value ?: ""
    val legacyLink = allConfigs.find { it.key == "telegram_support_link" }?.value ?: ""
    
    val activeSupportLinks = remember(rawLinksStr, legacyLink) {
        val parsed = if (rawLinksStr.isNotBlank()) {
            com.example.data.TelegramSupportConfig.parseList(rawLinksStr)
        } else if (legacyLink.isNotBlank()) {
            listOf(com.example.data.TelegramSupportConfig(legacyLink, true))
        } else {
            emptyList()
        }
        parsed.filter { it.isActive }
    }
    
    var showSupportSelectorDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sign Up") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = accountType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account Type") },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = currency,
                onValueChange = {},
                readOnly = true,
                label = { Text("Currency") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = bkash, onValueChange = { bkash = it }, label = { Text("bKash Number (Optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = nagad, onValueChange = { nagad = it }, label = { Text("Nagad Number (Optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = rocket, onValueChange = { rocket = it }, label = { Text("Rocket Number (Optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = withdrawPin, onValueChange = { withdrawPin = it }, label = { Text("Secret PIN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = referralCode, onValueChange = { referralCode = it }, label = { Text("Referral Code (Optional)") }, modifier = Modifier.fillMaxWidth())
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank() || name.isBlank() || withdrawPin.isBlank() || email.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                        return@Button
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Invalid email format"
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    
                    scope.launch {
                        val existing = repository.getUserByUsername(username)
                        if (existing != null) {
                            errorMessage = "Username already exists"
                        } else {
                            var referrer: User? = null
                            if (referralCode.isNotBlank()) {
                                referrer = repository.getUserByReferralCode(referralCode)
                                if (referrer == null) {
                                    errorMessage = "Invalid Referral Code"
                                    return@launch
                                }
                            }

                            val admin = repository.getUserByUsername("admin")
                            val defaultBonus = admin?.bonusPercent ?: 1.0
                            val myReferCode = "REF-${System.currentTimeMillis().toString().takeLast(6)}${(10..99).random()}"
                            
                            var initialBalance = 0.0
                            if (referrer != null) {
                                initialBalance = 50.0 // Bonus for signup with referral
                                
                                val newReferrerMain = referrer.mainBalance + 50.0
                                val newReferrer = referrer.copy(mainBalance = newReferrerMain)
                                repository.updateUser(newReferrer)
                                
                                repository.insertTransaction(
                                    com.example.data.Transaction(
                                        userId = referrer.id,
                                        type = "Referral Bonus",
                                        amount = 50.0,
                                        trxId = "REF-${System.currentTimeMillis()}",
                                        previousBalance = referrer.mainBalance,
                                        newBalance = newReferrerMain
                                    )
                                )
                            }

                            val generatedEposCode = if (accountType == "Cashier") "${(1000000..9999999).random()}" else ""
                            
                            val globalCommPercent = allConfigs.find { it.key == "global_commission_percent" }?.value?.toDoubleOrNull() ?: 2.0
                            val globalDpCommPercent = allConfigs.find { it.key == "global_dp_commission_percent" }?.value?.toDoubleOrNull() ?: 2.0
                            val globalWdCommPercent = allConfigs.find { it.key == "global_wd_commission_percent" }?.value?.toDoubleOrNull() ?: 2.0
                            
                            val user = User(
                                username = username,
                                passwordHash = password, // Simple demo
                                accountType = accountType,
                                currency = currency,
                                name = name,
                                email = email,
                                number = number,
                                bkashNumber = bkash,
                                nagadNumber = nagad,
                                rocketNumber = rocket,
                                withdrawPin = withdrawPin,
                                referralCode = myReferCode,
                                referredBy = referralCode,
                                bonusPercent = defaultBonus,
                                agentCommissionPercent = globalCommPercent,
                                agentDpCommissionPercent = globalDpCommPercent,
                                agentWdCommissionPercent = globalWdCommPercent,
                                mainBalance = initialBalance,
                                eposCode = generatedEposCode
                            )
                            val newId = repository.insertUser(user)
                            
                            if (initialBalance > 0.0) {
                                repository.insertTransaction(
                                    com.example.data.Transaction(
                                        userId = newId.toInt(),
                                        type = "Signup Bonus",
                                        amount = initialBalance,
                                        trxId = "SIGNUP-${System.currentTimeMillis()}",
                                        previousBalance = 0.0,
                                        newBalance = initialBalance
                                    )
                                )
                            }
                            
                            onSignupSuccess(newId.toInt())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("signup_submit_button")
            ) {
                Text("Create Account")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack) {
                Text("Already have an account? Login")
            }

            Spacer(modifier = Modifier.height(24.dp))
            val context = androidx.compose.ui.platform.LocalContext.current
            
            if (showSupportSelectorDialog) {
                AlertDialog(
                    onDismissRequest = { showSupportSelectorDialog = false },
                    title = { Text("Select Live Support Desk") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activeSupportLinks.forEach { config ->
                                val label = if (config.link.startsWith("@")) config.link else config.link
                                Button(
                                    onClick = {
                                        val cleaned = config.link.replace("@", "").trim()
                                        val url = if (config.link.startsWith("http")) config.link else "https://t.me/$cleaned"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            errorMessage = "Could not open Telegram"
                                        }
                                        showSupportSelectorDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A68E3))
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSupportSelectorDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    if (activeSupportLinks.size > 1) {
                        showSupportSelectorDialog = true
                    } else {
                        val targetLink = activeSupportLinks.firstOrNull()?.link ?: "personal_number_admin"
                        val cleaned = targetLink.replace("@", "").trim()
                        val url = if (targetLink.startsWith("http")) targetLink else "https://t.me/$cleaned"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            errorMessage = "Could not open Telegram"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0088cc))
            ) {
                Icon(Icons.Filled.Chat, contentDescription = "Live Chat", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Live Chat on Telegram")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
