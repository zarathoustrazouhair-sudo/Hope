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
import androidx.compose.ui.unit.dp
import com.syndic.app.ui.matrix.MatrixItemUiState
import com.syndic.app.ui.theme.CyanNeon
import com.syndic.app.ui.theme.RoseNeon
import com.syndic.app.ui.theme.Slate

@Composable
fun ResidentMatrixGrid(
    residents: List<MatrixItemUiState>,
    onResidentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
    ) {
        items(residents) { resident ->
            ResidentMatrixCell(resident, onResidentClick)
        }
    }
}

@Composable
fun ResidentMatrixCell(
    resident: MatrixItemUiState,
    onClick: (String) -> Unit
) {
    val borderColor = if (resident.isUpToDate) CyanNeon else RoseNeon

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Slate)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable { onClick(resident.userId) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AP${resident.apartmentNumber}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
