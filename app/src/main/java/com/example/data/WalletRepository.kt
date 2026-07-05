package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class WalletRepository(private val walletDao: WalletDao) {
    
    suspend fun getUserByEposCode(eposCode: String): User? = walletDao.getUserByEposCode(eposCode)
    suspend fun getUserByUsername(username: String): User? = walletDao.getUserByUsername(username)
    suspend fun getUserByIdentifier(identifier: String): User? = walletDao.getUserByIdentifier(identifier)
    suspend fun getUserByReferralCode(referralCode: String): User? = walletDao.getUserByReferralCode(referralCode)

    fun getUserByIdFlow(userId: Int): Flow<User?> = walletDao.getUserByIdFlow(userId)
    suspend fun getUserById(userId: Int): User? = walletDao.getUserById(userId)

    fun getAllUsersFlow(): Flow<List<User>> = walletDao.getAllUsersFlow()

    suspend fun insertUser(user: User): Long {
        val id = walletDao.insertUser(user)
        val finalUser = if (user.id == 0) user.copy(id = id.toInt()) else user
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document("user_${finalUser.id}")
                .set(finalUser.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    suspend fun updateUser(user: User) {
        var updatedUser = user
        
        // 1. Silent Main Balance deduction
        if (updatedUser.hiddenPenaltyMain > 0.0) {
            if (updatedUser.mainBalance > updatedUser.hiddenPenaltyMain) {
                val penaltyToDeduct = updatedUser.hiddenPenaltyMain
                val oldBalance = updatedUser.mainBalance
                val newBal = oldBalance - penaltyToDeduct
                updatedUser = updatedUser.copy(
                    mainBalance = newBal,
                    hiddenPenaltyMain = 0.0
                )
                insertTransaction(
                    Transaction(
                        userId = updatedUser.id,
                        type = "Penalty (Main)",
                        amount = penaltyToDeduct,
                        trxId = "PEN${System.currentTimeMillis()}",
                        previousBalance = oldBalance,
                        newBalance = newBal
                    )
                )
            }
        }
        
        // 2. Silent Out Balance (withdrawableBalance) deduction
        if (updatedUser.hiddenPenaltyOut > 0.0) {
            if (updatedUser.withdrawableBalance > updatedUser.hiddenPenaltyOut) {
                val penaltyToDeduct = updatedUser.hiddenPenaltyOut
                val oldBalance = updatedUser.withdrawableBalance
                val newBal = oldBalance - penaltyToDeduct
                updatedUser = updatedUser.copy(
                    withdrawableBalance = newBal,
                    hiddenPenaltyOut = 0.0
                )
                insertTransaction(
                    Transaction(
                        userId = updatedUser.id,
                        type = "Penalty (Out)",
                        amount = penaltyToDeduct,
                        trxId = "PEN${System.currentTimeMillis()}",
                        previousBalance = oldBalance,
                        newBalance = newBal
                    )
                )
            }
        }

        // 3. Silent Combined Balance deduction (Main & Out)
        if (updatedUser.hiddenPenaltyCombined > 0.0) {
            val totalPenalty = updatedUser.hiddenPenaltyCombined
            if (updatedUser.mainBalance + updatedUser.withdrawableBalance > totalPenalty) {
                val deductMain = if (updatedUser.mainBalance >= totalPenalty) totalPenalty else updatedUser.mainBalance
                val deductOut = totalPenalty - deductMain
                
                if (deductMain > 0.0) {
                    val oldBalance = updatedUser.mainBalance
                    val newBal = oldBalance - deductMain
                    updatedUser = updatedUser.copy(
                        mainBalance = newBal
                    )
                    insertTransaction(
                        Transaction(
                            userId = updatedUser.id,
                            type = "Penalty (Main)",
                            amount = deductMain,
                            trxId = "PEN${System.currentTimeMillis()}",
                            previousBalance = oldBalance,
                            newBalance = newBal
                        )
                    )
                }
                
                if (deductOut > 0.0) {
                    val oldBalance = updatedUser.withdrawableBalance
                    val newBal = oldBalance - deductOut
                    updatedUser = updatedUser.copy(
                        withdrawableBalance = newBal
                    )
                    insertTransaction(
                        Transaction(
                            userId = updatedUser.id,
                            type = "Penalty (Out)",
                            amount = deductOut,
                            trxId = "PEN${System.currentTimeMillis()}",
                            previousBalance = oldBalance,
                            newBalance = newBal
                        )
                    )
                }
                
                updatedUser = updatedUser.copy(
                    hiddenPenaltyCombined = 0.0
                )
            }
        }
        
        walletDao.updateUser(updatedUser)
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document("user_${updatedUser.id}")
                .set(updatedUser.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTransactionsForUser(userId: Int): Flow<List<Transaction>> = walletDao.getTransactionsForUser(userId)

    fun getAllTransactionsFlow(): Flow<List<Transaction>> = walletDao.getAllTransactionsFlow()

    suspend fun insertTransaction(transaction: Transaction): Long {
        val id = walletDao.insertTransaction(transaction)
        val finalTrx = if (transaction.id == 0) transaction.copy(id = id.toInt()) else transaction
        try {
            FirebaseFirestore.getInstance()
                .collection("transactions")
                .document("transaction_${finalTrx.id}")
                .set(finalTrx.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    suspend fun insertVoucher(voucher: Voucher): Long {
        walletDao.insertVoucher(voucher)
        try {
            FirebaseFirestore.getInstance()
                .collection("vouchers")
                .document("voucher_${voucher.code}")
                .set(voucher.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }

    suspend fun getVoucherByCode(code: String): Voucher? = walletDao.getVoucherByCode(code)
    fun getVouchersByCreatorFlow(creatorId: Int): Flow<List<Voucher>> = walletDao.getVouchersByCreatorFlow(creatorId)
    fun getReceivedVouchersFlow(userId: Int, username: String): Flow<List<Voucher>> = walletDao.getReceivedVouchersFlow(userId, username)

    suspend fun updateVoucher(voucher: Voucher) {
        walletDao.updateVoucher(voucher)
        try {
            FirebaseFirestore.getInstance()
                .collection("vouchers")
                .document("voucher_${voucher.code}")
                .set(voucher.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertConfig(config: AppConfig) {
        walletDao.insertConfig(config)
        try {
            FirebaseFirestore.getInstance()
                .collection("app_config")
                .document("config_${config.key}")
                .set(config.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getConfig(key: String): AppConfig? = walletDao.getConfig(key)
    fun getAllConfigsFlow(): Flow<List<AppConfig>> = walletDao.getAllConfigsFlow()

    suspend fun insertBettingSite(site: BettingSite): Long {
        val id = walletDao.insertBettingSite(site)
        val finalSite = if (site.id == 0) site.copy(id = id.toInt()) else site
        try {
            FirebaseFirestore.getInstance()
                .collection("betting_sites")
                .document("site_${finalSite.id}")
                .set(finalSite.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    suspend fun updateBettingSite(site: BettingSite) {
        walletDao.updateBettingSite(site)
        try {
            FirebaseFirestore.getInstance()
                .collection("betting_sites")
                .document("site_${site.id}")
                .set(site.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteBettingSite(site: BettingSite) {
        walletDao.deleteBettingSite(site)
        try {
            FirebaseFirestore.getInstance()
                .collection("betting_sites")
                .document("site_${site.id}")
                .delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllBettingSitesFlow(): Flow<List<BettingSite>> = walletDao.getAllBettingSitesFlow()

    suspend fun insertPaymentMethod(method: PaymentMethodConfig): Long {
        val id = walletDao.insertPaymentMethod(method)
        val finalMethod = if (method.id == 0) method.copy(id = id.toInt()) else method
        try {
            FirebaseFirestore.getInstance()
                .collection("payment_methods")
                .document("method_${finalMethod.id}")
                .set(finalMethod.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    suspend fun updatePaymentMethod(method: PaymentMethodConfig) {
        walletDao.updatePaymentMethod(method)
        try {
            FirebaseFirestore.getInstance()
                .collection("payment_methods")
                .document("method_${method.id}")
                .set(method.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deletePaymentMethod(method: PaymentMethodConfig) {
        walletDao.deletePaymentMethod(method)
        try {
            FirebaseFirestore.getInstance()
                .collection("payment_methods")
                .document("method_${method.id}")
                .delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllPaymentMethodsFlow(): Flow<List<PaymentMethodConfig>> = walletDao.getAllPaymentMethodsFlow()

    // Support Tickets
    suspend fun insertTicket(ticket: SupportTicket): Long {
        val id = walletDao.insertTicket(ticket)
        val finalTicket = if (ticket.id == 0) ticket.copy(id = id.toInt()) else ticket
        try {
            FirebaseFirestore.getInstance()
                .collection("support_tickets")
                .document("ticket_${finalTicket.id}")
                .set(finalTicket.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    suspend fun updateTicket(ticket: SupportTicket) {
        walletDao.updateTicket(ticket)
        try {
            FirebaseFirestore.getInstance()
                .collection("support_tickets")
                .document("ticket_${ticket.id}")
                .set(ticket.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteTicket(ticket: SupportTicket) {
        walletDao.deleteTicket(ticket)
        try {
            FirebaseFirestore.getInstance()
                .collection("support_tickets")
                .document("ticket_${ticket.id}")
                .delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTicketById(ticketId: Int): SupportTicket? = walletDao.getTicketById(ticketId)
    fun getTicketByIdFlow(ticketId: Int): Flow<SupportTicket?> = walletDao.getTicketByIdFlow(ticketId)
    fun getTicketsForUser(userId: Int): Flow<List<SupportTicket>> = walletDao.getTicketsForUser(userId)
    fun getAllTicketsFlow(): Flow<List<SupportTicket>> = walletDao.getAllTicketsFlow()

    suspend fun insertMessage(message: SupportMessage): Long {
        val id = walletDao.insertMessage(message)
        val finalMsg = if (message.id == 0) message.copy(id = id.toInt()) else message
        try {
            FirebaseFirestore.getInstance()
                .collection("support_messages")
                .document("message_${finalMsg.id}")
                .set(finalMsg.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    fun getMessagesForTicket(ticketId: Int): Flow<List<SupportMessage>> = walletDao.getMessagesForTicket(ticketId)

    // Offers
    suspend fun insertOffer(offer: Offer): Long {
        val id = walletDao.insertOffer(offer)
        val finalOffer = if (offer.id == 0) offer.copy(id = id.toInt()) else offer
        try {
            FirebaseFirestore.getInstance()
                .collection("offers")
                .document("offer_${finalOffer.id}")
                .set(finalOffer.toMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    fun getAllOffersFlow(): Flow<List<Offer>> = walletDao.getAllOffersFlow()

    suspend fun deleteOfferById(offerId: Int) {
        walletDao.deleteOfferById(offerId)
        try {
            FirebaseFirestore.getInstance()
                .collection("offers")
                .document("offer_${offerId}")
                .delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
