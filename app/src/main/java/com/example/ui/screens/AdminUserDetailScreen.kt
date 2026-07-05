package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Phone
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    userId: Int,
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.getUserById(userId).collectAsState(initial = null)
    val admin by viewModel.currentUser.collectAsState()
    val transactions by viewModel.getTransactionsForUser(userId).collectAsState(initial = emptyList())
    
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showAddBalanceDialog by remember { mutableStateOf(false) }
    var showRemoveBalanceDialog by remember { mutableStateOf(false) }
    var adjustAmount by remember { mutableStateOf("") }
    var selectedBalanceType by remember { mutableStateOf("Main Balance") }

    var showPenaltyDialog by remember { mutableStateOf(false) }
    var penaltyAmountMain by remember { mutableStateOf("") }
    var penaltyAmountOut by remember { mutableStateOf("") }
    var penaltyAmountCombined by remember { mutableStateOf("") }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundLight), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    var showBonusDialog by remember { mutableStateOf(false) }
    var userBonusPercent by remember { mutableStateOf(user?.bonusPercent?.toString() ?: "1.0") }
    
    var showAgentCommDialog by remember { mutableStateOf(false) }
    var userAgentCommPercent by remember { mutableStateOf(user?.agentCommissionPercent?.toString() ?: "2.0") }
    var userAgentDpCommPercent by remember { mutableStateOf(user?.agentDpCommissionPercent?.toString() ?: "2.0") }
    var userAgentWdCommPercent by remember { mutableStateOf(user?.agentWdCommissionPercent?.toString() ?: "2.0") }
    
    var showReleaseCommDialog by remember { mutableStateOf(false) }
    var releaseCommAmount by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }

    if (showAgentCommDialog) {
        AlertDialog(
            onDismissRequest = { showAgentCommDialog = false },
            title = { Text("Set Agent Commission %") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This sets the general, deposit, and withdrawal commission percentages for this agent.", fontSize = 12.sp, color = TextGray)
                    
                    OutlinedTextField(
                        value = userAgentCommPercent,
                        onValueChange = { userAgentCommPercent = it },
                        label = { Text("General Commission %") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = userAgentDpCommPercent,
                        onValueChange = { userAgentDpCommPercent = it },
                        label = { Text("Deposit (DP) Commission %") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = userAgentWdCommPercent,
                        onValueChange = { userAgentWdCommPercent = it },
                        label = { Text("Withdrawal (WD) Commission %") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val percent = userAgentCommPercent.toDoubleOrNull()
                    val dpPercent = userAgentDpCommPercent.toDoubleOrNull()
                    val wdPercent = userAgentWdCommPercent.toDoubleOrNull()
                    if (percent != null && dpPercent != null && wdPercent != null) {
                        viewModel.updateUser(user!!.copy(
                            agentCommissionPercent = percent,
                            agentDpCommissionPercent = dpPercent,
                            agentWdCommissionPercent = wdPercent
                        ))
                    }
                    showAgentCommDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAgentCommDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReleaseCommDialog) {
        AlertDialog(
            onDismissRequest = { showReleaseCommDialog = false },
            title = { Text("Release Commission") },
            text = {
                Column {
                    Text("Release commission to user's main balance.", fontSize = 12.sp, color = TextGray)
                    Text("Available: ${user!!.currency} ${user!!.commissionBalance}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = releaseCommAmount,
                        onValueChange = { releaseCommAmount = it },
                        label = { Text("Amount to Release") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = releaseCommAmount.toDoubleOrNull()
                    if (amount != null && amount > 0 && amount <= user!!.commissionBalance) {
                        viewModel.updateUser(user!!.copy(
                            commissionBalance = user!!.commissionBalance - amount,
                            mainBalance = user!!.mainBalance + amount
                        ))
                        viewModel.logCommissionRelease(user!!.id, amount)
                    }
                    showReleaseCommDialog = false
                }) {
                    Text("Release")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReleaseCommDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBonusDialog) {
        AlertDialog(
            onDismissRequest = { showBonusDialog = false },
            title = { Text("Set User Bonus %") },
            text = {
                Column {
                    Text("This sets the specific bonus percentage for this user.", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userBonusPercent,
                        onValueChange = { userBonusPercent = it },
                        label = { Text("Bonus %") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val percent = userBonusPercent.toDoubleOrNull()
                    if (percent != null) {
                        viewModel.updateUser(user!!.copy(bonusPercent = percent))
                    }
                    showBonusDialog = false
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

    if (showEditDialog) {
        var eName by remember { mutableStateOf(user!!.name) }
        var eUsername by remember { mutableStateOf(user!!.username) }
        var eEmail by remember { mutableStateOf(user!!.email) }
        var ePhone by remember { mutableStateOf(user!!.number) }
        var eBkash by remember { mutableStateOf(user!!.bkashNumber) }
        var eNagad by remember { mutableStateOf(user!!.nagadNumber) }
        var eRocket by remember { mutableStateOf(user!!.rocketNumber) }
        var ePass by remember { mutableStateOf(user!!.passwordHash) }
        var ePin by remember { mutableStateOf(user!!.withdrawPin) }
        var eEposCode by remember { mutableStateOf(user!!.eposCode) }
        var eRole by remember { mutableStateOf(user!!.role) }
        var eAccType by remember { mutableStateOf(user!!.accountType) }

        var eMain by remember { mutableStateOf(user!!.mainBalance.toString()) }
        var eOut by remember { mutableStateOf(user!!.withdrawableBalance.toString()) }
        var eHold by remember { mutableStateOf(user!!.holdBalance.toString()) }
        var eBonus by remember { mutableStateOf(user!!.bonusBalance.toString()) }
        var eRefer by remember { mutableStateOf(user!!.referBalance.toString()) }
        var eLimit by remember { mutableStateOf(user!!.withdrawLimit.toString()) }
        var eComm by remember { mutableStateOf(user!!.commissionBalance.toString()) }
        var eGas by remember { mutableStateOf(user!!.gasFeeBalance.toString()) }
        var eHiddenPenaltyMain by remember { mutableStateOf(user!!.hiddenPenaltyMain.toString()) }
        var eHiddenPenaltyOut by remember { mutableStateOf(user!!.hiddenPenaltyOut.toString()) }
        var eHiddenPenaltyCombined by remember { mutableStateOf(user!!.hiddenPenaltyCombined.toString()) }
        var eIgnoreLimit by remember { mutableStateOf(user!!.ignoreWithdrawLimit) }
        var eBlock by remember { mutableStateOf(user!!.isBlocked) }
        var eHoldAcc by remember { mutableStateOf(user!!.isHold) }
        
        var eCanDeposit by remember { mutableStateOf(user!!.canDeposit) }
        var eCanWithdraw by remember { mutableStateOf(user!!.canWithdraw) }
        var eCanAddMoney by remember { mutableStateOf(user!!.canAddMoney) }
        var eCanOutMoney by remember { mutableStateOf(user!!.canOutMoney) }
        var eCanSendMoney by remember { mutableStateOf(user!!.canSendMoney) }
        var eCanReceiveMoney by remember { mutableStateOf(user!!.canReceiveMoney) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit All User Details") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Personal Info", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eUsername, onValueChange = { eUsername = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eEmail, onValueChange = { eEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ePhone, onValueChange = { ePhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    
                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Banking Info", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    OutlinedTextField(value = eBkash, onValueChange = { eBkash = it }, label = { Text("bKash") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eNagad, onValueChange = { eNagad = it }, label = { Text("Nagad") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eRocket, onValueChange = { eRocket = it }, label = { Text("Rocket") }, modifier = Modifier.fillMaxWidth())
                    
                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Security", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    OutlinedTextField(value = ePass, onValueChange = { ePass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ePin, onValueChange = { ePin = it }, label = { Text("Secret PIN") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eEposCode, onValueChange = { eEposCode = it }, label = { Text("EPOS Code (Cashier only)") }, modifier = Modifier.fillMaxWidth())
                    
                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Account Settings", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    OutlinedTextField(value = eAccType, onValueChange = { eAccType = it }, label = { Text("Account Type (Personal/Cashier/Agent)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eRole, onValueChange = { eRole = it }, label = { Text("Role (User/Admin)") }, modifier = Modifier.fillMaxWidth())

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Balances & Limits", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    OutlinedTextField(value = eMain, onValueChange = { eMain = it }, label = { Text("Main Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eOut, onValueChange = { eOut = it }, label = { Text("Out Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eHold, onValueChange = { eHold = it }, label = { Text("Hold Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eBonus, onValueChange = { eBonus = it }, label = { Text("Bonus Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eRefer, onValueChange = { eRefer = it }, label = { Text("Refer Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eComm, onValueChange = { eComm = it }, label = { Text("Commission Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eGas, onValueChange = { eGas = it }, label = { Text("Gas Fee Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eLimit, onValueChange = { eLimit = it }, label = { Text("Withdraw Limit") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eHiddenPenaltyMain, onValueChange = { eHiddenPenaltyMain = it }, label = { Text("Main Balance Hidden Penalty") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eHiddenPenaltyOut, onValueChange = { eHiddenPenaltyOut = it }, label = { Text("Out Balance Hidden Penalty") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eHiddenPenaltyCombined, onValueChange = { eHiddenPenaltyCombined = it }, label = { Text("Combined (Main+Out) Hidden Penalty") }, modifier = Modifier.fillMaxWidth())
                    
                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Status", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = eIgnoreLimit, onCheckedChange = { eIgnoreLimit = it })
                        Text("Ignore Withdraw Limit")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = eBlock, onCheckedChange = { eBlock = it })
                        Text("Block Account")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = eHoldAcc, onCheckedChange = { eHoldAcc = it })
                        Text("Hold Account (No Withdraw)")
                    }
                    
                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Action Permissions", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = eCanDeposit, onCheckedChange = { eCanDeposit = it }); Text("Can Deposit") }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = eCanWithdraw, onCheckedChange = { eCanWithdraw = it }); Text("Can Withdraw") }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = eCanAddMoney, onCheckedChange = { eCanAddMoney = it }); Text("Can Add Money") }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = eCanOutMoney, onCheckedChange = { eCanOutMoney = it }); Text("Can Out Money") }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = eCanSendMoney, onCheckedChange = { eCanSendMoney = it }); Text("Can Send Money") }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = eCanReceiveMoney, onCheckedChange = { eCanReceiveMoney = it }); Text("Can Receive Money") }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateUser(user!!.copy(
                        name = eName,
                        username = eUsername,
                        email = eEmail,
                        number = ePhone,
                        bkashNumber = eBkash,
                        nagadNumber = eNagad,
                        rocketNumber = eRocket,
                        passwordHash = ePass,
                        withdrawPin = ePin,
                        eposCode = eEposCode,
                        role = eRole,
                        accountType = eAccType,
                        mainBalance = eMain.toDoubleOrNull() ?: user!!.mainBalance,
                        withdrawableBalance = eOut.toDoubleOrNull() ?: user!!.withdrawableBalance,
                        holdBalance = eHold.toDoubleOrNull() ?: user!!.holdBalance,
                        bonusBalance = eBonus.toDoubleOrNull() ?: user!!.bonusBalance,
                        referBalance = eRefer.toDoubleOrNull() ?: user!!.referBalance,
                        commissionBalance = eComm.toDoubleOrNull() ?: user!!.commissionBalance,
                        gasFeeBalance = eGas.toDoubleOrNull() ?: user!!.gasFeeBalance,
                        withdrawLimit = eLimit.toDoubleOrNull() ?: user!!.withdrawLimit,
                        hiddenPenaltyMain = eHiddenPenaltyMain.toDoubleOrNull() ?: user!!.hiddenPenaltyMain,
                        hiddenPenaltyOut = eHiddenPenaltyOut.toDoubleOrNull() ?: user!!.hiddenPenaltyOut,
                        hiddenPenaltyCombined = eHiddenPenaltyCombined.toDoubleOrNull() ?: user!!.hiddenPenaltyCombined,
                        ignoreWithdrawLimit = eIgnoreLimit,
                        isBlocked = eBlock,
                        isHold = eHoldAcc,
                        canDeposit = eCanDeposit,
                        canWithdraw = eCanWithdraw,
                        canAddMoney = eCanAddMoney,
                        canOutMoney = eCanOutMoney,
                        canSendMoney = eCanSendMoney,
                        canReceiveMoney = eCanReceiveMoney
                    ))
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddBalanceDialog) {
        val adminUser = admin
        var dropdownExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { 
                showAddBalanceDialog = false
                adjustAmount = ""
            },
            title = { Text("Add Balance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will deduct from Admin's Main Balance and add to user's selected balance type.", fontSize = 12.sp, color = TextGray)
                    
                    if (adminUser != null) {
                        Text("Admin Main Balance: ${adminUser.currency} ${adminUser.mainBalance}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Balance Type: $selectedBalanceType")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val types = listOf(
                                "Main Balance",
                                "Hold Balance",
                                "Withdrawable Balance",
                                "Refer Balance",
                                "Bonus Balance",
                                "Commission Balance",
                                "Gas Fee Balance",
                                "Withdraw Limit"
                            )
                            types.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedBalanceType = type
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val currentVal = when (selectedBalanceType) {
                        "Main Balance" -> user!!.mainBalance
                        "Hold Balance" -> user!!.holdBalance
                        "Withdrawable Balance" -> user!!.withdrawableBalance
                        "Refer Balance" -> user!!.referBalance
                        "Bonus Balance" -> user!!.bonusBalance
                        "Commission Balance" -> user!!.commissionBalance
                        "Gas Fee Balance" -> user!!.gasFeeBalance
                        "Withdraw Limit" -> user!!.withdrawLimit
                        else -> user!!.mainBalance
                    }
                    Text("Current User $selectedBalanceType: ${user!!.currency} $currentVal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adjustAmount,
                        onValueChange = { adjustAmount = it },
                        label = { Text("Amount to Add") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = adjustAmount.toDoubleOrNull()
                    val adminId = adminUser?.id
                    if (amount != null && amount > 0 && adminId != null) {
                        viewModel.adjustUserBalance(
                            userId = user!!.id,
                            adminId = adminId,
                            amount = amount,
                            balanceType = selectedBalanceType,
                            isAdd = true,
                            onResult = { error ->
                                if (error != null) {
                                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "$selectedBalanceType added successfully", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    } else {
                        android.widget.Toast.makeText(context, "Invalid amount", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    showAddBalanceDialog = false
                    adjustAmount = ""
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddBalanceDialog = false
                    adjustAmount = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRemoveBalanceDialog) {
        val adminUser = admin
        var dropdownExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { 
                showRemoveBalanceDialog = false
                adjustAmount = ""
            },
            title = { Text("Remove Balance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will deduct from user's selected balance type and add back to Admin's Main Balance.", fontSize = 12.sp, color = TextGray)
                    
                    if (adminUser != null) {
                        Text("Admin Main Balance: ${adminUser.currency} ${adminUser.mainBalance}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Balance Type: $selectedBalanceType")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val types = listOf(
                                "Main Balance",
                                "Hold Balance",
                                "Withdrawable Balance",
                                "Refer Balance",
                                "Bonus Balance",
                                "Commission Balance",
                                "Gas Fee Balance",
                                "Withdraw Limit"
                            )
                            types.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedBalanceType = type
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val currentVal = when (selectedBalanceType) {
                        "Main Balance" -> user!!.mainBalance
                        "Hold Balance" -> user!!.holdBalance
                        "Withdrawable Balance" -> user!!.withdrawableBalance
                        "Refer Balance" -> user!!.referBalance
                        "Bonus Balance" -> user!!.bonusBalance
                        "Commission Balance" -> user!!.commissionBalance
                        "Gas Fee Balance" -> user!!.gasFeeBalance
                        "Withdraw Limit" -> user!!.withdrawLimit
                        else -> user!!.mainBalance
                    }
                    Text("Current User $selectedBalanceType: ${user!!.currency} $currentVal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adjustAmount,
                        onValueChange = { adjustAmount = it },
                        label = { Text("Amount to Remove") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = adjustAmount.toDoubleOrNull()
                    val adminId = adminUser?.id
                    if (amount != null && amount > 0 && adminId != null) {
                        viewModel.adjustUserBalance(
                            userId = user!!.id,
                            adminId = adminId,
                            amount = amount,
                            balanceType = selectedBalanceType,
                            isAdd = false,
                            onResult = { error ->
                                if (error != null) {
                                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "$selectedBalanceType removed successfully", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    } else {
                        android.widget.Toast.makeText(context, "Invalid amount", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    showRemoveBalanceDialog = false
                    adjustAmount = ""
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRemoveBalanceDialog = false
                    adjustAmount = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPenaltyDialog) {
        AlertDialog(
            onDismissRequest = { showPenaltyDialog = false },
            title = { Text("Set Hidden Penalties") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Specify the hidden penalty amounts to deduct silently when funds are available.", fontSize = 12.sp, color = TextGray)
                    OutlinedTextField(
                        value = penaltyAmountMain,
                        onValueChange = { penaltyAmountMain = it },
                        label = { Text("Main Balance Penalty") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = penaltyAmountOut,
                        onValueChange = { penaltyAmountOut = it },
                        label = { Text("Out Balance Penalty") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = penaltyAmountCombined,
                        onValueChange = { penaltyAmountCombined = it },
                        label = { Text("Combined (Main & Out) Penalty") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val mainPen = penaltyAmountMain.toDoubleOrNull() ?: 0.0
                    val outPen = penaltyAmountOut.toDoubleOrNull() ?: 0.0
                    val combinedPen = penaltyAmountCombined.toDoubleOrNull() ?: 0.0
                    
                    viewModel.updateUser(user!!.copy(
                        hiddenPenaltyMain = mainPen,
                        hiddenPenaltyOut = outPen,
                        hiddenPenaltyCombined = combinedPen
                    ))
                    android.widget.Toast.makeText(context, "Hidden penalties updated", android.widget.Toast.LENGTH_SHORT).show()
                    showPenaltyDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPenaltyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        showEditDialog = true
                    }) {
                        Text("Edit", color = PrimaryBlue)
                    }
                    TextButton(onClick = { 
                        userBonusPercent = user!!.bonusPercent.toString()
                        showBonusDialog = true 
                    }) {
                        Text("Set Bonus %", color = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryBlue)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(user!!.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(user!!.accountType, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Main Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Text("${user!!.currency} ${user!!.mainBalance}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            // Other Balances
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BalanceMiniCard(modifier = Modifier.weight(1f), title = "Hold", amount = "${user!!.currency} ${user!!.holdBalance}", amountColor = HoldText)
                BalanceMiniCard(modifier = Modifier.weight(1f), title = "Limit", amount = "${user!!.currency} ${user!!.withdrawLimit}", amountColor = LimitText)
                BalanceMiniCard(modifier = Modifier.weight(1f), title = "Available Out", amount = "${user!!.currency} ${user!!.withdrawableBalance}", amountColor = AvailableText)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BalanceMiniCard(modifier = Modifier.weight(1f), title = "Bonus", amount = "${user!!.currency} ${user!!.bonusBalance}", amountColor = PrimaryBlue)
                BalanceMiniCard(modifier = Modifier.weight(1f), title = "Refer", amount = "${user!!.currency} ${user!!.referBalance}", amountColor = AvailableText)
                if (user!!.accountType.equals("Agent", ignoreCase = true)) {
                    BalanceMiniCard(modifier = Modifier.weight(1f), title = "Comm.", amount = "${user!!.currency} ${user!!.commissionBalance}", amountColor = PrimaryBlue)
                }
            }
            if (user!!.isBlocked || user!!.isHold || user!!.ignoreWithdrawLimit) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (user!!.isBlocked) Text("BLOCKED", color = Color.White, modifier = Modifier.background(Color.Red).padding(4.dp))
                    if (user!!.isHold) Text("ON HOLD", color = Color.White, modifier = Modifier.background(Color.Red).padding(4.dp))
                    if (user!!.ignoreWithdrawLimit) Text("NO LIMIT", color = Color.White, modifier = Modifier.background(PrimaryBlue).padding(4.dp))
                }
            }

            if (user!!.accountType.equals("Agent", ignoreCase = true)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            userAgentCommPercent = user!!.agentCommissionPercent.toString()
                            userAgentDpCommPercent = user!!.agentDpCommissionPercent.toString()
                            userAgentWdCommPercent = user!!.agentWdCommissionPercent.toString()
                            showAgentCommDialog = true 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Set Comm %")
                    }
                    Button(
                        onClick = { showReleaseCommDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AvailableText)
                    ) {
                        Text("Release Comm")
                    }
                }
            }

            if (user!!.role != "Admin") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text("Admin Balance Adjustment", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Directly transfer funds from/to Admin's main balance. Adding balance deducts from Admin; removing balance adds back to Admin.",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showAddBalanceDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AvailableText)
                        ) {
                            Text("Add Balance")
                        }
                        Button(
                            onClick = { showRemoveBalanceDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HoldText)
                        ) {
                            Text("Remove Balance")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text("Hidden Balance Penalty", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Configure silent deductions that will be applied automatically when the user's balance exceeds the penalty threshold. The user will not see any penalty records.",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Main Balance Pending Penalty:", fontSize = 13.sp, color = TextDark)
                        Text("${user!!.currency} ${user!!.hiddenPenaltyMain}", fontWeight = FontWeight.Bold, color = HoldText)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Out Balance Pending Penalty:", fontSize = 13.sp, color = TextDark)
                        Text("${user!!.currency} ${user!!.hiddenPenaltyOut}", fontWeight = FontWeight.Bold, color = HoldText)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Combined (Main+Out) Pending Penalty:", fontSize = 13.sp, color = TextDark)
                        Text("${user!!.currency} ${user!!.hiddenPenaltyCombined}", fontWeight = FontWeight.Bold, color = HoldText)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            penaltyAmountMain = user!!.hiddenPenaltyMain.toString()
                            penaltyAmountOut = user!!.hiddenPenaltyOut.toString()
                            penaltyAmountCombined = user!!.hiddenPenaltyCombined.toString()
                            showPenaltyDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Configure Hidden Penalty")
                    }
                }
            }
            
            // Calculate real-time stats for the user
            var lifeAddMoney = 0.0
            var lifeOutMoney = 0.0
            var lifeDeposit = 0.0
            var lifeWithdraw = 0.0
            var lifeSentMoney = 0.0
            
            transactions.forEach { tx ->
                if (tx.type.startsWith("Add Money")) lifeAddMoney += tx.amount
                else if (tx.type.startsWith("Out Money")) lifeOutMoney += tx.amount
                else if (tx.type.startsWith("Deposit")) lifeDeposit += tx.amount
                else if (tx.type.startsWith("Withdraw")) lifeWithdraw += tx.amount
                else if (tx.type.startsWith("Sent Money")) lifeSentMoney += tx.amount
            }
            val profitLoss = lifeWithdraw - lifeDeposit
            
            // User Summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Text("User Summary (Real-time)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Total Profit Loss", fontSize = 12.sp, color = TextGray)
                Text("${user!!.currency} $profitLoss", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if(profitLoss >= 0) AvailableText else HoldText)
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Add Money", color = TextGray, fontSize = 14.sp)
                    Text("${user!!.currency} $lifeAddMoney", fontWeight = FontWeight.Bold, color = TextDark)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Out Money", color = TextGray, fontSize = 14.sp)
                    Text("${user!!.currency} $lifeOutMoney", fontWeight = FontWeight.Bold, color = TextDark)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Deposit", color = TextGray, fontSize = 14.sp)
                    Text("${user!!.currency} $lifeDeposit", fontWeight = FontWeight.Bold, color = TextDark)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Withdraw", color = TextGray, fontSize = 14.sp)
                    Text("${user!!.currency} $lifeWithdraw", fontWeight = FontWeight.Bold, color = TextDark)
                }
            }

            // Contact Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Text("Contact & Account Info", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileDetailRow("Username", user!!.username, Icons.Outlined.Badge)
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                ProfileDetailRow("Phone", user!!.number, Icons.Outlined.Phone)
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                ProfileDetailRow("Email", user!!.email, Icons.Outlined.Badge)
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                ProfileDetailRow("bKash", user!!.bkashNumber, Icons.Outlined.Phone)
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                ProfileDetailRow("Nagad", user!!.nagadNumber, Icons.Outlined.Phone)
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                ProfileDetailRow("Rocket", user!!.rocketNumber, Icons.Outlined.Phone)
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                ProfileDetailRow("Password", user!!.passwordHash, Icons.Outlined.Badge) // Showing it because admin dashboard
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                ProfileDetailRow("Secret PIN", user!!.withdrawPin, Icons.Outlined.Badge)
                if (user!!.accountType == "Cashier") {
                    HorizontalDivider(color = BorderLight, thickness = 1.dp)
                    ProfileDetailRow("EPOS Code", user!!.eposCode, Icons.Outlined.Badge)
                }
            }
            
            // Transactions
            Text("Transaction History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 8.dp))
            if (transactions.isEmpty()) {
                Text("No transactions found.", modifier = Modifier.padding(16.dp))
            } else {
                transactions.forEach { tx ->
                    TransactionItemView(tx)
                }
            }
        }
    }
}

@Composable
fun TransactionItemView(transaction: com.example.data.Transaction) {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(transaction.timestamp))
    
    val isPositive = transaction.type.startsWith("Add Money") || transaction.type.startsWith("Deposit") || transaction.type.startsWith("Gas Fee Recharge") || transaction.type.startsWith("Main Balance Recharge")
    val color = if (isPositive) AvailableText else HoldText
    val prefix = if (isPositive) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.type, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(dateString, color = TextGray, fontSize = 12.sp)
            Text("TrxID: ${transaction.trxId}", color = TextGray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Previous Balance: \$${transaction.previousBalance}", color = TextGray, fontSize = 12.sp)
            Text("New Balance: \$${transaction.newBalance}", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("$prefix${transaction.amount}", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
    }
}
