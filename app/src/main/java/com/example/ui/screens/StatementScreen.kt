package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val isAdmin = user?.role == "Admin" || user?.accountType == "Admin"

    val transactions by if (isAdmin) {
        viewModel.allTransactions.collectAsState()
    } else {
        viewModel.currentTransactions.collectAsState()
    }

    val allUsers by viewModel.allUsers.collectAsState()

    var filterRole by remember { mutableStateOf("All Roles") }
    var filterRoleExpanded by remember { mutableStateOf(false) }

    var filterCategory by remember { mutableStateOf("All Categories") }
    var filterCategoryExpanded by remember { mutableStateOf(false) }

    var filterDateRange by remember { mutableStateOf("All Time") }
    var filterDateRangeExpanded by remember { mutableStateOf(false) }

    var expandedTxId by remember { mutableStateOf<Int?>(null) }

    val now = System.currentTimeMillis()
    val filteredTransactions = transactions.filter { tx ->
        val categoryMatch = when (filterCategory) {
            "All Categories" -> true
            "Financial Adjustments" -> tx.type.contains("Adjustment", ignoreCase = true) || tx.type.contains("Commission Release", ignoreCase = true)
            "Penalties" -> tx.type.contains("Penalty", ignoreCase = true)
            "Manual Balance Changes" -> tx.type.contains("Recharge", ignoreCase = true)
            "Deposits" -> tx.type.contains("Deposit", ignoreCase = true) || tx.type.contains("Add Money", ignoreCase = true)
            "Withdrawals" -> tx.type.contains("Withdraw", ignoreCase = true) || tx.type.contains("Out Money", ignoreCase = true)
            else -> true
        }

        val roleMatch = if (!isAdmin || filterRole == "All Roles") {
            true
        } else {
            val txUser = allUsers.find { it.id == tx.userId }
            val userRole = txUser?.role ?: "User"
            val userAccountType = txUser?.accountType ?: "Personal"
            when (filterRole) {
                "User" -> userRole != "Admin" && userAccountType == "Personal"
                "Agent" -> userRole != "Admin" && userAccountType == "Agent"
                "Cashier" -> userRole != "Admin" && userAccountType == "Cashier"
                "Admin" -> userRole == "Admin"
                else -> true
            }
        }

        val dateMatch = when (filterDateRange) {
            "All Time" -> true
            "Today" -> (now - tx.timestamp) <= 24L * 60 * 60 * 1000
            "Last 7 Days" -> (now - tx.timestamp) <= 7L * 24 * 60 * 60 * 1000
            "Last 30 Days" -> (now - tx.timestamp) <= 30L * 24 * 60 * 60 * 1000
            else -> true
        }

        categoryMatch && roleMatch && dateMatch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Filters", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isAdmin) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("User Role:", fontSize = 14.sp)
                            ExposedDropdownMenuBox(
                                expanded = filterRoleExpanded,
                                onExpandedChange = { filterRoleExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = filterRole,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.width(180.dp).menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterRoleExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                )
                                ExposedDropdownMenu(
                                    expanded = filterRoleExpanded,
                                    onDismissRequest = { filterRoleExpanded = false }
                                ) {
                                    listOf("All Roles", "User", "Agent", "Cashier", "Admin").forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role, fontSize = 12.sp) },
                                            onClick = { filterRole = role; filterRoleExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Category:", fontSize = 14.sp)
                        ExposedDropdownMenuBox(
                            expanded = filterCategoryExpanded,
                            onExpandedChange = { filterCategoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = filterCategory,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.width(180.dp).menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterCategoryExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            ExposedDropdownMenu(
                                expanded = filterCategoryExpanded,
                                onDismissRequest = { filterCategoryExpanded = false }
                            ) {
                                listOf("All Categories", "Financial Adjustments", "Penalties", "Manual Balance Changes", "Deposits", "Withdrawals").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontSize = 12.sp) },
                                        onClick = { filterCategory = cat; filterCategoryExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Date Range:", fontSize = 14.sp)
                        ExposedDropdownMenuBox(
                            expanded = filterDateRangeExpanded,
                            onExpandedChange = { filterDateRangeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = filterDateRange,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.width(180.dp).menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterDateRangeExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            ExposedDropdownMenu(
                                expanded = filterDateRangeExpanded,
                                onDismissRequest = { filterDateRangeExpanded = false }
                            ) {
                                listOf("All Time", "Today", "Last 7 Days", "Last 30 Days").forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range, fontSize = 12.sp) },
                                        onClick = { filterDateRange = range; filterDateRangeExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions) { tx ->
                        val isExpanded = expandedTxId == tx.id
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                expandedTxId = if (isExpanded) null else tx.id 
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (isAdmin) {
                                    val txUser = allUsers.find { it.id == tx.userId }
                                    Text(
                                        text = "User: ${txUser?.username ?: "ID ${tx.userId}"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    val isPositive = tx.type.startsWith("Add Money") || tx.type.startsWith("Deposit") || tx.type.startsWith("Gas Fee") || tx.type.startsWith("Main Balance")
                                    Text(
                                        text = tx.type, 
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val prefix = if (isPositive) "+" else "-"
                                    val currencyStr = user?.currency ?: "BDT"
                                    Text(
                                        text = "$prefix $currencyStr ${tx.amount}", 
                                        color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(tx.timestamp))}", style = MaterialTheme.typography.bodySmall)
                                
                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("TrxID: ${tx.trxId}", style = MaterialTheme.typography.bodySmall)
                                    Text("Previous Balance: ${tx.previousBalance}", style = MaterialTheme.typography.bodySmall)
                                    Text("After Balance: ${tx.newBalance}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}
