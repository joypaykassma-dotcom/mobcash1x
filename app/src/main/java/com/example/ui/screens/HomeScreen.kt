package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WalletViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WalletViewModel,
    onNavigateAction: (String) -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateStatement: () -> Unit,
    onNavigateOffer: () -> Unit,
    onNavigateSupport: () -> Unit,
    onNavigateReferDashboard: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val stats by viewModel.walletStats.collectAsState()
    
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundLight), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    val scrollState = rememberScrollState()
    var infoDialogData by remember { mutableStateOf<Pair<String, String>?>(null) }

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

    Scaffold(
        bottomBar = {
            Column {
                Divider(color = BorderColor, thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavButton(icon = Icons.Filled.Home, label = "Home", color = PrimaryBlue, onClick = {})
                    BottomNavButton(icon = Icons.Outlined.List, label = "TrxID", color = TextGray, onClick = onNavigateStatement)
                    BottomNavButton(icon = Icons.Outlined.LocalOffer, label = "Offer", color = TextGray, onClick = onNavigateOffer)
                    BottomNavButton(icon = Icons.Outlined.Person, label = "Profile", color = TextGray, onClick = onNavigateProfile)
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
                    Text("My Wallet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text(user!!.accountType.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = onNavigateReferDashboard,
                        modifier = Modifier.size(40.dp).background(AddBg, CircleShape)
                    ) {
                        Icon(Icons.Filled.GroupAdd, contentDescription = "Refer", tint = PrimaryBlue)
                    }
                    IconButton(
                        onClick = onNavigateSupport,
                        modifier = Modifier.size(40.dp).background(AddBg, CircleShape)
                    ) {
                        Icon(Icons.Outlined.SupportAgent, contentDescription = "Support", tint = PrimaryBlue)
                    }
                    IconButton(
                        onClick = onNavigateProfile,
                        modifier = Modifier.size(40.dp).background(PrimaryBlue, CircleShape)
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            }
            Divider(color = BorderColor, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balances Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(PrimaryBlue)
                        .padding(24.dp)
                ) {
                    // Decorative Circle (simple approximation)
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 24.dp, y = (-24).dp).size(128.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
                    
                    Column {
                        Text("Main Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${user!!.currency} ${user!!.mainBalance}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text("A/C NO", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("ACC${user!!.id.toString().padStart(5, '0')}", color = Color.White, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                            Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(user!!.currency, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Secondary Balances
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BalanceMiniCard(modifier = Modifier.weight(1f), title = "Hold", amount = "${user!!.currency} ${user!!.holdBalance}", amountColor = HoldText)
                    BalanceMiniCard(modifier = Modifier.weight(1f), title = "Limit", amount = "${user!!.currency} ${user!!.withdrawLimit}", amountColor = LimitText)
                    BalanceMiniCard(modifier = Modifier.weight(1f), title = "Out Bal.", amount = "${user!!.currency} ${user!!.withdrawableBalance}", amountColor = AvailableText)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (user!!.accountType.equals("Agent", ignoreCase = true)) {
                        BalanceMiniCard(modifier = Modifier.weight(1f), title = "Commission", amount = "${user!!.currency} ${user!!.commissionBalance}", amountColor = PrimaryBlue)
                    } else {
                        BalanceMiniCard(modifier = Modifier.weight(1f), title = "Bonus", amount = "${user!!.currency} ${user!!.bonusBalance}", amountColor = PrimaryBlue)
                    }
                    BalanceMiniCard(modifier = Modifier.weight(1f), title = "Refer", amount = "${user!!.currency} ${user!!.referBalance}", amountColor = AvailableText)
                }

                // Actions Grid
                val allConfigs by viewModel.allConfigs.collectAsState()
                
                val rolePrefix = when {
                    user!!.accountType.equals("Agent", ignoreCase = true) -> "agent"
                    user!!.accountType.equals("Cashier", ignoreCase = true) -> "cashier"
                    else -> "user"
                }

                val gDeposit = (allConfigs.find { it.key == "${rolePrefix}_deposit_enabled" }?.value ?: "true") == "true"
                val gWithdraw = (allConfigs.find { it.key == "${rolePrefix}_withdraw_enabled" }?.value ?: "true") == "true"
                val gAdd = (allConfigs.find { it.key == "${rolePrefix}_add_money_enabled" }?.value ?: "true") == "true"
                val gOut = (allConfigs.find { it.key == "${rolePrefix}_out_money_enabled" }?.value ?: "true") == "true"
                val gSend = (allConfigs.find { it.key == "${rolePrefix}_send_money_enabled" }?.value ?: "true") == "true"
                val gReceive = (allConfigs.find { it.key == "${rolePrefix}_receive_money_enabled" }?.value ?: "true") == "true"
                
                val showDeposit = gDeposit && user!!.canDeposit
                val showWithdraw = gWithdraw && user!!.canWithdraw
                val showAddMoney = gAdd && user!!.canAddMoney
                val showOutMoney = gOut && user!!.canOutMoney
                val showSendMoney = gSend && user!!.canSendMoney
                val showReceiveMoney = gReceive && user!!.canReceiveMoney

                if (user!!.accountType.equals("Cashier", ignoreCase = true)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (showAddMoney) ActionIconButton(icon = Icons.Outlined.AddCircleOutline, label = "Customer\nAdd", bg = AddBg, tint = AddIcon, onClick = { onNavigateAction("Customer Add Money") }, onInfoClick = { infoDialogData = "Customer Add Money" to "Add funds to a customer's account." })
                        if (showOutMoney) ActionIconButton(icon = Icons.Outlined.ArrowCircleUp, label = "Customer\nOut", bg = OutBg, tint = OutIcon, onClick = { onNavigateAction("Customer Cash Out") }, onInfoClick = { infoDialogData = "Customer Cash Out" to "Cash out funds from a customer's account." })
                        if (showDeposit) ActionIconButton(icon = Icons.Outlined.AccountBalance, label = "Cashier\nDeposit", bg = DepositBg, tint = DepositIcon, onClick = { onNavigateAction("Cashier Deposit") }, onInfoClick = { infoDialogData = "Cashier Deposit" to "Transfer money to a betting site." })
                        if (showWithdraw) ActionIconButton(icon = Icons.Outlined.Payments, label = "Cashier\nWithdraw", bg = WithdrawBg, tint = WithdrawIcon, onClick = { onNavigateAction("Cashier Withdraw") }, onInfoClick = { infoDialogData = "Cashier Withdraw" to "Receive money from a betting site." })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (showSendMoney) ActionIconButton(icon = Icons.Outlined.Send, label = "B2B Sent\nMoney", bg = SendBg, tint = SendIcon, onClick = { onNavigateAction("B2B Sent Money") }, onInfoClick = { infoDialogData = "B2B Sent Money" to "Transfer money directly to another user or business." })
                        Spacer(modifier = Modifier.weight(1f))
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (showAddMoney) ActionIconButton(icon = Icons.Outlined.AddCircleOutline, label = "Add", bg = AddBg, tint = AddIcon, onClick = { onNavigateAction("Add Money") }, onInfoClick = { infoDialogData = "Add Money" to "Add funds to your main balance using supported payment methods." })
                        if (showOutMoney) ActionIconButton(icon = Icons.Outlined.ArrowCircleUp, label = "Out", bg = OutBg, tint = OutIcon, onClick = { onNavigateAction("Out Money") }, onInfoClick = { infoDialogData = "Out Money" to "Withdraw money from your available balance." })
                        if (showDeposit) ActionIconButton(icon = Icons.Outlined.AccountBalance, label = "Deposit", bg = DepositBg, tint = DepositIcon, onClick = { onNavigateAction("Deposit") }, onInfoClick = { infoDialogData = "Deposit" to "Deposit your money to your betting account." })
                        if (showWithdraw) ActionIconButton(icon = Icons.Outlined.Payments, label = "Withdraw", bg = WithdrawBg, tint = WithdrawIcon, onClick = { onNavigateAction("Withdraw") }, onInfoClick = { infoDialogData = "Withdraw" to "Withdraw your money from your betting account." })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (showSendMoney) ActionIconButton(icon = Icons.Outlined.Send, label = "Send", bg = SendBg, tint = SendIcon, onClick = { onNavigateAction("Sent Money") }, onInfoClick = { infoDialogData = "Send Money" to "Send money securely to another user." })
                        if (showReceiveMoney) ActionIconButton(icon = androidx.compose.material.icons.Icons.Outlined.CallReceived, label = "Receive", bg = AddBg, tint = AddIcon, onClick = { onNavigateAction("Receive Money") }, onInfoClick = { infoDialogData = "Receive Money" to "View your details to receive money from others." })
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                
                // Profit Loss
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text("Total Profit Loss", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${user!!.currency} ${stats.totalProfitLoss}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if(stats.totalProfitLoss >= 0) AvailableText else HoldText)
                }

                // Today Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text("Today Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryRow("Total Add Money", "${user!!.currency} ${stats.todayAddMoney}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Total Out Money", "${user!!.currency} ${stats.todayOutMoney}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Today Deposit", "${user!!.currency} ${stats.todayDeposit}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Today Withdraw", "${user!!.currency} ${stats.todayWithdraw}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Today Sent Money", "${user!!.currency} ${stats.todaySentMoney}")
                }

                // Lifetime Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text("Lifetime Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryRow("Total Add Money", "${user!!.currency} ${stats.lifeAddMoney}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Total Out Money", "${user!!.currency} ${stats.lifeOutMoney}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Lifetime Deposit", "${user!!.currency} ${stats.lifeDeposit}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Lifetime Withdraw", "${user!!.currency} ${stats.lifeWithdraw}")
                    Divider(color = BorderLight, thickness = 1.dp)
                    SummaryRow("Lifetime Sent Money", "${user!!.currency} ${stats.lifeSentMoney}")
                }

                // Profile Details Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Profile Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, modifier = Modifier.clickable { onNavigateProfile() })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileDetailRow(label = "Full Name", value = user!!.name, icon = Icons.Outlined.Badge)
                    Divider(color = BorderLight, thickness = 1.dp)
                    ProfileDetailRow(label = "Username", value = user!!.username, icon = Icons.Outlined.AlternateEmail)
                    Divider(color = BorderLight, thickness = 1.dp)
                    ProfileDetailRow(label = "Mobile Number", value = user!!.number, icon = Icons.Outlined.Phone)
                    Divider(color = BorderLight, thickness = 1.dp)
                    ProfileDetailRow(label = "Email Address", value = user!!.email, icon = Icons.Outlined.Email, isLast = true)
                }
            }
        }
    }
}

@Composable
fun BalanceMiniCard(modifier: Modifier = Modifier, title: String, amount: String, amountColor: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = amountColor)
    }
}

@Composable
fun ActionIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, tint: Color, onClick: () -> Unit, onInfoClick: (() -> Unit)? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = tint)
            }
            if (onInfoClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                        .border(1.dp, Color(0xFF2196F3), androidx.compose.foundation.shape.CircleShape)
                        .clickable(onClick = onInfoClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text("i", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isLast: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9E9E9E))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
        }
        Icon(icon, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextGray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
fun BottomNavButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

