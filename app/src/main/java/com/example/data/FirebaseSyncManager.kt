package com.example.data

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// --- Entity Mappers to and from Maps ---

fun User.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "username" to username,
    "passwordHash" to passwordHash,
    "accountType" to accountType,
    "currency" to currency,
    "name" to name,
    "email" to email,
    "number" to number,
    "bkashNumber" to bkashNumber,
    "nagadNumber" to nagadNumber,
    "rocketNumber" to rocketNumber,
    "withdrawPin" to withdrawPin,
    "mainBalance" to mainBalance,
    "withdrawLimit" to withdrawLimit,
    "holdBalance" to holdBalance,
    "withdrawableBalance" to withdrawableBalance,
    "referBalance" to referBalance,
    "bonusBalance" to bonusBalance,
    "commissionBalance" to commissionBalance,
    "gasFeeBalance" to gasFeeBalance,
    "bonusPercent" to bonusPercent,
    "agentCommissionPercent" to agentCommissionPercent,
    "agentDpCommissionPercent" to agentDpCommissionPercent,
    "agentWdCommissionPercent" to agentWdCommissionPercent,
    "role" to role,
    "referralCode" to referralCode,
    "referredBy" to referredBy,
    "lastBettingSite" to lastBettingSite,
    "lastBettingUserId" to lastBettingUserId,
    "isBlocked" to isBlocked,
    "isHold" to isHold,
    "ignoreWithdrawLimit" to ignoreWithdrawLimit,
    "canDeposit" to canDeposit,
    "canWithdraw" to canWithdraw,
    "canAddMoney" to canAddMoney,
    "canOutMoney" to canOutMoney,
    "canSendMoney" to canSendMoney,
    "canReceiveMoney" to canReceiveMoney,
    "eposCode" to eposCode,
    "hiddenPenaltyMain" to hiddenPenaltyMain,
    "hiddenPenaltyOut" to hiddenPenaltyOut,
    "hiddenPenaltyCombined" to hiddenPenaltyCombined
)

