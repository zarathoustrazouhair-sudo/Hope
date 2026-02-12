package com.syndic.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syndic.app.ui.matrix.MatrixColor
import com.syndic.app.ui.matrix.ResidentStatus
import com.syndic.app.ui.theme.CyanNeon
import com.syndic.app.ui.theme.Gold
import com.syndic.app.ui.theme.RoseNeon
import com.syndic.app.ui.theme.Slate

@Composable
fun ResidentMatrixGrid(
    residents: List<ResidentStatus>,
    onResidentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 Columns
        modifier = modifier
    ) {
        items(residents) { resident ->
            ResidentMatrixCell(resident, onResidentClick)
        }
    }
}

@Composable
fun ResidentMatrixCell(
    resident: ResidentStatus,
    onClick: (String) -> Unit
) {
    val borderColor = when (resident.statusColor) {
        MatrixColor.GOLD -> Gold
        MatrixColor.GREEN -> CyanNeon
        MatrixColor.RED -> RoseNeon
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f) // Square cells
            .clip(RoundedCornerShape(8.dp))
            .background(Slate) // Base color
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable { onClick(resident.apartment) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = resident.apartment,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}
