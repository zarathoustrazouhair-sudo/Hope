package com.syndic.app.ui.cockpit.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.TaskEntity
import com.syndic.app.domain.repository.TaskRepository
import com.syndic.app.ui.theme.CockpitGold
import com.syndic.app.ui.theme.CockpitGreen
import com.syndic.app.ui.theme.Slate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getPendingTasks().collectLatest {
                _tasks.value = it
            }
        }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.completeTask(task.id)
        }
    }
}

@Composable
fun TaskWidget(
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate),
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("TÂCHES PRIORITAIRES", color = CockpitGold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Text("Aucune tâche en attente.", color = Color.Gray)
            } else {
                LazyColumn {
                    items(tasks.take(3)) { task -> // Show max 3
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(task.title, color = Color.White)
                                Text(dateFormat.format(task.dueDate), color = Color.Gray, fontSize = 10.sp)
                            }
                            Checkbox(
                                checked = false,
                                onCheckedChange = { viewModel.completeTask(task) },
                                colors = CheckboxDefaults.colors(checkmarkColor = CockpitGreen)
                            )
                        }
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}