fun Map<String, Any?>.toUser(): User {
    return User(
        id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Int) ?: 0,
        username = this["username"] as? String ?: "",
        passwordHash = this["passwordHash"] as? String ?: "",
        accountType = this["accountType"] as? String ?: "",
        currency = this["currency"] as? String ?: "",
        name = this["name"] as? String ?: "",
        email = this["email"] as? String ?: "",
        number = this["number"] as? String ?: "",
        bkashNumber = this["bkashNumber"] as? String ?: "",
        nagadNumber = this["nagadNumber"] as? String ?: "",
        rocketNumber = this["rocketNumber"] as? String ?: "",
        withdrawPin = this["withdrawPin"] as? String ?: "",
        mainBalance = (this["mainBalance"] as? Double) ?: ((this["mainBalance"] as? Number)?.toDouble()) ?: 0.0,
        withdrawLimit = (this["withdrawLimit"] as? Double) ?: ((this["withdrawLimit"] as? Number)?.toDouble()) ?: 0.0,
        holdBalance = (this["holdBalance"] as? Double) ?: ((this["holdBalance"] as? Number)?.toDouble()) ?: 0.0,
        withdrawableBalance = (this["withdrawableBalance"] as? Double) ?: ((this["withdrawableBalance"] as? Number)?.toDouble()) ?: 0.0,
        referBalance = (this["referBalance"] as? Double) ?: ((this["referBalance"] as? Number)?.toDouble()) ?: 0.0,
        bonusBalance = (this["bonusBalance"] as? Double) ?: ((this["bonusBalance"] as? Number)?.toDouble()) ?: 0.0,
        commissionBalance = (this["commissionBalance"] as? Double) ?: ((this["commissionBalance"] as? Number)?.toDouble()) ?: 0.0,
        gasFeeBalance = (this["gasFeeBalance"] as? Double) ?: ((this["gasFeeBalance"] as? Number)?.toDouble()) ?: 0.0,
        bonusPercent = (this["bonusPercent"] as? Double) ?: ((this["bonusPercent"] as? Number)?.toDouble()) ?: 2.0,
        agentCommissionPercent = (this["agentCommissionPercent"] as? Double) ?: ((this["agentCommissionPercent"] as? Number)?.toDouble()) ?: 2.0,
        agentDpCommissionPercent = (this["agentDpCommissionPercent"] as? Double) ?: ((this["agentDpCommissionPercent"] as? Number)?.toDouble()) ?: 2.0,
        agentWdCommissionPercent = (this["agentWdCommissionPercent"] as? Double) ?: ((this["agentWdCommissionPercent"] as? Number)?.toDouble()) ?: 2.0,
        role = this["role"] as? String ?: "User",
        referralCode = this["referralCode"] as? String ?: "",
        referredBy = this["referredBy"] as? String ?: "",
        lastBettingSite = this["lastBettingSite"] as? String ?: "1xbet",
        lastBettingUserId = this["lastBettingUserId"] as? String ?: "",
        isBlocked = this["isBlocked"] as? Boolean ?: false,
        isHold = this["isHold"] as? Boolean ?: false,
        ignoreWithdrawLimit = this["ignoreWithdrawLimit"] as? Boolean ?: false,
        canDeposit = this["canDeposit"] as? Boolean ?: true,
        canWithdraw = this["canWithdraw"] as? Boolean ?: true,
        canAddMoney = this["canAddMoney"] as? Boolean ?: true,
        canOutMoney = this["canOutMoney"] as? Boolean ?: true,
        canSendMoney = this["canSendMoney"] as? Boolean ?: true,
        canReceiveMoney = this["canReceiveMoney"] as? Boolean ?: true,
        eposCode = this["eposCode"] as? String ?: "",
        hiddenPenaltyMain = (this["hiddenPenaltyMain"] as? Double) ?: ((this["hiddenPenaltyMain"] as? Number)?.toDouble()) ?: 0.0,
        hiddenPenaltyOut = (this["hiddenPenaltyOut"] as? Double) ?: ((this["hiddenPenaltyOut"] as? Number)?.toDouble()) ?: 0.0,
        hiddenPenaltyCombined = (this["hiddenPenaltyCombined"] as? Double) ?: ((this["hiddenPenaltyCombined"] as? Number)?.toDouble()) ?: 0.0
    )
}

fun Transaction.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "type" to type,
    "amount" to amount,
    "trxId" to trxId,
    "previousBalance" to previousBalance,
    "newBalance" to newBalance,
    "timestamp" to timestamp
)

