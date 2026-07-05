package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.ui.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionScreen(
    actionType: String, // "Deposit", "Withdraw", "Sent Money", "Add Money", "Out Money"
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var withdrawCode by remember { mutableStateOf("") }
    var secretCode by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    
    // Deposit specific fields
    var bettingSite by remember { mutableStateOf("1xbet") }
    var bettingSiteExpanded by remember { mutableStateOf(false) }
    var bettingUserId by remember { mutableStateOf("") }
    var balanceType by remember { mutableStateOf("Select Balance Type") }
    var balanceTypeExpanded by remember { mutableStateOf(false) }
    
    var receiveUsername by remember { mutableStateOf("") }
    var receiveVoucher by remember { mutableStateOf("") }
    var acceptVoucherDialog by remember { mutableStateOf<com.example.data.Voucher?>(null) }
    
    var paymentMethod by remember { mutableStateOf("bKash") }
    var paymentMethodExpanded by remember { mutableStateOf(false) }
    
    val user by viewModel.currentUser.collectAsState()
    val bettingSites by viewModel.bettingSites.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(user) {
        if (user != null && (actionType == "Deposit" || actionType == "Withdraw")) {
            if (bettingUserId.isEmpty()) bettingUserId = user!!.lastBettingUserId
            if (bettingSite == "1xbet" && user!!.lastBettingSite.isNotEmpty()) bettingSite = user!!.lastBettingSite
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(actionType) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val okExPayUrl by viewModel.okExPayUrl.collectAsState()
            val okExPayOutTradeNo by viewModel.okExPayOutTradeNo.collectAsState()
            val okExPayAmount by viewModel.okExPayAmount.collectAsState()
            val okExPayStatus by viewModel.okExPayStatus.collectAsState()

            if (actionType == "Add Money" && okExPayUrl != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "OKExPay Gateway Checkout",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Order ID:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(okExPayOutTradeNo ?: "", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Amount:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${user?.currency ?: "BDT"} ${okExPayAmount ?: 0.0}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Status:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(okExPayStatus ?: "Pending Payment", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(okExPayUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Payment Page")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.queryOkExPayStatus { success, statusMsg ->
                                        message = statusMsg
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Verify")
                            }
                            
                            Button(
                                onClick = {
                                    viewModel.forceCompleteOkExPayPayment { success, statusMsg ->
                                        message = statusMsg
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Force Success")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                viewModel.cancelOkExPayPayment()
                                message = "Payment cancelled."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel / Go Back")
                        }
                    }
                }
                
                if (message != null) {
                    Text(message!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth())
                }
            } else {
                if (actionType == "Deposit" || actionType == "Sent Money") {
                ExposedDropdownMenuBox(
                    expanded = balanceTypeExpanded,
                    onExpandedChange = { balanceTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = balanceType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Balance Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = balanceTypeExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = balanceTypeExpanded,
                        onDismissRequest = { balanceTypeExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("Main Balance - ${user?.currency} ${user?.mainBalance ?: 0.0}") }, onClick = { balanceType = "Main Balance"; balanceTypeExpanded = false })
                        DropdownMenuItem(text = { Text("Out Balance - ${user?.currency} ${user?.withdrawableBalance ?: 0.0}") }, onClick = { balanceType = "Out Balance"; balanceTypeExpanded = false })
                        DropdownMenuItem(text = { Text("Hold Balance - ${user?.currency} ${user?.holdBalance ?: 0.0}") }, onClick = { balanceType = "Hold Balance"; balanceTypeExpanded = false })
                        DropdownMenuItem(text = { Text("Out Balance Bonus - ${user?.currency} ${user?.bonusBalance ?: 0.0}") }, onClick = { balanceType = "Out Balance Bonus"; balanceTypeExpanded = false })
                        DropdownMenuItem(text = { Text("Refer Balance - ${user?.currency} ${user?.referBalance ?: 0.0}") }, onClick = { balanceType = "Refer Balance"; balanceTypeExpanded = false })
                    }
                }
                
                val selectedBalanceAmount = when (balanceType) {
                    "Main Balance" -> user?.mainBalance ?: 0.0
                    "Out Balance" -> user?.withdrawableBalance ?: 0.0
                    "Hold Balance" -> user?.holdBalance ?: 0.0
                    "Out Balance Bonus" -> user?.bonusBalance ?: 0.0
                    "Refer Balance" -> user?.referBalance ?: 0.0
                    else -> null
                }
                if (selectedBalanceAmount != null) {
                    Text(
                        text = "Available Balance: ${user?.currency ?: "BDT"} $selectedBalanceAmount",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (actionType == "Deposit" || actionType == "Withdraw") {
                ExposedDropdownMenuBox(
                    expanded = bettingSiteExpanded,
                    onExpandedChange = { bettingSiteExpanded = it }
                ) {
                    OutlinedTextField(
                        value = bettingSite,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Betting Site") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bettingSiteExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = bettingSiteExpanded,
                        onDismissRequest = { bettingSiteExpanded = false }
                    ) {
                        bettingSites.map { it.siteName }.distinct().forEach { siteName ->
                            DropdownMenuItem(text = { Text(siteName) }, onClick = { bettingSite = siteName; bettingSiteExpanded = false })
                        }
                        if (bettingSites.isEmpty()) {
                            DropdownMenuItem(text = { Text("1xbet") }, onClick = { bettingSite = "1xbet"; bettingSiteExpanded = false })
                            DropdownMenuItem(text = { Text("melbet") }, onClick = { bettingSite = "melbet"; bettingSiteExpanded = false })
                            DropdownMenuItem(text = { Text("linebet") }, onClick = { bettingSite = "linebet"; bettingSiteExpanded = false })
                        }
                    }
                }
                
                OutlinedTextField(
                    value = bettingUserId,
                    onValueChange = { bettingUserId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (actionType == "Withdraw") {
                Text(
                    text = "Limit: ${user?.currency} ${user?.withdrawLimit ?: 0.0}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (actionType == "Out Money") {
                Text(
                    text = "Available Out Balance: ${user?.currency} ${user?.withdrawableBalance ?: 0.0}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (actionType == "Add Money" || actionType == "Out Money") {
                ExposedDropdownMenuBox(
                    expanded = paymentMethodExpanded,
                    onExpandedChange = { paymentMethodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentMethodExpanded,
                        onDismissRequest = { paymentMethodExpanded = false }
                    ) {
                        val availableMethods = if (paymentMethods.isNotEmpty()) paymentMethods.map { it.name }.distinct() else listOf("bKash", "Nagad", "Rocket")
                        availableMethods.forEach { method ->
                            DropdownMenuItem(text = { Text(method) }, onClick = { paymentMethod = method; paymentMethodExpanded = false })
                        }
                    }
                }
                
                var subMethod by remember { mutableStateOf("Sent Money") }
                var subMethodExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = subMethodExpanded,
                    onExpandedChange = { subMethodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sub Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subMethodExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subMethodExpanded,
                        onDismissRequest = { subMethodExpanded = false }
                    ) {
                        listOf("Sent Money", "Cashout", "Auto Payment", "Number Payment", "Bangla QR", "NPSB").forEach { method ->
                            DropdownMenuItem(text = { Text(method) }, onClick = { subMethod = method; subMethodExpanded = false })
                        }
                    }
                }
                
                if (actionType == "Add Money") {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (subMethod == "Bangla QR") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color.White),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(Icons.Filled.QrCode2, contentDescription = "QR Code", modifier = Modifier.size(120.dp), tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "Send your funds to Admin's $paymentMethod Number:\n01711111111",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (actionType == "Sent Money") {
                var receiverName by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(receiveUsername) {
                    if (receiveUsername.isNotBlank()) {
                        val receiverUser = viewModel.getUserByIdentifier(receiveUsername)
                        receiverName = receiverUser?.name
                    } else {
                        receiverName = null
                    }
                }
                OutlinedTextField(
                    value = receiveUsername,
                    onValueChange = { receiveUsername = it },
                    label = { Text("Receiver (Email/Username/ID/Number)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (receiverName != null) {
                    Text(
                        text = "Receiver Name: $receiverName",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (actionType == "Receive Money") {
                OutlinedTextField(
                    value = receiveUsername,
                    onValueChange = { receiveUsername = it },
                    label = { Text("Sender Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = receiveVoucher,
                    onValueChange = { receiveVoucher = it },
                    label = { Text("Voucher Code") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (actionType == "Withdraw") {
                var withdrawAddressExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = withdrawAddressExpanded,
                    onExpandedChange = { withdrawAddressExpanded = it }
                ) {
                    OutlinedTextField(
                        value = withdrawCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Withdraw Address") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = withdrawAddressExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = withdrawAddressExpanded,
                        onDismissRequest = { withdrawAddressExpanded = false }
                    ) {
                        val filteredSites = bettingSites.filter { it.siteName == bettingSite }
                        filteredSites.forEach { site ->
                            DropdownMenuItem(
                                text = { Text("${site.siteName} - ${site.withdrawAddress}") },
                                onClick = { 
                                    withdrawCode = site.withdrawAddress
                                    withdrawAddressExpanded = false 
                                }
                            )
                        }
                        if (filteredSites.isEmpty()) {
                            DropdownMenuItem(text = { Text("No addresses found") }, onClick = { withdrawAddressExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = secretCode,
                    onValueChange = { secretCode = it },
                    label = { Text("Secret Code") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("Secret PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )

            if (message != null) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(message!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    if (message!!.contains("Voucher Code:")) {
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        IconButton(onClick = {
                            val code = message!!.substringAfter("Voucher Code:").trim()
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code))
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Voucher")
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (user?.isBlocked == true) {
                        message = "Account is blocked!"
                        return@Button
                    }
                    if (user?.isHold == true && actionType == "Withdraw") {
                        message = "Account is on hold! Withdrawals disabled."
                        return@Button
                    }
                    
                    if (actionType == "Receive Money") {
                        if (receiveUsername.isEmpty() || receiveVoucher.isEmpty()) {
                            message = "Please enter username and voucher code"
                            return@Button
                        }
                        if (user?.withdrawPin != pin) {
                            message = "Invalid PIN"
                            return@Button
                        }
                        viewModel.receiveVoucher(receiveVoucher, receiveUsername) { resultMsg ->
                            message = resultMsg
                            if (resultMsg.contains("successful", ignoreCase = true)) {
                                receiveUsername = ""
                                receiveVoucher = ""
                                pin = ""
                            }
                        }
                        return@Button
                    }

                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal != null && amountVal > 0) {
                        if (user?.withdrawPin == pin) {
                            var normalizedType = actionType
                            if (actionType == "Cashier Deposit") normalizedType = "Deposit"
                            if (actionType == "Cashier Withdraw") normalizedType = "Withdraw"
                            if (actionType == "Customer Add Money") normalizedType = "Add Money"
                            if (actionType == "Customer Cash Out") normalizedType = "Out Money"
                            if (actionType == "B2B Sent Money") normalizedType = "Sent Money"

                            if ((normalizedType == "Deposit" || normalizedType == "Sent Money") && balanceType == "Select Balance Type") {
                                message = "Please select a balance type!"
                                return@Button
                            }
                            var hasEnoughBalance = true
                            var limitError = false
                            if (normalizedType == "Withdraw") {
                                if (user?.ignoreWithdrawLimit == false && amountVal > (user?.withdrawLimit ?: 0.0)) limitError = true
                            } else if (normalizedType == "Out Money") {
                                if (amountVal > (user?.withdrawableBalance ?: 0.0)) hasEnoughBalance = false
                            } else if (normalizedType == "Deposit" || normalizedType == "Sent Money") {
                                val currentBalance = when (balanceType) {
                                    "Main Balance" -> user?.mainBalance ?: 0.0
                                    "Out Balance" -> user?.withdrawableBalance ?: 0.0
                                    "Hold Balance" -> user?.holdBalance ?: 0.0
                                    "Out Balance Bonus" -> user?.bonusBalance ?: 0.0
                                    "Refer Balance" -> user?.referBalance ?: 0.0
                                    else -> 0.0
                                }
                                if (amountVal > currentBalance) hasEnoughBalance = false
                            }

                            if (limitError) {
                                message = "Amount exceeds withdraw limit!"
                            } else if (!hasEnoughBalance) {
                                message = "Insufficient balance!"
                            } else if (normalizedType == "Withdraw" && secretCode.isEmpty()) {
                                message = "Secret Code is required!"
                            } else if (normalizedType == "Add Money") {
                                viewModel.initiateOkExPayAddMoney(
                                    amount = amountVal,
                                    paymentMethod = paymentMethod,
                                    onResult = { url, errorMsg ->
                                        if (errorMsg != null) {
                                            message = errorMsg
                                        } else {
                                            message = "Payment URL generated! Please complete the payment on the opened page."
                                            amount = ""
                                            pin = ""
                                        }
                                    }
                                )
                            } else {
                                val details = if (normalizedType == "Deposit") "$bettingSite - $bettingUserId from $balanceType" 
                                              else if (normalizedType == "Withdraw") "$bettingSite - $bettingUserId (Addr: $withdrawCode, Secret: $secretCode)"
                                              else if (normalizedType == "Sent Money") "To: $receiveUsername"
                                              else null
                                val method = if (normalizedType == "Add Money" || normalizedType == "Out Money") paymentMethod else null
                                viewModel.performTransaction(
                                    type = actionType, 
                                    amount = amountVal, 
                                    details = details,
                                    balanceType = balanceType,
                                    paymentMethod = method,
                                    bettingSite = if (normalizedType == "Deposit" || normalizedType == "Withdraw") bettingSite else null,
                                    bettingUserId = if (normalizedType == "Deposit" || normalizedType == "Withdraw") bettingUserId else null,
                                    receiverUsername = if (normalizedType == "Sent Money") receiveUsername else null,
                                    withdrawAddress = if (normalizedType == "Withdraw") withdrawCode else null,
                                    onResult = { errorMsg ->
                                        if (errorMsg != null) {
                                            message = errorMsg
                                        } else {
                                            message = "$actionType successful!"
                                            amount = ""
                                            pin = ""
                                            withdrawCode = ""
                                        }
                                    }
                                )
                            }
                        } else {
                            message = "Invalid PIN"
                        }
                    } else {
                        message = "Invalid amount"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm $actionType")
            }

            if (actionType == "Sent Money") {
                val vouchers by viewModel.currentVouchers.collectAsState()
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sent Money History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (vouchers.isEmpty()) {
                    Text("No sent money history found.")
                } else {
                    vouchers.forEach { voucher ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val status = when {
                                    voucher.isCancelled -> "Cancelled"
                                    voucher.isRedeemed -> "Redeemed"
                                    voucher.expiresAt < System.currentTimeMillis() -> "Expired"
                                    else -> "Pending"
                                }
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Voucher Code: ${voucher.code}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (status == "Pending") {
                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                        IconButton(onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(voucher.code))
                                        }) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Voucher")
                                        }
                                    }
                                }
                                Text("Amount: ${user?.currency ?: "BDT"} ${voucher.amount} (${voucher.balanceType})")
                                
                                if (voucher.designatedReceiver != null) {
                                    Text("To: ${voucher.designatedReceiver}")
                                } else {
                                    Text("To: Anyone")
                                }
                                
                                val statusColor = when (status) {
                                    "Pending" -> Color(0xFFFFA000) // Amber
                                    "Redeemed" -> Color(0xFF4CAF50) // Green
                                    else -> Color(0xFFF44336) // Red
                                }
                                Text("Status: $status", color = statusColor, fontWeight = FontWeight.Bold)
                                
                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                Text("Created At: ${sdf.format(java.util.Date(voucher.timestamp))}", style = MaterialTheme.typography.bodySmall)
                                
                                if (status == "Pending" || status == "Expired") {
                                    if (status == "Pending") {
                                        val remainingMillis = voucher.expiresAt - System.currentTimeMillis()
                                        val remainingHours = remainingMillis / (1000 * 60 * 60)
                                        val remainingMinutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)
                                        Text("Auto-cancels in: ${remainingHours}h ${remainingMinutes}m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(onClick = {
                                        viewModel.cancelVoucher(voucher) { msg ->
                                            message = msg
                                        }
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Cancel & Refund", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (actionType == "Receive Money") {
                val receivedVouchers by viewModel.receivedVouchers.collectAsState()
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Received Money History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (receivedVouchers.isEmpty()) {
                    Text("No received money history found.")
                } else {
                    receivedVouchers.forEach { voucher ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val status = when {
                                    voucher.isCancelled -> "Cancelled"
                                    voucher.isRedeemed -> "Redeemed"
                                    voucher.expiresAt < System.currentTimeMillis() -> "Expired"
                                    else -> "Pending"
                                }
                                
                                val statusColor = when (status) {
                                    "Pending" -> Color(0xFFFFA000) // Amber
                                    "Redeemed" -> Color(0xFF4CAF50) // Green
                                    else -> Color(0xFFF44336) // Red
                                }

                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Voucher Code: ${if (status == "Redeemed") voucher.code else "********"}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (status == "Redeemed") {
                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                        IconButton(onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(voucher.code))
                                        }) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Voucher")
                                        }
                                    }
                                }
                                Text("Amount: ${user?.currency ?: "BDT"} ${voucher.amount} (${voucher.balanceType})")
                                
                                val creatorUser by viewModel.getUserById(voucher.creatorId).collectAsState(initial = null)
                                Text("From: ${if (status == "Redeemed") creatorUser?.username ?: "User ${voucher.creatorId}" else "********"}")
                                
                                Text("Status: $status", color = statusColor, fontWeight = FontWeight.Bold)
                                
                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                Text("Created At: ${sdf.format(java.util.Date(voucher.timestamp))}", style = MaterialTheme.typography.bodySmall)

                                if (status == "Pending" || status == "Expired") {
                                    if (status == "Pending") {
                                        val remainingMillis = voucher.expiresAt - System.currentTimeMillis()
                                        val remainingHours = remainingMillis / (1000 * 60 * 60)
                                        val remainingMinutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)
                                        Text("Auto-cancels in: ${remainingHours}h ${remainingMinutes}m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = {
                                            viewModel.cancelVoucher(voucher) { msg ->
                                                message = msg
                                            }
                                        }, modifier = Modifier.weight(1f)) {
                                            Text("Reject", color = MaterialTheme.colorScheme.error)
                                        }
                                        if (status == "Pending") {
                                            Button(onClick = {
                                                acceptVoucherDialog = voucher
                                            }, modifier = Modifier.weight(1f)) {
                                                Text("Accept")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (acceptVoucherDialog != null) {
        var inputSenderUsername by remember { mutableStateOf("") }
        var inputVoucherCode by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { acceptVoucherDialog = null },
            title = { Text("Accept Received Money") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please enter the sender's username and the voucher code to claim this money.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = inputSenderUsername,
                        onValueChange = { inputSenderUsername = it },
                        label = { Text("Sender Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputVoucherCode,
                        onValueChange = { inputVoucherCode = it },
                        label = { Text("Voucher Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.receiveVoucher(inputVoucherCode, inputSenderUsername) { msg ->
                            message = msg
                            if (msg.contains("successful", ignoreCase = true) || msg.contains("Redeemed", ignoreCase = true)) {
                                acceptVoucherDialog = null
                            } else if (!msg.contains("Invalid", ignoreCase = true)) {
                                acceptVoucherDialog = null
                            }
                        }
                    },
                    enabled = inputSenderUsername.isNotBlank() && inputVoucherCode.isNotBlank()
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { acceptVoucherDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
