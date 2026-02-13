package com.syndic.app.domain.service

import com.syndic.app.data.local.entity.TransactionEntity
import java.io.File

interface PdfService {
    suspend fun generateReceipt(transaction: TransactionEntity): Result<File>
    suspend fun generateExpenseVoucher(transaction: TransactionEntity): Result<File>
}
