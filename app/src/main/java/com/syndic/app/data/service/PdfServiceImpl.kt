package com.syndic.app.data.service

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import com.syndic.app.R
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.service.PdfService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class PdfServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configRepository: ConfigRepository
) : PdfService {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    // A5 Landscape Dimensions (595 x 420 points approx)
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 420

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

    private suspend fun generatePdf(file: File, title: String, transaction: TransactionEntity) {
        val config = configRepository.getConfig().firstOrNull()

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create() // A5 Landscape
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Background
        canvas.drawColor(Color.WHITE)

        // Draw Logo (Placeholder if no custom logo, reusing app logo vector roughly or drawing placeholder)
        // Since we can't easily rasterize vector drawable without a bitmap helper here, we assume standard bitmap logic.
        // For strict offline constraint without coil, we draw a text placeholder or primitive.
        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText(config?.residenceName ?: "RÉSIDENCE", 20f, 30f, paint)

        // Title
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(title, PAGE_WIDTH / 2f, 50f, paint)

        // Divider
        paint.strokeWidth = 2f
        canvas.drawLine(20f, 70f, (PAGE_WIDTH - 20).toFloat(), 70f, paint)

        // Content
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.LEFT

        var y = 100f
        val x = 50f
        val lineHeight = 20f

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
        canvas.drawRect(x, amountRectY, (PAGE_WIDTH - 50).toFloat(), amountRectY + 50, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("MONTANT: ${String.format("%.2f", transaction.amount)} DH", x + 20, y + 15, paint)

        // Footer / Stamp
        y += 80f
        val footerY = (PAGE_HEIGHT - 60).toFloat()

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Le Syndic ${config?.syndicCivility ?: ""} ${config?.residenceName ?: ""}", (PAGE_WIDTH - 50).toFloat(), footerY, paint)

        // Auto Stamp Logic
        if (config?.isAutoStampEnabled == true && config.stampUri != null) {
            try {
                val uri = Uri.parse(config.stampUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                // Draw bitmap scaled down at bottom right
                if (bitmap != null) {
                    val stampSize = 80
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, stampSize, stampSize, true)
                    canvas.drawBitmap(scaledBitmap, (PAGE_WIDTH - 50 - stampSize).toFloat(), footerY - 10, null)
                }
                inputStream?.close()
            } catch (e: Exception) {
                // Ignore stamp load failure, just don't draw
            }
        }

        pdfDocument.finishPage(page)

        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()
    }
}
