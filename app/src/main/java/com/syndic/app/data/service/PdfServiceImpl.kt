package com.syndic.app.data.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.domain.service.PdfService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class PdfServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PdfService {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    override suspend fun generateReceipt(transaction: TransactionEntity): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                if (transaction.type != TransactionType.PAIEMENT) {
                    return@withContext Result.failure(IllegalArgumentException("Not a receipt transaction"))
                }

                val fileName = "Recu_${transaction.userId}_${fileDateFormat.format(transaction.date)}.pdf"
                val file = File(getDocsDir(), fileName)

                generatePdf(file, "REÇU DE PAIEMENT", transaction)

                Result.success(file)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun generateExpenseVoucher(transaction: TransactionEntity): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                if (transaction.type != TransactionType.DEPENSE) {
                    return@withContext Result.failure(IllegalArgumentException("Not an expense transaction"))
                }

                val fileName = "Bon_Depense_${fileDateFormat.format(transaction.date)}.pdf"
                val file = File(getDocsDir(), fileName)

                generatePdf(file, "BON DE DÉPENSE", transaction)

                Result.success(file)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getDocsDir(): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Receipts")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun generatePdf(file: File, title: String, transaction: TransactionEntity) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Background
        canvas.drawColor(Color.WHITE)

        // Title
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(title, 297.5f, 50f, paint)

        // Divider
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 70f, 545f, 70f, paint)

        // Content
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.LEFT

        var y = 120f
        val x = 50f
        val lineHeight = 25f

        // Metadata
        canvas.drawText("Date: ${dateFormat.format(transaction.date)}", x, y, paint)
        y += lineHeight
        canvas.drawText("Référence: ${transaction.id.take(8)}", x, y, paint)
        y += lineHeight * 2

        // Specifics
        if (transaction.type == TransactionType.PAIEMENT) {
            canvas.drawText("Résident: ${transaction.userId ?: "N/A"}", x, y, paint)
            y += lineHeight
            canvas.drawText("Méthode: ${transaction.paymentMethod?.name ?: "N/A"}", x, y, paint)
        } else if (transaction.type == TransactionType.DEPENSE) {
            canvas.drawText("Prestataire: ${transaction.provider ?: "N/A"}", x, y, paint)
            y += lineHeight
            canvas.drawText("Catégorie: ${transaction.category ?: "N/A"}", x, y, paint)
        }

        y += lineHeight * 2

        // Amount Box
        val amountRectY = y - 20
        paint.style = Paint.Style.STROKE
        canvas.drawRect(x, amountRectY, 545f, amountRectY + 60, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("MONTANT: ${String.format("%.2f", transaction.amount)} DH", x + 20, y + 20, paint)

        y += 100f

        // Footer
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Signature:", 400f, y, paint)

        pdfDocument.finishPage(page)

        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()
    }
}
