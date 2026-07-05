package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WalletViewModel
import com.example.data.BettingSite
import com.example.data.PaymentMethodConfig
import com.example.data.TelegramSupportConfig
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfigScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val bettingSites by viewModel.bettingSites.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    val allConfigs by viewModel.allConfigs.collectAsState()
    
    // User Configs
    val userDeposit = allConfigs.find { it.key == "user_deposit_enabled" }?.value ?: "true"
    val userWithdraw = allConfigs.find { it.key == "user_withdraw_enabled" }?.value ?: "true"
    val userAdd = allConfigs.find { it.key == "user_add_money_enabled" }?.value ?: "true"
    val userOut = allConfigs.find { it.key == "user_out_money_enabled" }?.value ?: "true"
    val userSend = allConfigs.find { it.key == "user_send_money_enabled" }?.value ?: "true"
    val userReceive = allConfigs.find { it.key == "user_receive_money_enabled" }?.value ?: "true"
    
    // Agent Configs
    val agentDeposit = allConfigs.find { it.key == "agent_deposit_enabled" }?.value ?: "true"
    val agentWithdraw = allConfigs.find { it.key == "agent_withdraw_enabled" }?.value ?: "true"
    val agentAdd = allConfigs.find { it.key == "agent_add_money_enabled" }?.value ?: "true"
    val agentOut = allConfigs.find { it.key == "agent_out_money_enabled" }?.value ?: "true"
    val agentSend = allConfigs.find { it.key == "agent_send_money_enabled" }?.value ?: "true"
    val agentReceive = allConfigs.find { it.key == "agent_receive_money_enabled" }?.value ?: "true"

    // Cashier Configs
    val cashierDeposit = allConfigs.find { it.key == "cashier_deposit_enabled" }?.value ?: "true"
    val cashierWithdraw = allConfigs.find { it.key == "cashier_withdraw_enabled" }?.value ?: "true"
    val cashierAdd = allConfigs.find { it.key == "cashier_add_money_enabled" }?.value ?: "true"
    val cashierOut = allConfigs.find { it.key == "cashier_out_money_enabled" }?.value ?: "true"
    val cashierSend = allConfigs.find { it.key == "cashier_send_money_enabled" }?.value ?: "true"
    val cashierReceive = allConfigs.find { it.key == "cashier_receive_money_enabled" }?.value ?: "true"

    var newSiteName by remember { mutableStateOf("") }
    var newSiteWithdrawAddr by remember { mutableStateOf("") }
    var newSiteUsername by remember { mutableStateOf("") }
    var newSitePassword by remember { mutableStateOf("") }
    var newSiteWorkId by remember { mutableStateOf("") }

    var newMethodName by remember { mutableStateOf("") }
    var newMethodNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Global Action Features", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            
            var selectedRoleTab by remember { mutableStateOf("User") }
            
            ScrollableTabRow(
                selectedTabIndex = listOf("User", "Agent", "Cashier").indexOf(selectedRoleTab),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 0.dp
            ) {
                listOf("User", "Agent", "Cashier").forEachIndexed { _, title ->
                    Tab(
                        selected = selectedRoleTab == title,
                        onClick = { selectedRoleTab = title },
                        text = { Text(title) }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val prefix = selectedRoleTab.lowercase()
                    
                    val depositVal = if (selectedRoleTab == "User") userDeposit else if (selectedRoleTab == "Agent") agentDeposit else cashierDeposit
                    val withdrawVal = if (selectedRoleTab == "User") userWithdraw else if (selectedRoleTab == "Agent") agentWithdraw else cashierWithdraw
                    val addVal = if (selectedRoleTab == "User") userAdd else if (selectedRoleTab == "Agent") agentAdd else cashierAdd
                    val outVal = if (selectedRoleTab == "User") userOut else if (selectedRoleTab == "Agent") agentOut else cashierOut
                    val sendVal = if (selectedRoleTab == "User") userSend else if (selectedRoleTab == "Agent") agentSend else cashierSend
                    val receiveVal = if (selectedRoleTab == "User") userReceive else if (selectedRoleTab == "Agent") agentReceive else cashierReceive

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Deposit")
                        Switch(checked = depositVal == "true", onCheckedChange = { viewModel.updateConfig("${prefix}_deposit_enabled", it.toString()) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Withdraw")
                        Switch(checked = withdrawVal == "true", onCheckedChange = { viewModel.updateConfig("${prefix}_withdraw_enabled", it.toString()) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Add Money")
                        Switch(checked = addVal == "true", onCheckedChange = { viewModel.updateConfig("${prefix}_add_money_enabled", it.toString()) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Out Money")
                        Switch(checked = outVal == "true", onCheckedChange = { viewModel.updateConfig("${prefix}_out_money_enabled", it.toString()) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Send Money")
                        Switch(checked = sendVal == "true", onCheckedChange = { viewModel.updateConfig("${prefix}_send_money_enabled", it.toString()) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Receive Money")
                        Switch(checked = receiveVal == "true", onCheckedChange = { viewModel.updateConfig("${prefix}_receive_money_enabled", it.toString()) })
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Support Configuration", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val rawLinksStr = allConfigs.find { it.key == "telegram_support_links" }?.value ?: ""
                    val legacyLink = allConfigs.find { it.key == "telegram_support_link" }?.value ?: ""
                    
                    val supportLinks = remember(rawLinksStr, legacyLink) {
                        if (rawLinksStr.isNotBlank()) {
                            TelegramSupportConfig.parseList(rawLinksStr)
                        } else if (legacyLink.isNotBlank()) {
                            listOf(TelegramSupportConfig(legacyLink, true))
                        } else {
                            emptyList()
                        }
                    }

                    fun updateAndSave(updatedList: List<TelegramSupportConfig>) {
                        val serialized = TelegramSupportConfig.serializeList(updatedList)
                        viewModel.updateConfig("telegram_support_links", serialized)
                        if (updatedList.isNotEmpty()) {
                            viewModel.updateConfig("telegram_support_link", updatedList.firstOrNull { it.isActive }?.link ?: updatedList.first().link)
                        } else {
                            viewModel.updateConfig("telegram_support_link", "")
                        }
                    }

                    var newSupportLink by remember { mutableStateOf("") }
                    var editingLinkIndex by remember { mutableStateOf<Int?>(null) }
                    var editingLinkValue by remember { mutableStateOf("") }

                    Text("Add Telegram Support Handle / Link", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSupportLink,
                            onValueChange = { newSupportLink = it },
                            placeholder = { Text("@username or https://t.me/...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newSupportLink.isNotBlank()) {
                                    val cleaned = newSupportLink.trim()
                                    val newList = supportLinks + TelegramSupportConfig(cleaned, true)
                                    updateAndSave(newList)
                                    newSupportLink = ""
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Configured Support Handles (${supportLinks.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (supportLinks.isEmpty()) {
                        Text("No active support accounts configured. Falling back to default: @personal_number_admin", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            supportLinks.forEachIndexed { index, config ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (editingLinkIndex == index) {
                                            OutlinedTextField(
                                                value = editingLinkValue,
                                                onValueChange = { editingLinkValue = it },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(onClick = {
                                                if (editingLinkValue.isNotBlank()) {
                                                    val newList = supportLinks.toMutableList()
                                                    newList[index] = config.copy(link = editingLinkValue.trim())
                                                    updateAndSave(newList)
                                                    editingLinkIndex = null
                                                }
                                            }) {
                                                Icon(Icons.Filled.Check, contentDescription = "Save", tint = Color.Green)
                                            }
                                        } else {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(config.link, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    text = if (config.isActive) "Active" else "Inactive",
                                                    color = if (config.isActive) Color(0xFF4CAF50) else Color.Gray,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            
                                            Switch(
                                                checked = config.isActive,
                                                onCheckedChange = { isChecked ->
                                                    val newList = supportLinks.toMutableList()
                                                    newList[index] = config.copy(isActive = isChecked)
                                                    updateAndSave(newList)
                                                }
                                            )
                                            
                                            IconButton(onClick = {
                                                editingLinkIndex = index
                                                editingLinkValue = config.link
                                            }) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                            }

                                            IconButton(onClick = {
                                                val newList = supportLinks.toMutableList()
                                                newList.removeAt(index)
                                                updateAndSave(newList)
                                            }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text("Betting Sites", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            bettingSites.forEach { site ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(site.siteName, fontWeight = FontWeight.Bold)
                            Text("Username: ${site.username}", style = MaterialTheme.typography.bodySmall)
                            Text("Work ID: ${site.workId}", style = MaterialTheme.typography.bodySmall)
                            Text("Withdraw Addr: ${site.withdrawAddress}")
                            Text("Balance: ${site.balance}")
                        }
                        IconButton(onClick = { viewModel.deleteBettingSite(site) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            OutlinedTextField(value = newSiteName, onValueChange = { newSiteName = it }, label = { Text("New Site Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = newSiteUsername, onValueChange = { newSiteUsername = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = newSitePassword, onValueChange = { newSitePassword = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = newSiteWorkId, onValueChange = { newSiteWorkId = it }, label = { Text("Work ID") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = newSiteWithdrawAddr, onValueChange = { newSiteWithdrawAddr = it }, label = { Text("Withdraw Address") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                if (newSiteName.isNotBlank() && newSiteWithdrawAddr.isNotBlank() && newSiteUsername.isNotBlank() && newSitePassword.isNotBlank() && newSiteWorkId.isNotBlank()) {
                    viewModel.saveBettingSite(BettingSite(siteName = newSiteName, username = newSiteUsername, passwordHash = newSitePassword, workId = newSiteWorkId, withdrawAddress = newSiteWithdrawAddr))
                    newSiteName = ""
                    newSiteWithdrawAddr = ""
                    newSiteUsername = ""
                    newSitePassword = ""
                    newSiteWorkId = ""
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Betting Site")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Payment Methods", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            paymentMethods.forEach { method ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(method.name, fontWeight = FontWeight.Bold)
                            Text("Number: ${method.number}")
                        }
                        IconButton(onClick = { viewModel.deletePaymentMethod(method) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            OutlinedTextField(value = newMethodName, onValueChange = { newMethodName = it }, label = { Text("Method Name (e.g. bKash)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = newMethodNumber, onValueChange = { newMethodNumber = it }, label = { Text("Admin Number") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                if (newMethodName.isNotBlank() && newMethodNumber.isNotBlank()) {
                    viewModel.savePaymentMethod(PaymentMethodConfig(name = newMethodName, number = newMethodNumber, subMethod = "All"))
                    newMethodName = ""
                    newMethodNumber = ""
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Payment Method")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("OKExPay Gateway Settings", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            
            var okexpayBaseUrl by remember { mutableStateOf("https://api.wpay.life/") }
            var okexpayMchId by remember { mutableStateOf("5171") }
            var okexpayApiSecret by remember { mutableStateOf("eb6da0e4cab2d2ea9df343404ac8d2c2") }
            var hasLoadedOkexpayConfigs by remember { mutableStateOf(false) }
            
            LaunchedEffect(allConfigs) {
                if (!hasLoadedOkexpayConfigs && allConfigs.isNotEmpty()) {
                    allConfigs.find { it.key == "okexpay_base_url" }?.value?.let { okexpayBaseUrl = it }
                    allConfigs.find { it.key == "okexpay_mch_id" }?.value?.let { okexpayMchId = it }
                    allConfigs.find { it.key == "okexpay_api_secret" }?.value?.let { okexpayApiSecret = it }
                    hasLoadedOkexpayConfigs = true
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Gateway Credentials", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = okexpayBaseUrl,
                        onValueChange = { okexpayBaseUrl = it },
                        label = { Text("Base URL") },
                        placeholder = { Text("https://sandbox.okexpay.dev/") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = okexpayMchId,
                        onValueChange = { okexpayMchId = it },
                        label = { Text("Merchant ID (mchId)") },
                        placeholder = { Text("5171") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = okexpayApiSecret,
                        onValueChange = { okexpayApiSecret = it },
                        label = { Text("API Secret Key") },
                        placeholder = { Text("eb6da0e4cab2d2ea9df343404ac8d2c2") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    var saveMessage by remember { mutableStateOf("") }
                    if (saveMessage.isNotBlank()) {
                        Text(saveMessage, color = if (saveMessage.contains("success")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Button(
                        onClick = {
                            if (okexpayBaseUrl.isNotBlank() && okexpayMchId.isNotBlank() && okexpayApiSecret.isNotBlank()) {
                                viewModel.updateConfig("okexpay_base_url", okexpayBaseUrl.trim())
                                viewModel.updateConfig("okexpay_mch_id", okexpayMchId.trim())
                                viewModel.updateConfig("okexpay_api_secret", okexpayApiSecret.trim())
                                saveMessage = "OKExPay configurations saved successfully!"
                            } else {
                                saveMessage = "All fields are required!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save OKExPay Settings")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Global Commission Settings", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

            var globalCommissionPercent by remember { mutableStateOf("2.0") }
            var globalDpCommissionPercent by remember { mutableStateOf("2.0") }
            var globalWdCommissionPercent by remember { mutableStateOf("2.0") }
            var hasLoadedGlobalComm by remember { mutableStateOf(false) }

            LaunchedEffect(allConfigs) {
                if (!hasLoadedGlobalComm && allConfigs.isNotEmpty()) {
                    allConfigs.find { it.key == "global_commission_percent" }?.value?.let { globalCommissionPercent = it }
                    allConfigs.find { it.key == "global_dp_commission_percent" }?.value?.let { globalDpCommissionPercent = it }
                    allConfigs.find { it.key == "global_wd_commission_percent" }?.value?.let { globalWdCommissionPercent = it }
                    hasLoadedGlobalComm = true
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Agent Global Commissions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    Text(
                        "Configure global default commission percentages. Saving a percentage applies it immediately to all existing agents and cashiers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    var commSaveMessage by remember { mutableStateOf("") }
                    if (commSaveMessage.isNotBlank()) {
                        Text(
                            commSaveMessage,
                            color = if (commSaveMessage.contains("successfully")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Section 1: General Commission
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("General Commission (%)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = globalCommissionPercent,
                            onValueChange = { globalCommissionPercent = it },
                            label = { Text("General Commission Percentage") },
                            placeholder = { Text("2.0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val percent = globalCommissionPercent.toDoubleOrNull()
                                if (percent != null && percent >= 0.0) {
                                    viewModel.updateGlobalCommission(percent)
                                    commSaveMessage = "Global general commission set and applied successfully!"
                                } else {
                                    commSaveMessage = "Please enter a valid numeric general commission percentage!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Apply General Commission")
                        }
                    }

                    HorizontalDivider()

                    // Section 2: DP (Deposit) Commission
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Deposit (DP) Commission (%)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = globalDpCommissionPercent,
                            onValueChange = { globalDpCommissionPercent = it },
                            label = { Text("Deposit Commission Percentage") },
                            placeholder = { Text("2.0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val percent = globalDpCommissionPercent.toDoubleOrNull()
                                if (percent != null && percent >= 0.0) {
                                    viewModel.updateGlobalDpCommission(percent)
                                    commSaveMessage = "Global Deposit (DP) commission set and applied successfully!"
                                } else {
                                    commSaveMessage = "Please enter a valid numeric deposit commission percentage!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Apply DP Commission")
                        }
                    }

                    HorizontalDivider()

                    // Section 3: WD (Withdrawal) Commission
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Withdrawal (WD) Commission (%)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = globalWdCommissionPercent,
                            onValueChange = { globalWdCommissionPercent = it },
                            label = { Text("Withdrawal Commission Percentage") },
                            placeholder = { Text("2.0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val percent = globalWdCommissionPercent.toDoubleOrNull()
                                if (percent != null && percent >= 0.0) {
                                    viewModel.updateGlobalWdCommission(percent)
                                    commSaveMessage = "Global Withdrawal (WD) commission set and applied successfully!"
                                } else {
                                    commSaveMessage = "Please enter a valid numeric withdrawal commission percentage!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Apply WD Commission")
                        }
                    }
                }
            }
        }
    }
}
