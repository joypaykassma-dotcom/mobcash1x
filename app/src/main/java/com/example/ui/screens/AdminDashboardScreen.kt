package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.draw.scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WalletViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: WalletViewModel,
    onNavigateUserDetail: (Int) -> Unit,
    onLogout: () -> Unit,
    onNavigateStatement: () -> Unit,
    onNavigateConfig: () -> Unit,
    onNavigateTickets: () -> Unit
) {
    val admin by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    var showRechargeDialog by remember { mutableStateOf(false) }
    var rechargeAmount by remember { mutableStateOf("") }

    var showMainBalanceRechargeDialog by remember { mutableStateOf(false) }
    var mainBalanceRechargeAmount by remember { mutableStateOf("") }
    
    var searchQuery by remember { mutableStateOf("") }
    
    var showBonusDialog by remember { mutableStateOf(false) }
    var globalBonusPercent by remember { mutableStateOf("") }
    
    var showGlobalReleaseDialog by remember { mutableStateOf(false) }
    var infoDialogData by remember { mutableStateOf<Pair<String, String>?>(null) }

    var selectedUserIds by remember { mutableStateOf(setOf<Int>()) }

    var showBulkPermissionDialog by remember { mutableStateOf(false) }
    var bulkCanDeposit by remember { mutableStateOf(true) }
    var bulkCanWithdraw by remember { mutableStateOf(true) }
    var bulkCanAddMoney by remember { mutableStateOf(true) }
    var bulkCanOutMoney by remember { mutableStateOf(true) }
    var bulkCanSendMoney by remember { mutableStateOf(true) }
    var bulkCanReceiveMoney by remember { mutableStateOf(true) }

    var showBulkStatusDialog by remember { mutableStateOf(false) }
    var bulkIsActive by remember { mutableStateOf(true) }

    var showManageOffersDialog by remember { mutableStateOf(false) }
    var offerTitle by remember { mutableStateOf("") }
    var offerDescription by remember { mutableStateOf("") }
    val offers by viewModel.allOffers.collectAsState()

    if (infoDialogData != null) {
        AlertDialog(
            onDismissRequest = { infoDialogData = null },
            title = { Text(infoDialogData!!.first) },
            text = { Text(infoDialogData!!.second) },
            confirmButton = {
                TextButton(onClick = { infoDialogData = null }) { Text("OK") }
            }
        )
    }

    if (showBulkPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showBulkPermissionDialog = false },
            title = { Text("Bulk Button Permissions", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enable/Unhide or Disable/Hide buttons for all ${selectedUserIds.size} selected users:", fontSize = 13.sp, color = TextGray)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { bulkCanDeposit = !bulkCanDeposit }) {
                        Checkbox(checked = bulkCanDeposit, onCheckedChange = { bulkCanDeposit = it })
                        Text("Show Deposit Button", fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { bulkCanWithdraw = !bulkCanWithdraw }) {
                        Checkbox(checked = bulkCanWithdraw, onCheckedChange = { bulkCanWithdraw = it })
                        Text("Show Withdraw Button", fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { bulkCanAddMoney = !bulkCanAddMoney }) {
                        Checkbox(checked = bulkCanAddMoney, onCheckedChange = { bulkCanAddMoney = it })
                        Text("Show Add Money Button", fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { bulkCanOutMoney = !bulkCanOutMoney }) {
                        Checkbox(checked = bulkCanOutMoney, onCheckedChange = { bulkCanOutMoney = it })
                        Text("Show Out Money Button", fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { bulkCanSendMoney = !bulkCanSendMoney }) {
                        Checkbox(checked = bulkCanSendMoney, onCheckedChange = { bulkCanSendMoney = it })
                        Text("Show Send Money Button", fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { bulkCanReceiveMoney = !bulkCanReceiveMoney }) {
                        Checkbox(checked = bulkCanReceiveMoney, onCheckedChange = { bulkCanReceiveMoney = it })
                        Text("Show Receive Money Button", fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedUsersObj = allUsers.filter { selectedUserIds.contains(it.id) }
                        val updatedUsers = selectedUsersObj.map { u ->
                            u.copy(
                                canDeposit = bulkCanDeposit,
                                canWithdraw = bulkCanWithdraw,
                                canAddMoney = bulkCanAddMoney,
                                canOutMoney = bulkCanOutMoney,
                                canSendMoney = bulkCanSendMoney,
                                canReceiveMoney = bulkCanReceiveMoney
                            )
                        }
                        viewModel.bulkUpdateUsers(updatedUsers)
                        selectedUserIds = emptySet()
                        showBulkPermissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Apply to Selected")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBulkStatusDialog) {
        AlertDialog(
            onDismissRequest = { showBulkStatusDialog = false },
            title = { Text("Bulk Status Update", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Set active/suspended status for all ${selectedUserIds.size} selected users:", fontSize = 13.sp, color = TextGray)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().clickable { bulkIsActive = !bulkIsActive }.padding(vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (bulkIsActive) AvailableText else HoldText)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (bulkIsActive) "Set Active" else "Set Suspended",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (bulkIsActive) AvailableText else HoldText
                            )
                        }
                        Switch(
                            checked = bulkIsActive,
                            onCheckedChange = { bulkIsActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AvailableText,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = HoldText
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedUsersObj = allUsers.filter { selectedUserIds.contains(it.id) }
                        val updatedUsers = selectedUsersObj.map { u ->
                            u.copy(isBlocked = !bulkIsActive)
                        }
                        viewModel.bulkUpdateUsers(updatedUsers)
                        selectedUserIds = emptySet()
                        showBulkStatusDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Apply Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (admin == null) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundLight), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    val scrollState = rememberScrollState()

    // Calculate admin stats
    var systemTotalBalance = 0.0
    var systemTotalWithdraw = 0.0
    allUsers.forEach { user ->
        systemTotalBalance += user.mainBalance
    }
    allTransactions.forEach { tx ->
        if (tx.type == "Withdraw" || tx.type == "Out Money") {
            systemTotalWithdraw += tx.amount
        }
    }

    if (showMainBalanceRechargeDialog) {
        AlertDialog(
            onDismissRequest = { showMainBalanceRechargeDialog = false },
            title = { Text("Recharge Main Balance") },
            text = {
                Column {
                    Text("Add funds directly to Admin Main Balance.", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mainBalanceRechargeAmount,
                        onValueChange = { mainBalanceRechargeAmount = it },
                        label = { Text("Amount") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = mainBalanceRechargeAmount.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.rechargeMainBalance(amount)
                    }
                    showMainBalanceRechargeDialog = false
                    mainBalanceRechargeAmount = ""
                }) {
                    Text("Recharge")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMainBalanceRechargeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRechargeDialog) {
        AlertDialog(
            onDismissRequest = { showRechargeDialog = false },
            title = { Text("Recharge Gas Fee") },
            text = {
                Column {
                    Text("Gas fee is 1%. E.g., 100 BDT Gas Fee Recharge = 10,000 BDT Gas Fee Balance.", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rechargeAmount,
                        onValueChange = { rechargeAmount = it },
                        label = { Text("Amount") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = rechargeAmount.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.rechargeGasFee(amount)
                    }
                    showRechargeDialog = false
                    rechargeAmount = ""
                }) {
                    Text("Recharge")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRechargeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBonusDialog) {
        AlertDialog(
            onDismissRequest = { showBonusDialog = false },
            title = { Text("Set Global Bonus %") },
            text = {
                Column {
                    Text("This bonus % will apply to all new users by default.", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = globalBonusPercent,
                        onValueChange = { globalBonusPercent = it },
                        label = { Text("Bonus %") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val percent = globalBonusPercent.toDoubleOrNull()
                    if (percent != null) {
                        viewModel.updateUser(admin!!.copy(bonusPercent = percent))
                    }
                    showBonusDialog = false
                    globalBonusPercent = ""
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBonusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGlobalReleaseDialog) {
        AlertDialog(
            onDismissRequest = { showGlobalReleaseDialog = false },
            title = { Text("Global Commission Release") },
            text = {
                Text("Are you sure you want to release commission for ALL agents? This will transfer their commission balance to their main balance.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.globalCommissionRelease()
                    showGlobalReleaseDialog = false
                }) {
                    Text("Release All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGlobalReleaseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showManageOffersDialog) {
        AlertDialog(
            onDismissRequest = { showManageOffersDialog = false },
            title = { Text("Manage Promotional Offers", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Publish a new offer or manage existing ones:", fontSize = 13.sp, color = TextGray)
                    
                    OutlinedTextField(
                        value = offerTitle,
                        onValueChange = { offerTitle = it },
                        label = { Text("Offer Title") },
                        placeholder = { Text("e.g. 10% Cash Back on Deposit") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = offerDescription,
                        onValueChange = { offerDescription = it },
                        label = { Text("Offer Description") },
                        placeholder = { Text("e.g. Deposit today and get 10% cash back instantly.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            val titleStr = offerTitle.trim()
                            val descStr = offerDescription.trim()
                            if (titleStr.isNotBlank() && descStr.isNotBlank()) {
                                val dateFormat = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm", java.util.Locale.getDefault())
                                val currentDateStr = dateFormat.format(java.util.Date())
                                viewModel.addOffer(titleStr, descStr, currentDateStr) {
                                    offerTitle = ""
                                    offerDescription = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send/Publish Offer")
                    }

                    Divider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Text("Active Offers (${offers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)

                    if (offers.isEmpty()) {
                        Text("No active offers. Use the form above to publish one.", fontSize = 11.sp, color = TextGray)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(offers) { offer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundLight, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(offer.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDark)
                                        Text(offer.description, fontSize = 10.sp, color = TextGray)
                                        Text(offer.dateCreated, fontSize = 9.sp, color = TextGray)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteOffer(offer.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageOffersDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = BorderColor, thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavButton(icon = Icons.Filled.Home, label = "Admin", color = PrimaryBlue, onClick = {})
                    BottomNavButton(icon = Icons.Outlined.List, label = "Trx/History", color = TextGray, onClick = onNavigateStatement)
                    BottomNavButton(icon = Icons.Outlined.ExitToApp, label = "Logout", color = HoldText, onClick = {
                        viewModel.logout()
                        onLogout()
                    })
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Admin Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text("Control Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                }
                Box(modifier = Modifier.size(40.dp).background(PrimaryBlue, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = "Admin", tint = Color.White)
                }
            }
            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            // Alert for Pending/Recent Withdrawals
            val recentWithdrawals = allTransactions.filter { 
                it.type.startsWith("Withdraw") && (System.currentTimeMillis() - it.timestamp) < 24 * 60 * 60 * 1000 
            }
            if (recentWithdrawals.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF4F4)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Alert", tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${recentWithdrawals.size} recent withdrawal(s) in the last 24h.", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = BorderColor, thickness = 1.dp)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Admin Balances (Clean, focused only on balances with compact quick action recharge triggers)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryBlue)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Admin Main Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text("${admin!!.currency} ${admin!!.mainBalance}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { showMainBalanceRechargeDialog = true },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Recharge Main Balance")
                            }
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Admin Gas Fee Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text("${admin!!.currency} ${admin!!.gasFeeBalance}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { showRechargeDialog = true },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Recharge Gas Fee")
                            }
                        }
                    }
                }

                // Admin Panel Tools Section (Beautifully arranged grid of key actions)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Admin Panel Tools", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    
                    // Row 1: System Config & Support Tickets
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).clickable { onNavigateConfig() },
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                Text("System Configuration", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Configure betting & payments", fontSize = 10.sp, color = TextGray)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f).clickable { onNavigateTickets() },
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Email, contentDescription = "Support", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                Text("Support Tickets", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Reply to user tickets", fontSize = 10.sp, color = TextGray)
                            }
                        }
                    }
                    
                    // Row 2: Default Bonus % & Release Commissions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).clickable { showBonusDialog = true },
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Star, contentDescription = "Bonus", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                Text("Global Bonus %", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Set default new user bonus", fontSize = 10.sp, color = TextGray)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f).clickable { showGlobalReleaseDialog = true },
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Release", tint = AvailableText, modifier = Modifier.size(24.dp))
                                Text("Release Commissions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Globally release to all agents", fontSize = 10.sp, color = TextGray)
                            }
                        }
                    }

                    // Row 3: Send & Manage Offers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).clickable { showManageOffersDialog = true },
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LocalOffer, contentDescription = "Offers", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                Text("Manage Offers", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Send & delete various promotional offers", fontSize = 10.sp, color = TextGray)
                            }
                        }
                        Box(modifier = Modifier.weight(1f))
                    }
                }

                // System Overview (Grid Layout)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text("System Overview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DashboardWidget(modifier = Modifier.weight(1f), title = "Total Users", value = "${allUsers.size}")
                        DashboardWidget(modifier = Modifier.weight(1f), title = "Transactions", value = "${allTransactions.size}")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DashboardWidget(modifier = Modifier.weight(1f), title = "System Balance", value = "${admin!!.currency} $systemTotalBalance", color = AvailableText)
                        DashboardWidget(modifier = Modifier.weight(1f), title = "System Withdrawals", value = "${admin!!.currency} $systemTotalWithdraw", color = HoldText)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateStatement,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View All Transactions (Monitor List)")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.Info, contentDescription = "Info", modifier = Modifier.size(16.dp).clickable { infoDialogData = "All Transactions" to "Monitor all transactions across the platform." })
                        }
                    }
                }
                
                VolumeChart(transactions = allTransactions)

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("All Users", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 8.dp))
                }
                var selectedUserTab by remember { mutableStateOf("All") }
                
                ScrollableTabRow(
                    selectedTabIndex = listOf("All", "User", "Agent", "Cashier", "Admin").indexOf(selectedUserTab),
                    containerColor = Color.Transparent,
                    contentColor = PrimaryBlue,
                    edgePadding = 0.dp
                ) {
                    listOf("All", "User", "Agent", "Cashier", "Admin").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedUserTab == title,
                            onClick = { selectedUserTab = title },
                            text = { Text(title) }
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by username, phone, email") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )

                val filteredUsers = allUsers.filter {
                    (selectedUserTab == "All" || 
                     (selectedUserTab == "Admin" && it.role == "Admin") ||
                     (selectedUserTab != "Admin" && it.role != "Admin" && (
                         (selectedUserTab == "Personal" || selectedUserTab == "User") && it.accountType == "Personal" ||
                         selectedUserTab == "Agent" && it.accountType == "Agent" ||
                         selectedUserTab == "Cashier" && it.accountType == "Cashier"
                     ))) &&
                    (it.username.contains(searchQuery, ignoreCase = true) ||
                    it.number.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true))
                }

                if (selectedUserIds.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedUserIds.size} Users Selected",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                TextButton(
                                    onClick = { selectedUserIds = emptySet() },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Clear", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showBulkPermissionDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Text("Bulk Buttons", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { showBulkStatusDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AvailableText),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Text("Bulk Status", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (filteredUsers.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedUserIds.size == filteredUsers.size && filteredUsers.isNotEmpty(),
                            onCheckedChange = { checked ->
                                selectedUserIds = if (checked) {
                                    filteredUsers.map { it.id }.toSet()
                                } else {
                                    emptySet()
                                }
                            }
                        )
                        Text(
                            text = "Select All (${filteredUsers.size} users)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                filteredUsers.forEach { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedUserIds.contains(user.id),
                            onCheckedChange = { checked ->
                                selectedUserIds = if (checked) {
                                    selectedUserIds + user.id
                                } else {
                                    selectedUserIds - user.id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            UserDetailCard(
                                user = user,
                                onStatusToggle = { active ->
                                    viewModel.updateUser(user.copy(isBlocked = !active))
                                },
                                onClick = { onNavigateUserDetail(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardWidget(modifier: Modifier = Modifier, title: String, value: String, color: Color = TextDark) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundLight)
            .padding(12.dp)
    ) {
        Text(title, fontSize = 12.sp, color = TextGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun VolumeChart(transactions: List<com.example.data.Transaction>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text("Daily Volume Trend (Last 30 Days)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            // Simplified custom canvas line chart representation
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val deposits = transactions.filter { it.type.startsWith("Deposit") }
                val withdraws = transactions.filter { it.type.startsWith("Withdraw") }
                // Visual simulation for the trend line
                val path = androidx.compose.ui.graphics.Path()
                if (deposits.isNotEmpty() || withdraws.isNotEmpty()) {
                    path.moveTo(0f, size.height)
                    path.lineTo(size.width * 0.3f, size.height * 0.6f)
                    path.lineTo(size.width * 0.7f, size.height * 0.3f)
                    path.lineTo(size.width, size.height * 0.5f)
                    drawPath(
                        path = path,
                        color = PrimaryBlue,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                } else {
                    drawRect(color = BackgroundLight)
                }
            }
            if (transactions.none { it.type.startsWith("Deposit") || it.type.startsWith("Withdraw") }) {
                Text("No data available", color = TextGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun UserDetailCard(
    user: com.example.data.User,
    onStatusToggle: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(user.name, fontWeight = FontWeight.Bold, color = TextDark)
            val displayType = if (user.accountType == "Personal") "User" else user.accountType
            Text(if (user.role == "Admin") "Admin" else displayType, fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Username:", fontSize = 12.sp, color = TextGray)
            Text(user.username, fontSize = 12.sp, color = TextDark)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Balance:", fontSize = 12.sp, color = TextGray)
            Text("${user.currency} ${user.mainBalance}", fontSize = 12.sp, color = AvailableText, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Phone:", fontSize = 12.sp, color = TextGray)
            Text(user.number, fontSize = 12.sp, color = TextDark)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BorderColor)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (user.isBlocked) HoldText else AvailableText)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (user.isBlocked) "Suspended" else "Active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (user.isBlocked) HoldText else AvailableText
                )
            }
            
            if (onStatusToggle != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Toggle Status ", fontSize = 11.sp, color = TextGray)
                    Switch(
                        checked = !user.isBlocked,
                        onCheckedChange = { active ->
                            onStatusToggle(active)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AvailableText,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = HoldText
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }
        }
    }
}
