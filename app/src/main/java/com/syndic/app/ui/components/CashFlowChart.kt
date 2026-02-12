package com.syndic.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.syndic.app.ui.theme.CyanNeon

@Composable
fun CashFlowChart(
    modifier: Modifier = Modifier
) {
    // Placeholder Bezier Curve for "Zero Dependencies" requirement
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, height * 0.8f)
            cubicTo(
                width * 0.3f, height * 0.9f,
                width * 0.6f, height * 0.4f,
                width, height * 0.2f
            )
        }

        drawPath(
            path = path,
            color = CyanNeon,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
