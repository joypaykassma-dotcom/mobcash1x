package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM users WHERE eposCode = :eposCode LIMIT 1")
    suspend fun getUserByEposCode(eposCode: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :identifier OR email = :identifier OR number = :identifier OR CAST(id AS TEXT) = :identifier LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): User?

    @Query("SELECT * FROM users WHERE referralCode = :referralCode LIMIT 1")
    suspend fun getUserByReferralCode(referralCode: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    // Voucher queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: Voucher)

    @Query("SELECT * FROM vouchers WHERE code = :code LIMIT 1")
    suspend fun getVoucherByCode(code: String): Voucher?

    @Query("SELECT * FROM vouchers WHERE creatorId = :creatorId ORDER BY timestamp DESC")
    fun getVouchersByCreatorFlow(creatorId: Int): Flow<List<Voucher>>

    @Query("SELECT * FROM vouchers WHERE redeemedById = :userId OR designatedReceiver = :username ORDER BY timestamp DESC")
    fun getReceivedVouchersFlow(userId: Int, username: String): Flow<List<Voucher>>

    @Update
    suspend fun updateVoucher(voucher: Voucher)

    // AppConfig queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)

    @Query("SELECT * FROM app_config WHERE `key` = :key LIMIT 1")
    suspend fun getConfig(key: String): AppConfig?

    @Query("SELECT * FROM app_config")
    fun getAllConfigsFlow(): Flow<List<AppConfig>>

    // BettingSite queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBettingSite(site: BettingSite): Long

    @Update
    suspend fun updateBettingSite(site: BettingSite)

    @Delete
    suspend fun deleteBettingSite(site: BettingSite)

    @Query("SELECT * FROM betting_sites")
    fun getAllBettingSitesFlow(): Flow<List<BettingSite>>

    // PaymentMethodConfig queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(method: PaymentMethodConfig): Long

    @Update
    suspend fun updatePaymentMethod(method: PaymentMethodConfig)

    @Delete
    suspend fun deletePaymentMethod(method: PaymentMethodConfig)

    @Query("SELECT * FROM payment_methods")
    fun getAllPaymentMethodsFlow(): Flow<List<PaymentMethodConfig>>

    // Support Ticket queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicket): Long

    @Update
    suspend fun updateTicket(ticket: SupportTicket)

    @Delete
    suspend fun deleteTicket(ticket: SupportTicket)

    @Query("SELECT * FROM support_tickets WHERE id = :ticketId LIMIT 1")
    suspend fun getTicketById(ticketId: Int): SupportTicket?

    @Query("SELECT * FROM support_tickets WHERE id = :ticketId LIMIT 1")
    fun getTicketByIdFlow(ticketId: Int): Flow<SupportTicket?>

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY lastUpdated DESC")
    fun getTicketsForUser(userId: Int): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets ORDER BY lastUpdated DESC")
    fun getAllTicketsFlow(): Flow<List<SupportTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SupportMessage): Long

    @Query("SELECT * FROM support_messages WHERE ticketId = :ticketId ORDER BY timestamp ASC")
    fun getMessagesForTicket(ticketId: Int): Flow<List<SupportMessage>>

    // Offer queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer): Long

    @Query("SELECT * FROM offers ORDER BY id DESC")
    fun getAllOffersFlow(): Flow<List<Offer>>

    @Query("DELETE FROM offers WHERE id = :offerId")
    suspend fun deleteOfferById(offerId: Int)
}
