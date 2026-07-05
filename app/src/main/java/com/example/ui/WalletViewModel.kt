package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Transaction
import com.example.data.User
import com.example.data.WalletRepository
import com.example.data.SupportTicket
import com.example.data.SupportMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class WalletStats(
    val todayAddMoney: Double = 0.0,
    val todayOutMoney: Double = 0.0,
    val todayDeposit: Double = 0.0,
    val todayWithdraw: Double = 0.0,
    val todaySentMoney: Double = 0.0,
    val lifeAddMoney: Double = 0.0,
    val lifeOutMoney: Double = 0.0,
    val lifeDeposit: Double = 0.0,
    val lifeWithdraw: Double = 0.0,
    val lifeSentMoney: Double = 0.0,
    val totalProfitLoss: Double = 0.0
)

class WalletViewModel(val repository: WalletRepository) : ViewModel() {

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    val currentUser: StateFlow<User?> = _currentUserId.flatMapLatest { id ->
        if (id != null) {
            repository.getUserByIdFlow(id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentTransactions: StateFlow<List<Transaction>> = _currentUserId.flatMapLatest { id ->
        if (id != null) {
            repository.getTransactionsForUser(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentVouchers: StateFlow<List<com.example.data.Voucher>> = _currentUserId.flatMapLatest { id ->
        if (id != null) {
            repository.getVouchersByCreatorFlow(id).onEach { vouchers ->
                vouchers.forEach { voucher ->
                    if (!voucher.isCancelled && !voucher.isRedeemed && voucher.expiresAt < System.currentTimeMillis()) {
                        cancelVoucher(voucher) {}
                    }
                }
            }
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receivedVouchers: StateFlow<List<com.example.data.Voucher>> = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getReceivedVouchersFlow(user.id, user.username).onEach { vouchers ->
                vouchers.forEach { voucher ->
                    if (!voucher.isCancelled && !voucher.isRedeemed && voucher.expiresAt < System.currentTimeMillis()) {
                        cancelVoucher(voucher) {}
                    }
                }
            }
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val adminUser = repository.getUserByUsername("admin")
            if (adminUser == null) {
                repository.insertUser(User(
                    username = "admin",
                    passwordHash = "admin",
                    accountType = "Admin",
                    currency = "BDT",
                    name = "Admin",
                    email = "admin@example.com",
                    number = "0000000000",
                    withdrawPin = "1234",
                    role = "Admin",
                    mainBalance = 0.0,
                    gasFeeBalance = 0.0
                ))
            } else if (adminUser.currency != "BDT") {
                repository.updateUser(adminUser.copy(currency = "BDT"))
            }
        }
    }

    val walletStats: StateFlow<WalletStats> = currentTransactions.map { transactions ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        var tAdd = 0.0; var tOut = 0.0; var tDep = 0.0; var tWith = 0.0; var tSent = 0.0
        var lAdd = 0.0; var lOut = 0.0; var lDep = 0.0; var lWith = 0.0; var lSent = 0.0
        
        for (tx in transactions) {
            val isToday = tx.timestamp >= startOfToday
            if (tx.type.startsWith("Add Money")) { lAdd += tx.amount; if (isToday) tAdd += tx.amount }
            else if (tx.type.startsWith("Out Money")) { lOut += tx.amount; if (isToday) tOut += tx.amount }
            else if (tx.type.startsWith("Deposit")) { lDep += tx.amount; if (isToday) tDep += tx.amount }
            else if (tx.type.startsWith("Withdraw")) { lWith += tx.amount; if (isToday) tWith += tx.amount }
            else if (tx.type.startsWith("Sent Money")) { lSent += tx.amount; if (isToday) tSent += tx.amount }
        }
        val totalProfitLoss = lWith - lDep
        WalletStats(
            todayAddMoney = tAdd, todayOutMoney = tOut, todayDeposit = tDep, todayWithdraw = tWith, todaySentMoney = tSent,
            lifeAddMoney = lAdd, lifeOutMoney = lOut, lifeDeposit = lDep, lifeWithdraw = lWith, lifeSentMoney = lSent,
            totalProfitLoss = totalProfitLoss
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletStats())

    val allUsers: StateFlow<List<User>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bettingSites: StateFlow<List<com.example.data.BettingSite>> = repository.getAllBettingSitesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<com.example.data.PaymentMethodConfig>> = repository.getAllPaymentMethodsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConfigs: StateFlow<List<com.example.data.AppConfig>> = repository.getAllConfigsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOffers: StateFlow<List<com.example.data.Offer>> = repository.getAllOffersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<SupportTicket>> = repository.getAllTicketsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateConfig(key: String, value: String) {
        viewModelScope.launch {
            repository.insertConfig(com.example.data.AppConfig(key = key, value = value))
        }
    }

    fun updateGlobalCommission(percent: Double) {
        viewModelScope.launch {
            repository.insertConfig(com.example.data.AppConfig(key = "global_commission_percent", value = percent.toString()))
            val agents = allUsers.value.filter { it.accountType.equals("Agent", ignoreCase = true) || it.accountType.equals("Cashier", ignoreCase = true) }
            agents.forEach { agent ->
                repository.updateUser(agent.copy(agentCommissionPercent = percent))
            }
        }
    }

    fun updateGlobalDpCommission(percent: Double) {
        viewModelScope.launch {
            repository.insertConfig(com.example.data.AppConfig(key = "global_dp_commission_percent", value = percent.toString()))
            val agents = allUsers.value.filter { it.accountType.equals("Agent", ignoreCase = true) || it.accountType.equals("Cashier", ignoreCase = true) }
            agents.forEach { agent ->
                repository.updateUser(agent.copy(agentDpCommissionPercent = percent))
            }
        }
    }

    fun updateGlobalWdCommission(percent: Double) {
        viewModelScope.launch {
            repository.insertConfig(com.example.data.AppConfig(key = "global_wd_commission_percent", value = percent.toString()))
            val agents = allUsers.value.filter { it.accountType.equals("Agent", ignoreCase = true) || it.accountType.equals("Cashier", ignoreCase = true) }
            agents.forEach { agent ->
                repository.updateUser(agent.copy(agentWdCommissionPercent = percent))
            }
        }
    }

    // Actions
    fun login(userId: Int) {
        _currentUserId.value = userId
    }

    fun logout() {
        _currentUserId.value = null
    }

    fun rechargeGasFee(amount: Double) {
        val admin = currentUser.value ?: return
        if (admin.role != "Admin") return
        viewModelScope.launch {
            val updatedAdmin = admin.copy(
                gasFeeBalance = admin.gasFeeBalance + (amount * 100)
            )
            repository.updateUser(updatedAdmin)

            val transaction = Transaction(
                userId = admin.id,
                type = "Gas Fee Recharge",
                amount = amount,
                trxId = "GAS${System.currentTimeMillis()}",
                previousBalance = admin.gasFeeBalance,
                newBalance = updatedAdmin.gasFeeBalance
            )
            repository.insertTransaction(transaction)
        }
    }

    fun rechargeMainBalance(amount: Double) {
        val admin = currentUser.value ?: return
        if (admin.role != "Admin") return
        viewModelScope.launch {
            val updatedAdmin = admin.copy(
                mainBalance = admin.mainBalance + amount
            )
            repository.updateUser(updatedAdmin)

            val transaction = Transaction(
                userId = admin.id,
                type = "Main Balance Recharge",
                amount = amount,
                trxId = "MAIN${System.currentTimeMillis()}",
                previousBalance = admin.mainBalance,
                newBalance = updatedAdmin.mainBalance
            )
            repository.insertTransaction(transaction)
        }
    }

    fun adjustUserBalance(userId: Int, adminId: Int, amount: Double, balanceType: String, isAdd: Boolean, onResult: (String?) -> Unit = {}) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            if (user == null) {
                onResult("User not found!")
                return@launch
            }
            val admin = repository.getUserById(adminId)
            if (admin == null || admin.role != "Admin") {
                onResult("Admin not found or invalid privileges!")
                return@launch
            }

            if (amount <= 0.0) {
                onResult("Amount must be greater than zero!")
                return@launch
            }

            val previousUserBalance = when (balanceType) {
                "Main Balance" -> user.mainBalance
                "Hold Balance" -> user.holdBalance
                "Withdrawable Balance" -> user.withdrawableBalance
                "Refer Balance" -> user.referBalance
                "Bonus Balance" -> user.bonusBalance
                "Commission Balance" -> user.commissionBalance
                "Gas Fee Balance" -> user.gasFeeBalance
                "Withdraw Limit" -> user.withdrawLimit
                else -> user.mainBalance
            }

            if (isAdd) {
                if (admin.mainBalance < amount) {
                    onResult("Insufficient Admin main balance!")
                    return@launch
                }

                val newUserBalance = previousUserBalance + amount
                val updatedUser = when (balanceType) {
                    "Main Balance" -> user.copy(mainBalance = newUserBalance)
                    "Hold Balance" -> user.copy(holdBalance = newUserBalance)
                    "Withdrawable Balance" -> user.copy(withdrawableBalance = newUserBalance)
                    "Refer Balance" -> user.copy(referBalance = newUserBalance)
                    "Bonus Balance" -> user.copy(bonusBalance = newUserBalance)
                    "Commission Balance" -> user.copy(commissionBalance = newUserBalance)
                    "Gas Fee Balance" -> user.copy(gasFeeBalance = newUserBalance)
                    "Withdraw Limit" -> user.copy(withdrawLimit = newUserBalance)
                    else -> user.copy(mainBalance = newUserBalance)
                }

                val updatedAdmin = admin.copy(mainBalance = admin.mainBalance - amount)

                repository.updateUser(updatedAdmin)
                repository.updateUser(updatedUser)

                val trxId = "ADJ${System.currentTimeMillis()}"

                // Create transaction for user
                val userTransaction = Transaction(
                    userId = user.id,
                    type = "Admin Adjustment",
                    amount = amount,
                    trxId = trxId,
                    previousBalance = previousUserBalance,
                    newBalance = newUserBalance
                )
                repository.insertTransaction(userTransaction)

                // Create transaction for admin
                val adminTransaction = Transaction(
                    userId = admin.id,
                    type = "Admin Adjustment ($balanceType) to @${user.username}",
                    amount = amount,
                    trxId = trxId,
                    previousBalance = admin.mainBalance,
                    newBalance = updatedAdmin.mainBalance
                )
                repository.insertTransaction(adminTransaction)

                onResult(null)
            } else {
                if (previousUserBalance < amount) {
                    onResult("Insufficient user $balanceType to deduct!")
                    return@launch
                }

                val newUserBalance = previousUserBalance - amount
                val updatedUser = when (balanceType) {
                    "Main Balance" -> user.copy(mainBalance = newUserBalance)
                    "Hold Balance" -> user.copy(holdBalance = newUserBalance)
                    "Withdrawable Balance" -> user.copy(withdrawableBalance = newUserBalance)
                    "Refer Balance" -> user.copy(referBalance = newUserBalance)
                    "Bonus Balance" -> user.copy(bonusBalance = newUserBalance)
                    "Commission Balance" -> user.copy(commissionBalance = newUserBalance)
                    "Gas Fee Balance" -> user.copy(gasFeeBalance = newUserBalance)
                    "Withdraw Limit" -> user.copy(withdrawLimit = newUserBalance)
                    else -> user.copy(mainBalance = newUserBalance)
                }

                val updatedAdmin = admin.copy(mainBalance = admin.mainBalance + amount)

                repository.updateUser(updatedAdmin)
                repository.updateUser(updatedUser)

                val trxId = "ADJ${System.currentTimeMillis()}"

                // Create transaction for user
                val userTransaction = Transaction(
                    userId = user.id,
                    type = "Admin Adjustment",
                    amount = amount,
                    trxId = trxId,
                    previousBalance = previousUserBalance,
                    newBalance = newUserBalance
                )
                repository.insertTransaction(userTransaction)

                // Create transaction for admin
                val adminTransaction = Transaction(
                    userId = admin.id,
                    type = "Admin Adjustment ($balanceType) from @${user.username}",
                    amount = amount,
                    trxId = trxId,
                    previousBalance = admin.mainBalance,
                    newBalance = updatedAdmin.mainBalance
                )
                repository.insertTransaction(adminTransaction)

                onResult(null)
            }
        }
    }

    fun getUserById(userId: Int): kotlinx.coroutines.flow.Flow<User?> {
        return repository.getUserByIdFlow(userId)
    }

    suspend fun getUserByIdentifier(identifier: String): User? {
        return repository.getUserByIdentifier(identifier)
    }

    fun getTransactionsForUser(userId: Int): kotlinx.coroutines.flow.Flow<List<Transaction>> {
        return repository.getTransactionsForUser(userId)
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun bulkUpdateUsers(users: List<User>) {
        viewModelScope.launch {
            users.forEach { user ->
                repository.updateUser(user)
            }
        }
    }

    fun performTransaction(
        type: String, 
        amount: Double, 
        details: String? = null,
        balanceType: String? = null,
        paymentMethod: String? = null,
        bettingSite: String? = null,
        bettingUserId: String? = null,
        receiverUsername: String? = null,
        withdrawAddress: String? = null,
        onResult: (String?) -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        
        // Normalize Cashier action types
        var normalizedType = type
        if (type == "Cashier Deposit") normalizedType = "Deposit"
        if (type == "Cashier Withdraw") normalizedType = "Withdraw"
        if (type == "Customer Add Money") normalizedType = "Add Money"
        if (type == "Customer Cash Out") normalizedType = "Out Money"
        if (type == "B2B Sent Money") normalizedType = "Sent Money"

        viewModelScope.launch {
            var actualReceiverIdentifier: String? = null
            if (normalizedType == "Sent Money" && !receiverUsername.isNullOrBlank()) {
                val receiver = repository.getUserByIdentifier(receiverUsername)
                if (receiver == null) {
                    onResult("Receiver not found!")
                    return@launch
                }
                actualReceiverIdentifier = receiver.username
            }
            
            val admin = repository.getUserByUsername("admin")
            if (admin != null && (normalizedType == "Deposit" || normalizedType == "Withdraw")) {
                val gasFeeRequired = amount * 0.01 // Assuming 1% gas fee
                if (admin.gasFeeBalance < gasFeeRequired) {
                    onResult("Error 501: Emergency contact admin in support to solve this issue (Insufficient Gas Fee).")
                    return@launch
                }
                // Deduct gas fee from admin
                repository.updateUser(admin.copy(gasFeeBalance = admin.gasFeeBalance - gasFeeRequired))
            }

            var newMainBalance = user.mainBalance
            var newWithdrawableBalance = user.withdrawableBalance
            var newHoldBalance = user.holdBalance
            var newBonusBalance = user.bonusBalance
            var newReferBalance = user.referBalance
            var newCommissionBalance = user.commissionBalance
            var newLimit = user.withdrawLimit

            if (normalizedType == "Deposit") {
                if (balanceType == "Main Balance") newMainBalance -= amount
                if (balanceType == "Out Balance") newWithdrawableBalance -= amount
                if (balanceType == "Hold Balance") newHoldBalance -= amount
                if (balanceType == "Out Balance Bonus") newBonusBalance -= amount
                if (balanceType == "Refer Balance") newReferBalance -= amount
                
                if (user.accountType.equals("Agent", ignoreCase = true) || user.accountType.equals("Cashier", ignoreCase = true)) {
                    val rate = if (user.agentDpCommissionPercent >= 0.0) user.agentDpCommissionPercent else user.agentCommissionPercent
                    val commissionAmount = amount * (rate / 100.0)
                    newCommissionBalance += commissionAmount
                } else if (balanceType == "Out Balance") {
                    val bonusAmount = amount * (user.bonusPercent / 100.0)
                    newBonusBalance += bonusAmount
                }
                
                newLimit += amount
            } else if (normalizedType == "Withdraw") {
                newWithdrawableBalance += amount
                newLimit -= amount
                
                if (user.accountType.equals("Agent", ignoreCase = true) || user.accountType.equals("Cashier", ignoreCase = true)) {
                    val rate = if (user.agentWdCommissionPercent >= 0.0) user.agentWdCommissionPercent else user.agentCommissionPercent
                    val commissionAmount = amount * (rate / 100.0)
                    newCommissionBalance += commissionAmount
                }
            } else if (normalizedType == "Add Money") {
                newMainBalance += amount
            } else if (normalizedType == "Out Money") {
                newWithdrawableBalance -= amount
            } else if (normalizedType == "Sent Money") {
                when (balanceType) {
                    "Main Balance" -> newMainBalance -= amount
                    "Out Balance" -> newWithdrawableBalance -= amount
                    "Hold Balance" -> newHoldBalance -= amount
                    "Out Balance Bonus" -> newBonusBalance -= amount
                    "Refer Balance" -> newReferBalance -= amount
                    else -> newMainBalance -= amount
                }
            }

            if (normalizedType == "Withdraw" && withdrawAddress != null) {
                // Find betting site by withdraw address
                val site = bettingSites.value.find { it.withdrawAddress == withdrawAddress }
                if (site != null) {
                    val updatedSite = site.copy(balance = site.balance + amount)
                    repository.updateBettingSite(updatedSite)
                }
            }

            val updatedUser = user.copy(
                mainBalance = newMainBalance,
                withdrawableBalance = newWithdrawableBalance,
                holdBalance = newHoldBalance,
                bonusBalance = newBonusBalance,
                referBalance = newReferBalance,
                commissionBalance = newCommissionBalance,
                withdrawLimit = newLimit,
                lastBettingSite = bettingSite ?: user.lastBettingSite,
                lastBettingUserId = bettingUserId ?: user.lastBettingUserId
            )
            repository.updateUser(updatedUser)
            
            var prevBalance = 0.0
            var nBalance = 0.0
            if (normalizedType == "Deposit" || normalizedType == "Sent Money") {
                prevBalance = when (balanceType) {
                    "Main Balance" -> user.mainBalance
                    "Out Balance" -> user.withdrawableBalance
                    "Hold Balance" -> user.holdBalance
                    "Out Balance Bonus" -> user.bonusBalance
                    "Refer Balance" -> user.referBalance
                    else -> 0.0
                }
                nBalance = prevBalance - amount
            } else if (normalizedType == "Withdraw" || normalizedType == "Out Money") {
                prevBalance = user.withdrawableBalance
                nBalance = if (normalizedType == "Withdraw") prevBalance + amount else prevBalance - amount
            } else if (normalizedType == "Add Money") {
                prevBalance = user.mainBalance
                nBalance = prevBalance + amount
            }

            var finalType = type
            if (paymentMethod != null) finalType = "$finalType via $paymentMethod"
            if (details != null) finalType = "$finalType ($details)"

            val trxPrefix = if (user.currency.equals("BDT", ignoreCase = true)) "BDT" else "TRX"
            val trxId = "$trxPrefix${System.currentTimeMillis()}"
            val transaction = Transaction(
                userId = user.id,
                type = finalType,
                amount = amount,
                trxId = trxId,
                previousBalance = prevBalance,
                newBalance = nBalance
            )
            repository.insertTransaction(transaction)

            var resultMsg: String? = null
            if (type == "Sent Money") {
                val voucherCode = "VOUCHER-${System.currentTimeMillis()}"
                repository.insertVoucher(com.example.data.Voucher(
                    code = voucherCode,
                    amount = amount,
                    creatorId = user.id,
                    balanceType = balanceType ?: "Main Balance",
                    designatedReceiver = actualReceiverIdentifier
                ))
                resultMsg = "Sent Money successful! Voucher Code: $voucherCode"
            }

            // Log admin transaction
            if (admin != null && (type == "Deposit" || type == "Withdraw")) {
                val adminTransaction = Transaction(
                    userId = admin.id,
                    type = "User ${user.username} $type",
                    amount = amount,
                    trxId = trxId,
                    previousBalance = 0.0,
                    newBalance = 0.0
                )
                repository.insertTransaction(adminTransaction)
            }
            onResult(resultMsg)
        }
    }

    fun receiveVoucher(voucherCode: String, senderUsername: String, onResult: (String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val sender = repository.getUserByUsername(senderUsername)
            if (sender == null) {
                onResult("Sender username not found")
                return@launch
            }
            val voucher = repository.getVoucherByCode(voucherCode)
            if (voucher == null) {
                onResult("Invalid voucher code")
                return@launch
            }
            if (voucher.creatorId != sender.id) {
                onResult("Voucher was not created by this sender")
                return@launch
            }
            if (voucher.isRedeemed) {
                onResult("Voucher has already been redeemed")
                return@launch
            }
            if (voucher.isCancelled) {
                onResult("Voucher has been cancelled")
                return@launch
            }
            if (voucher.expiresAt < System.currentTimeMillis()) {
                onResult("Voucher has expired")
                return@launch
            }
            if (voucher.designatedReceiver != null && voucher.designatedReceiver != user.username) {
                onResult("This voucher is not designated for you")
                return@launch
            }

            // Redeem voucher
            repository.updateVoucher(voucher.copy(isRedeemed = true, redeemedById = user.id))

            // Add amount to user's main balance
            val prevBalance = user.mainBalance
            val newBalance = prevBalance + voucher.amount
            repository.updateUser(user.copy(mainBalance = newBalance))

            val trxPrefix = if (user.currency.equals("BDT", ignoreCase = true)) "BDT" else "TRX"
            val trxId = "$trxPrefix${System.currentTimeMillis()}"
            val transaction = Transaction(
                userId = user.id,
                type = "Received Money from ${sender.username}",
                amount = voucher.amount,
                trxId = trxId,
                previousBalance = prevBalance,
                newBalance = newBalance
            )
            repository.insertTransaction(transaction)

            onResult("Receive Money successful!")
        }
    }

    fun cancelVoucher(voucher: com.example.data.Voucher, onResult: (String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            if (voucher.creatorId != user.id) {
                onResult("Not authorized to cancel this voucher")
                return@launch
            }
            if (voucher.isRedeemed) {
                onResult("Voucher has already been redeemed")
                return@launch
            }
            if (voucher.isCancelled) {
                onResult("Voucher is already cancelled")
                return@launch
            }

            // Cancel voucher
            repository.updateVoucher(voucher.copy(isCancelled = true))

            // Return balance to creator
            var newMainBalance = user.mainBalance
            var newWithdrawableBalance = user.withdrawableBalance
            var newHoldBalance = user.holdBalance
            var newBonusBalance = user.bonusBalance
            var newReferBalance = user.referBalance
            val prevBalance = when (voucher.balanceType) {
                "Main Balance" -> user.mainBalance
                "Out Balance" -> user.withdrawableBalance
                "Hold Balance" -> user.holdBalance
                "Out Balance Bonus" -> user.bonusBalance
                "Refer Balance" -> user.referBalance
                else -> user.mainBalance
            }

            when (voucher.balanceType) {
                "Main Balance" -> newMainBalance += voucher.amount
                "Out Balance" -> newWithdrawableBalance += voucher.amount
                "Hold Balance" -> newHoldBalance += voucher.amount
                "Out Balance Bonus" -> newBonusBalance += voucher.amount
                "Refer Balance" -> newReferBalance += voucher.amount
                else -> newMainBalance += voucher.amount
            }

            repository.updateUser(user.copy(
                mainBalance = newMainBalance,
                withdrawableBalance = newWithdrawableBalance,
                holdBalance = newHoldBalance,
                bonusBalance = newBonusBalance,
                referBalance = newReferBalance
            ))

            val trxPrefix = if (user.currency.equals("BDT", ignoreCase = true)) "BDT" else "TRX"
            val trxId = "$trxPrefix${System.currentTimeMillis()}"
            val transaction = Transaction(
                userId = user.id,
                type = "Cancelled Sent Money",
                amount = voucher.amount,
                trxId = trxId,
                previousBalance = prevBalance,
                newBalance = prevBalance + voucher.amount
            )
            repository.insertTransaction(transaction)

            onResult("Voucher cancelled and refunded successfully!")
        }
    }

    fun logCommissionRelease(userId: Int, amount: Double) {
        viewModelScope.launch {
            val targetUser = repository.getUserByIdentifier(userId.toString()) ?: return@launch
            val trxPrefix = if (targetUser.currency.equals("BDT", ignoreCase = true)) "BDT" else "TRX"
            val trxId = "$trxPrefix${System.currentTimeMillis()}"
            val transaction = Transaction(
                userId = userId,
                type = "Commission Release",
                amount = amount,
                trxId = trxId,
                previousBalance = targetUser.mainBalance - amount,
                newBalance = targetUser.mainBalance
            )
            repository.insertTransaction(transaction)
        }
    }

    fun globalCommissionRelease() {
        viewModelScope.launch {
            val users = allUsers.value
            users.forEach { user ->
                if (user.accountType.equals("Agent", ignoreCase = true) && user.commissionBalance > 0.0) {
                    val amount = user.commissionBalance
                    val updatedUser = user.copy(
                        commissionBalance = 0.0,
                        mainBalance = user.mainBalance + amount
                    )
                    repository.updateUser(updatedUser)
                    
                    val trxPrefix = if (updatedUser.currency.equals("BDT", ignoreCase = true)) "BDT" else "TRX"
                    val trxId = "$trxPrefix${System.currentTimeMillis()}"
                    val transaction = Transaction(
                        userId = updatedUser.id,
                        type = "Global Commission Release",
                        amount = amount,
                        trxId = trxId,
                        previousBalance = user.mainBalance,
                        newBalance = updatedUser.mainBalance
                    )
                    repository.insertTransaction(transaction)
                }
            }
        }
    }

    fun saveBettingSite(site: com.example.data.BettingSite) {
        viewModelScope.launch { repository.insertBettingSite(site) }
    }

    fun deleteBettingSite(site: com.example.data.BettingSite) {
        viewModelScope.launch { repository.deleteBettingSite(site) }
    }

    fun savePaymentMethod(method: com.example.data.PaymentMethodConfig) {
        viewModelScope.launch { repository.insertPaymentMethod(method) }
    }

    fun deletePaymentMethod(method: com.example.data.PaymentMethodConfig) {
        viewModelScope.launch { repository.deletePaymentMethod(method) }
    }

    fun getTicketsForUser(userId: Int): kotlinx.coroutines.flow.Flow<List<SupportTicket>> =
        repository.getTicketsForUser(userId)

    fun getMessagesForTicket(ticketId: Int): kotlinx.coroutines.flow.Flow<List<SupportMessage>> =
        repository.getMessagesForTicket(ticketId)

    fun getTicketByIdFlow(ticketId: Int): kotlinx.coroutines.flow.Flow<SupportTicket?> =
        repository.getTicketByIdFlow(ticketId)

    fun openTicket(userId: Int, username: String, subject: String, firstMessage: String, imagePath: String? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val ticketId = repository.insertTicket(
                SupportTicket(
                    userId = userId,
                    username = username,
                    subject = subject,
                    status = "Open",
                    createdAt = now,
                    lastUpdated = now
                )
            ).toInt()
            
            repository.insertMessage(
                SupportMessage(
                    ticketId = ticketId,
                    senderId = userId,
                    senderRole = "User",
                    senderName = username,
                    message = firstMessage,
                    timestamp = now,
                    imagePath = imagePath
                )
            )
        }
    }

    fun sendSupportMessage(ticketId: Int, senderId: Int, senderRole: String, senderName: String, messageText: String, imagePath: String? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.insertMessage(
                SupportMessage(
                    ticketId = ticketId,
                    senderId = senderId,
                    senderRole = senderRole,
                    senderName = senderName,
                    message = messageText,
                    timestamp = now,
                    imagePath = imagePath
                )
            )
            
            val ticket = repository.getTicketById(ticketId)
            if (ticket != null) {
                val newStatus = if (senderRole == "Admin") "Pending User Response" else "Pending Admin Response"
                repository.updateTicket(
                    ticket.copy(
                        status = newStatus,
                        lastUpdated = now
                    )
                )
            }
        }
    }

    fun closeTicket(ticketId: Int) {
        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId)
            if (ticket != null) {
                repository.updateTicket(
                    ticket.copy(
                        status = "Closed",
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // OKExPay Add Money integration
    val okExPayUrl = MutableStateFlow<String?>(null)
    val okExPayOutTradeNo = MutableStateFlow<String?>(null)
    val okExPayAmount = MutableStateFlow<Double?>(null)
    val okExPayStatus = MutableStateFlow<String?>(null)

    fun initiateOkExPayAddMoney(
        amount: Double,
        paymentMethod: String,
        onResult: (String?, String?) -> Unit // (url, error)
    ) {
        val user = currentUser.value
        if (user == null) {
            onResult(null, "User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                val outTradeNo = "TRX" + System.currentTimeMillis() + (1000..9999).random()
                val payType = when (paymentMethod.lowercase()) {
                    "bkash" -> "BKASH"
                    "nagad" -> "NAGAD"
                    "rocket" -> "ROCKET"
                    else -> paymentMethod.uppercase()
                }

                val baseUrl = allConfigs.value.find { it.key == "okexpay_base_url" }?.value?.trim() ?: "https://api.wpay.life/"
                val mchId = allConfigs.value.find { it.key == "okexpay_mch_id" }?.value?.trim() ?: "5171"
                val secretKey = allConfigs.value.find { it.key == "okexpay_api_secret" }?.value?.trim() ?: "eb6da0e4cab2d2ea9df343404ac8d2c2"

                val params = mapOf(
                    "mchId" to mchId,
                    "currency" to (if (user.currency.isNotBlank()) user.currency else "BDT"),
                    "out_trade_no" to outTradeNo,
                    "pay_type" to payType,
                    "money" to amount.toInt().toString(),
                    "attach" to user.id.toString(),
                    "notify_url" to "https://www.sandbox.wpay.one/callback/payin",
                    "returnUrl" to "https://www.google.com"
                )

                val sign = com.example.api.OkExPayClient.generateSign(params, secretKey)
                val fullParams = params.toMutableMap().apply {
                    put("sign", sign)
                }

                val apiService = com.example.api.OkExPayClient.getService(baseUrl)
                val response = apiService.collect(fullParams)
                if (response.code == 0 && response.data != null) {
                    val paymentUrl = response.data.url
                    if (paymentUrl != null) {
                        okExPayUrl.value = paymentUrl
                        okExPayOutTradeNo.value = outTradeNo
                        okExPayAmount.value = amount
                        okExPayStatus.value = "Pending Payment"
                        onResult(paymentUrl, null)
                    } else {
                        onResult(null, "API returned empty payment URL")
                    }
                } else {
                    onResult(null, "Error ${response.code}: ${response.msg}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null, "Connection failed: ${e.message}")
            }
        }
    }

    fun queryOkExPayStatus(onResult: (Boolean, String) -> Unit) {
        val outTradeNo = okExPayOutTradeNo.value
        val amount = okExPayAmount.value
        val user = currentUser.value
        if (outTradeNo == null || amount == null || user == null) {
            onResult(false, "No active OKExPay transaction found.")
            return
        }

        viewModelScope.launch {
            try {
                val baseUrl = allConfigs.value.find { it.key == "okexpay_base_url" }?.value?.trim() ?: "https://api.wpay.life/"
                val mchId = allConfigs.value.find { it.key == "okexpay_mch_id" }?.value?.trim() ?: "5171"
                val secretKey = allConfigs.value.find { it.key == "okexpay_api_secret" }?.value?.trim() ?: "eb6da0e4cab2d2ea9df343404ac8d2c2"

                val params = mapOf(
                    "mchId" to mchId,
                    "out_trade_no" to outTradeNo
                )
                val sign = com.example.api.OkExPayClient.generateSign(params, secretKey)
                val fullParams = params.toMutableMap().apply {
                    put("sign", sign)
                }

                val apiService = com.example.api.OkExPayClient.getService(baseUrl)

                // Try queryCollect first, fallback to queryPayout
                var queryResponse: com.example.api.OkExPayQueryResponse? = null
                try {
                    queryResponse = apiService.queryCollect(fullParams)
                } catch (e: Exception) {
                    try {
                        queryResponse = apiService.queryPayout(fullParams)
                    } catch (ex: Exception) {
                        // ignore
                    }
                }

                if (queryResponse != null && queryResponse.code == 0 && queryResponse.data != null) {
                    val status = queryResponse.data.status
                    if (status == 1) {
                        okExPayStatus.value = "Payment Success"
                        // Credit user balance
                        val oldBalance = user.mainBalance
                        val newBalance = oldBalance + amount
                        repository.updateUser(user.copy(mainBalance = newBalance))

                        val trxPrefix = if (user.currency.equals("BDT", ignoreCase = true)) "BDT" else "TRX"
                        val trxId = "$trxPrefix${System.currentTimeMillis()}"
                        val transaction = Transaction(
                            userId = user.id,
                            type = "Add Money",
                            amount = amount,
                            trxId = trxId,
                            previousBalance = oldBalance,
                            newBalance = newBalance
                        )
                        repository.insertTransaction(transaction)

                        // Clear active transaction
                        okExPayUrl.value = null
                        okExPayOutTradeNo.value = null
                        okExPayAmount.value = null
                        okExPayStatus.value = null

                        onResult(true, "Payment successful! Credited ${user.currency} $amount.")
                    } else if (status == 2) {
                        okExPayStatus.value = "Payment Failed"
                        onResult(false, "Payment has been marked as failed by the gateway.")
                    } else {
                        okExPayStatus.value = "Pending Payment"
                        onResult(false, "Payment is still pending on the gateway.")
                    }
                } else {
                    val errorMsg = queryResponse?.msg ?: "Unknown API response"
                    onResult(false, "Gateway Query Error: $errorMsg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Connection error: ${e.message}")
            }
        }
    }

    fun forceCompleteOkExPayPayment(onResult: (Boolean, String) -> Unit) {
        val outTradeNo = okExPayOutTradeNo.value
        val amount = okExPayAmount.value
        val user = currentUser.value
        if (outTradeNo == null || amount == null || user == null) {
            onResult(false, "No active OKExPay transaction found.")
            return
        }

        viewModelScope.launch {
            okExPayStatus.value = "Payment Success"
            // Credit user balance
            val oldBalance = user.mainBalance
            val newBalance = oldBalance + amount
            repository.updateUser(user.copy(mainBalance = newBalance))

            val trxPrefix = if (user.currency.equals("BDT", ignoreCase = true)) "BDT" else "TRX"
            val trxId = "$trxPrefix${System.currentTimeMillis()}"
            val transaction = Transaction(
                userId = user.id,
                type = "Add Money",
                amount = amount,
                trxId = trxId,
                previousBalance = oldBalance,
                newBalance = newBalance
            )
            repository.insertTransaction(transaction)

            // Clear active transaction
            okExPayUrl.value = null
            okExPayOutTradeNo.value = null
            okExPayAmount.value = null
            okExPayStatus.value = null

            onResult(true, "Sandbox Simulation Success! Credited ${user.currency} $amount.")
        }
    }

    fun cancelOkExPayPayment() {
        okExPayUrl.value = null
        okExPayOutTradeNo.value = null
        okExPayAmount.value = null
        okExPayStatus.value = null
    }

    fun addOffer(title: String, description: String, dateCreated: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertOffer(com.example.data.Offer(title = title, description = description, dateCreated = dateCreated))
            onComplete()
        }
    }

    fun deleteOffer(offerId: Int) {
        viewModelScope.launch {
            repository.deleteOfferById(offerId)
        }
    }
}

class WalletViewModelFactory(private val repository: WalletRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
