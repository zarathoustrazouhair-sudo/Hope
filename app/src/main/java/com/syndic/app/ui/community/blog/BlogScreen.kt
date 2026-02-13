package com.syndic.app.ui.community.blog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syndic.app.data.local.entity.BlogPostEntity
import com.syndic.app.ui.theme.CockpitGold
import com.syndic.app.ui.theme.CockpitGreen
import com.syndic.app.ui.theme.NightBlue
import com.syndic.app.ui.theme.Slate
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen(
    viewModel: BlogViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddPostDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NightBlue,
        floatingActionButton = {
            if (state.isSyndic) {
                FloatingActionButton(
                    onClick = { showAddPostDialog = true },
                    containerColor = CockpitGold
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = NightBlue)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Header
            Text(
                text = "MAGAZINE RÉSIDENCE",
                color = CockpitGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif, // Playfair Display equivalent fallback
                modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally)
            )

            if (state.isLoading && state.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CockpitGold)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.posts) { post ->
                        BlogPostCard(post)
                    }
                }
            }
        }
    }

    if (showAddPostDialog) {
        AddPostDialog(
            onDismiss = { showAddPostDialog = false },
            onConfirm = { title, content ->
                viewModel.createPost(title, content)
                showAddPostDialog = false
            }
        )
    }
}

@Composable
fun BlogPostCard(post: BlogPostEntity) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = post.category.uppercase(),
                color = CockpitGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.title,
                color = CockpitGold,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormat.format(post.date),
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = post.content,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "— Syndic", // Assuming Syndic is author for now, or fetch name
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Annonce") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre (Luxe)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenu") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, content) },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitGold)
            ) {
                Text("Publier", color = NightBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