fun Map<String, Any?>.toTransaction(): Transaction {
    return Transaction(
        id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Int) ?: 0,
        userId = (this["userId"] as? Long)?.toInt() ?: (this["userId"] as? Int) ?: 0,
        type = this["type"] as? String ?: "",
        amount = (this["amount"] as? Double) ?: ((this["amount"] as? Number)?.toDouble()) ?: 0.0,
        trxId = this["trxId"] as? String ?: "",
        previousBalance = (this["previousBalance"] as? Double) ?: ((this["previousBalance"] as? Number)?.toDouble()) ?: 0.0,
        newBalance = (this["newBalance"] as? Double) ?: ((this["newBalance"] as? Number)?.toDouble()) ?: 0.0,
        timestamp = (this["timestamp"] as? Long) ?: ((this["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )
}

fun Voucher.toMap(): Map<String, Any?> = mapOf(
    "code" to code,
    "amount" to amount,
    "creatorId" to creatorId,
    "isRedeemed" to isRedeemed,
    "redeemedById" to redeemedById,
    "balanceType" to balanceType,
    "isCancelled" to isCancelled,
    "designatedReceiver" to designatedReceiver,
    "expiresAt" to expiresAt,
    "timestamp" to timestamp
)

fun Map<String, Any?>.toVoucher(): Voucher {
    return Voucher(
        code = this["code"] as? String ?: "",
        amount = (this["amount"] as? Double) ?: ((this["amount"] as? Number)?.toDouble()) ?: 0.0,
        creatorId = (this["creatorId"] as? Long)?.toInt() ?: (this["creatorId"] as? Int) ?: 0,
        isRedeemed = this["isRedeemed"] as? Boolean ?: false,
        redeemedById = (this["redeemedById"] as? Long)?.toInt() ?: (this["redeemedById"] as? Int),
        balanceType = this["balanceType"] as? String ?: "Main Balance",
        isCancelled = this["isCancelled"] as? Boolean ?: false,
        designatedReceiver = this["designatedReceiver"] as? String,
        expiresAt = (this["expiresAt"] as? Long) ?: ((this["expiresAt"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
        timestamp = (this["timestamp"] as? Long) ?: ((this["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )
}

fun AppConfig.toMap(): Map<String, Any?> = mapOf(
    "key" to key,
    "value" to value
)

fun Map<String, Any?>.toAppConfig(): AppConfig {
    return AppConfig(
        key = this["key"] as? String ?: "",
        value = this["value"] as? String ?: ""
    )
}

fun BettingSite.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "siteName" to siteName,
    "username" to username,
    "passwordHash" to passwordHash,
    "workId" to workId,
    "withdrawAddress" to withdrawAddress,
    "balance" to balance,
    "isActive" to isActive,
    "onlyDeposit" to onlyDeposit,
    "onlyWithdraw" to onlyWithdraw
)

fun Map<String, Any?>.toBettingSite(): BettingSite {
    return BettingSite(
        id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Int) ?: 0,
        siteName = this["siteName"] as? String ?: "",
        username = this["username"] as? String ?: "",
        passwordHash = this["passwordHash"] as? String ?: "",
        workId = this["workId"] as? String ?: "",
        withdrawAddress = this["withdrawAddress"] as? String ?: "",
        balance = (this["balance"] as? Double) ?: ((this["balance"] as? Number)?.toDouble()) ?: 0.0,
        isActive = this["isActive"] as? Boolean ?: true,
        onlyDeposit = this["onlyDeposit"] as? Boolean ?: false,
        onlyWithdraw = this["onlyWithdraw"] as? Boolean ?: false
    )
}

fun PaymentMethodConfig.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "number" to number,
    "subMethod" to subMethod,
    "isActive" to isActive
)

fun Map<String, Any?>.toPaymentMethodConfig(): PaymentMethodConfig {
    return PaymentMethodConfig(
        id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Int) ?: 0,
        name = this["name"] as? String ?: "",
        number = this["number"] as? String ?: "",
        subMethod = this["subMethod"] as? String ?: "",
        isActive = this["isActive"] as? Boolean ?: true
    )
}

fun SupportTicket.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "username" to username,
    "subject" to subject,
    "status" to status,
    "lastUpdated" to lastUpdated,
    "createdAt" to createdAt
)

fun Map<String, Any?>.toSupportTicket(): SupportTicket {
    return SupportTicket(
        id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Int) ?: 0,
        userId = (this["userId"] as? Long)?.toInt() ?: (this["userId"] as? Int) ?: 0,
        username = this["username"] as? String ?: "",
        subject = this["subject"] as? String ?: "",
        status = this["status"] as? String ?: "Open",
        lastUpdated = (this["lastUpdated"] as? Long) ?: ((this["lastUpdated"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
        createdAt = (this["createdAt"] as? Long) ?: ((this["createdAt"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )
}

fun SupportMessage.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "ticketId" to ticketId,
    "senderId" to senderId,
    "senderRole" to senderRole,
    "senderName" to senderName,
    "message" to message,
    "timestamp" to timestamp,
    "imagePath" to imagePath
)

fun Map<String, Any?>.toSupportMessage(): SupportMessage {
    return SupportMessage(
        id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Int) ?: 0,
        ticketId = (this["ticketId"] as? Long)?.toInt() ?: (this["ticketId"] as? Int) ?: 0,
        senderId = (this["senderId"] as? Long)?.toInt() ?: (this["senderId"] as? Int) ?: 0,
        senderRole = this["senderRole"] as? String ?: "User",
        senderName = this["senderName"] as? String ?: "",
        message = this["message"] as? String ?: "",
        timestamp = (this["timestamp"] as? Long) ?: ((this["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
        imagePath = this["imagePath"] as? String
    )
}

fun Offer.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "description" to description,
    "dateCreated" to dateCreated
)

fun Map<String, Any?>.toOffer(): Offer {
    return Offer(
        id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Int) ?: 0,
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        dateCreated = this["dateCreated"] as? String ?: ""
    )
}

// --- Real-time Bidirectional Firebase Synchronization Manager ---

object FirebaseSyncManager {
    private const val TAG = "FirebaseSyncManager"
    private var isInitialized = false

    fun initSync(db: WalletDatabase) {
        if (isInitialized) return
        isInitialized = true
        Log.d(TAG, "Initializing Firebase real-time synchronization...")

        val firestore = FirebaseFirestore.getInstance()
        val dao = db.walletDao()
        val scope = CoroutineScope(Dispatchers.IO)

        // 1. Sync Users collection
        firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: users", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                val user = data.toUser()
                                dao.insertUser(user)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing user document sync: ", e)
                        }
                    }
                }
            }
        }

        // 2. Sync Transactions collection
        firestore.collection("transactions").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: transactions", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                val trx = data.toTransaction()
                                dao.insertTransaction(trx)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing transaction sync: ", e)
                        }
                    }
                }
            }
        }

        // 3. Sync Vouchers collection
        firestore.collection("vouchers").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: vouchers", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                val voucher = data.toVoucher()
                                dao.insertVoucher(voucher)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing voucher sync: ", e)
                        }
                    }
                }
            }
        }

        // 4. Sync App Config
        firestore.collection("app_config").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: app_config", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                val config = data.toAppConfig()
                                dao.insertConfig(config)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing config sync: ", e)
                        }
                    }
                }
            }
        }

        // 5. Sync Betting Sites
        firestore.collection("betting_sites").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: betting_sites", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            val site = data.toBettingSite()
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                dao.insertBettingSite(site)
                            } else if (change.type == DocumentChange.Type.REMOVED) {
                                dao.deleteBettingSite(site)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing betting site sync: ", e)
                        }
                    }
                }
            }
        }

        // 6. Sync Payment Methods
        firestore.collection("payment_methods").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: payment_methods", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            val method = data.toPaymentMethodConfig()
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                dao.insertPaymentMethod(method)
                            } else if (change.type == DocumentChange.Type.REMOVED) {
                                dao.deletePaymentMethod(method)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing payment method sync: ", e)
                        }
                    }
                }
            }
        }

        // 7. Sync Support Tickets
        firestore.collection("support_tickets").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: support_tickets", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            val ticket = data.toSupportTicket()
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                dao.insertTicket(ticket)
                            } else if (change.type == DocumentChange.Type.REMOVED) {
                                dao.deleteTicket(ticket)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing support ticket sync: ", e)
                        }
                    }
                }
            }
        }

        // 8. Sync Support Messages
        firestore.collection("support_messages").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: support_messages", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                val msg = data.toSupportMessage()
                                dao.insertMessage(msg)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing support message sync: ", e)
                        }
                    }
                }
            }
        }

        // 9. Sync Offers
        firestore.collection("offers").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Sync error on collection: offers", error)
                return@addSnapshotListener
            }
            snapshot?.let { sn ->
                scope.launch {
                    for (change in sn.documentChanges) {
                        try {
                            val data = change.document.data
                            val offer = data.toOffer()
                            if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                                dao.insertOffer(offer)
                            } else if (change.type == DocumentChange.Type.REMOVED) {
                                dao.deleteOfferById(offer.id)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing offer sync: ", e)
                        }
                    }
                }
            }
        }
    }
}
