package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.WalletRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    repository: WalletRepository,
    onLoginSuccess: (Int, String) -> Unit,
    onNavigateSignup: (String) -> Unit,
    onNavigateForgetPassword: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = User, 1 = Cashier, 2 = Agent
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var eposCode by remember { mutableStateOf("") }
    var rememberEpos by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
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

    val bgColor = Color(0xFFF5F6FA)
    val primaryPurple = Color(0xFF7A68E3)
    val lightPurpleBtn = Color(0xFFC7C1EF)
    val textDark = Color(0xFF222222)
    val textGray = Color(0xFF8B92A5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "build : ${com.example.BuildConfig.BUILD_TIME}",
            fontSize = 12.sp,
            color = textGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Logo and Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B2D31)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "mobcash",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = textDark
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Login",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = textDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Log in using your account details",
            fontSize = 16.sp,
            color = textGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = { HorizontalDivider(color = Color(0xFFDCDFEA)) },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = primaryPurple,
                    height = 2.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "User",
                        color = if (selectedTab == 0) primaryPurple else textGray,
                        fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Cashier",
                        color = if (selectedTab == 1) primaryPurple else textGray,
                        fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        "Agent",
                        color = if (selectedTab == 2) primaryPurple else textGray,
                        fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inputs
        TextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Username", color = Color(0xFFAAAEC1)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag("username_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = primaryPurple
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFFAAAEC1)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Color(0xFFAAAEC1))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag("password_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = primaryPurple
            ),
            singleLine = true
        )

        if (selectedTab == 1) { // Cashier
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = eposCode,
                onValueChange = { if (it.length <= 7 && it.all { char -> char.isDigit() }) eposCode = it },
                placeholder = { Text("7-digit EPOS code", color = Color(0xFFAAAEC1)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = primaryPurple
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
            ) {
                Checkbox(
                    checked = rememberEpos,
                    onCheckedChange = { rememberEpos = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = primaryPurple,
                        uncheckedColor = textGray
                    )
                )
                Text("Remember EPOS code", color = textDark, fontSize = 14.sp)
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    var user: com.example.data.User? = null
                    var isValid = false
                    val expectedAccountType = when (selectedTab) {
                        0 -> "Personal"
                        1 -> "Cashier"
                        else -> "Agent"
                    }

                    user = repository.getUserByUsername(username)
                    if (user != null && user.passwordHash == password && (user.accountType == expectedAccountType || user.role == "Admin")) {
                        isValid = true
                        
                        if (selectedTab == 1 && user.role != "Admin") { // Cashier specific check
                            if (user.eposCode != eposCode) {
                                isValid = false
                            }
                        }
                    }

                    if (isValid) {
                        onLoginSuccess(user!!.id, user.role)
                    } else {
                        errorMessage = "Invalid credentials"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = lightPurpleBtn,
                contentColor = Color.White
            )
        ) {
            Text("Log in", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        if (selectedTab == 2) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { onNavigateSignup("Agent") }) {
                Text("Become an agent", color = primaryPurple, fontSize = 16.sp)
            }
        } else if (selectedTab == 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateForgetPassword) {
                    Text("Forgot Password?", color = primaryPurple, fontSize = 14.sp)
                }
                TextButton(onClick = { onNavigateSignup("Cashier") }) {
                    Text("Sign up", color = primaryPurple, fontSize = 14.sp)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateForgetPassword) {
                    Text("Forgot Password?", color = primaryPurple, fontSize = 14.sp)
                }
                TextButton(onClick = { onNavigateSignup("Personal") }) {
                    Text("Sign up", color = primaryPurple, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        
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
                                colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0088cc))
        ) {
            Icon(Icons.Default.Chat, contentDescription = "Live Chat", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Live Chat on Telegram", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
