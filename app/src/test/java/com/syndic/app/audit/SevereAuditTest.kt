package com.syndic.app.audit

import com.syndic.app.data.local.dao.TransactionDao
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.data.repository.TransactionRepositoryImpl
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.util.MainDispatcherRule
import com.syndic.app.util.SecurityUtils
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class SevereAuditTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mock dependencies
    private val transactionDao = mockk<TransactionDao>()
    private val configRepository = mockk<ConfigRepository>()
    private val transactionRepository: TransactionRepository = TransactionRepositoryImpl(transactionDao, configRepository)

    // Helper for Transactions
    private fun createTx(amount: Double, type: TransactionType): TransactionEntity {
        return TransactionEntity(
            id = "tx_${System.nanoTime()}",
            userId = if (type != TransactionType.DEPENSE) "user1" else null,
            amount = amount,
            type = type,
            label = "Audit Tx",
            date = Date(),
            createdAt = Date()
        )
    }

    // --- FINANCIAL ENGINE AUDIT ---

    @Test
    fun `Audit 1 - Global Balance Calculation (Strict)`() = runTest {
        // Scenario:
        // 1. Initial Balance 0
        // 2. Income (PAIEMENT) +1000
        // 3. Expense (DEPENSE) -300
        // 4. Cotisation (COTISATION) +250 (Should NOT affect GLOBAL balance, only User balance!)

        val transactions = listOf(
            createTx(1000.0, TransactionType.PAIEMENT),
            createTx(300.0, TransactionType.DEPENSE),
            createTx(250.0, TransactionType.COTISATION) // Cotisation is internal debt tracking, not cash flow?
            // Wait, usually Cotisation creates a DEBT for user. Payment settles it.
            // Global Balance = Cash on Hand.
            // Cash on Hand increases when Payment is made. Decreases when Expense is made.
            // Cotisation (Debit) does NOT increase Cash on Hand. It increases Receivables (Créances).
            // Let's verify Repository logic:
            // getGlobalBalance: sum(PAIEMENT) - sum(DEPENSE). Correct. Cotisation is ignored.
        )

        coEvery { transactionDao.getAllTransactions() } returns flowOf(transactions)

        val globalBalance = transactionRepository.getGlobalBalance().first()

        // Expected: 1000 - 300 = 700. Cotisation (250) is ignored.
        assertEquals(700.0, globalBalance, 0.001)
    }

    @Test
    fun `Audit 2 - User Balance Calculation (Debt Logic)`() = runTest {
        // Scenario:
        // 1. User has Cotisation (Debt) of 250.
        // 2. User pays 200 (Partial Payment).
        // 3. User Balance should be -50 (Still owes 50).

        val transactions = listOf(
            createTx(250.0, TransactionType.COTISATION), // Debt
            createTx(200.0, TransactionType.PAIEMENT)    // Credit
        )

        coEvery { transactionDao.getUserTransactions("user1") } returns flowOf(transactions)

        val userBalance = transactionRepository.getUserBalance("user1").first()

        // Repository Logic: PAIEMENT - COTISATION
        // 200 - 250 = -50.
        assertEquals(-50.0, userBalance, 0.001)
    }

    @Test
    fun `Audit 3 - Runway Calculation (Survival Mode)`() = runTest {
        // Scenario:
        // Cash on Hand: 5000 DH
        // Monthly Fixed Costs:
        // - Concierge: 2000
        // - Cleaning: 500
        // - Maint: 0
        // - Other: 0
        // Total Burn: 2500
        // Expected Runway: 5000 / 2500 = 2.0 Months.

        val transactions = listOf(
            createTx(6000.0, TransactionType.PAIEMENT),
            createTx(1000.0, TransactionType.DEPENSE)
        ) // Net Cash: 5000

        val config = ResidenceConfigEntity(
            residenceName = "Test",
            conciergeSalary = 2000.0,
            cleaningCost = 500.0,
            maintenanceCost = 0.0,
            otherFixedCosts = 0.0
        )

        coEvery { transactionDao.getAllTransactionsSync() } returns transactions
        coEvery { configRepository.getConfig() } returns flowOf(config)

        val runway = transactionRepository.getRunway()

        assertEquals(2.0, runway, 0.001)
    }

    @Test
    fun `Audit 4 - Runway Edge Case (Zero Burn)`() = runTest {
        // Cash: 1000
        // Burn: 0
        // Expected: 99.9 (Infinite)

        val transactions = listOf(createTx(1000.0, TransactionType.PAIEMENT))
        val config = ResidenceConfigEntity(residenceName = "Test", conciergeSalary = 0.0)

        coEvery { transactionDao.getAllTransactionsSync() } returns transactions
        coEvery { configRepository.getConfig() } returns flowOf(config)

        val runway = transactionRepository.getRunway()

        assertEquals(99.9, runway, 0.001)
    }

    @Test
    fun `Audit 5 - Recovery Rate (Recouvrement)`() = runTest {
        // Scenario:
        // Total Cotisations (Due): 1000
        // Total Payments (Received): 800
        // Recovery Rate: 80%

        val transactions = listOf(
            createTx(500.0, TransactionType.COTISATION),
            createTx(500.0, TransactionType.COTISATION),
            createTx(800.0, TransactionType.PAIEMENT)
        )

        coEvery { transactionDao.getAllTransactionsSync() } returns transactions

        val rate = transactionRepository.getRecoveryRate()

        assertEquals(80.0, rate, 0.001)
    }

    // --- SECURITY AUDIT ---

    @Test
    fun `Audit 6 - PIN Security (Hashing)`() {
        // Plain PINs must never match their hash directly
        val pin = "1234"
        val hash = SecurityUtils.hashPin(pin)

        // 1. Hash is not empty
        assertTrue(hash.isNotEmpty())
        // 2. Hash is not the PIN itself
        assertFalse(hash == pin)
        // 3. Validation works
        assertTrue(SecurityUtils.validatePin(pin, hash))
        // 4. Wrong PIN fails
        assertFalse(SecurityUtils.validatePin("0000", hash))
        // 5. Empty PIN fails
        assertFalse(SecurityUtils.validatePin("", hash))
        // 6. Null hash fails safely
        assertFalse(SecurityUtils.validatePin("1234", null))
    }
}
