package com.bibbidi.habittracker.ui.detailhabit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibbidi.habittracker.data.model.DBResult
import com.bibbidi.habittracker.data.source.HabitsRepository
import com.bibbidi.habittracker.ui.common.Constants.HABIT_ID_KEY
import com.bibbidi.habittracker.ui.common.MutableEventFlow
import com.bibbidi.habittracker.ui.common.UiState
import com.bibbidi.habittracker.ui.common.asEventFlow
import com.bibbidi.habittracker.ui.mapper.asDomain
import com.bibbidi.habittracker.ui.mapper.asUiModel
import com.bibbidi.habittracker.ui.model.habit.HabitMemoItem
import com.bibbidi.habittracker.ui.model.habit.HabitResultUiModel
import com.bibbidi.habittracker.ui.model.habit.HabitUiModel
import com.bibbidi.habittracker.ui.model.habit.HabitWithLogsUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: HabitsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id = savedStateHandle.get<Long>(HABIT_ID_KEY)

    private val dateFlow: MutableStateFlow<LocalDate>

    private val _habitFlow = MutableStateFlow(HabitUiModel())
    val habitFlow = _habitFlow.asStateFlow()

    private val _habitResultFlow =
        MutableStateFlow<UiState<HabitResultUiModel>>(UiState.Loading)
    val habitResultFlow = _habitResultFlow.asStateFlow()

    private val _habitWithLogsFlow =
        MutableStateFlow<UiState<HabitWithLogsUiModel>>(UiState.Loading)
    val habitWithLogsFlow = _habitWithLogsFlow.asStateFlow()

    private val _event = MutableEventFlow<DetailHabitEvent>()
    val event = _event.asEventFlow()

    private val _memoFlow: MutableStateFlow<UiState<List<HabitMemoItem>>> = MutableStateFlow(UiState.Loading)
    val memoFlow = _memoFlow.asStateFlow()

    init {
        val now = LocalDate.now()
        dateFlow = MutableStateFlow(now)
        fetchHabit()
        loadHabitWithLogs()
        loadHabitResult(now)
        loadHabitMemos()
    }

    fun fetchHabit() {
        viewModelScope.launch {
            _habitFlow.value = repository.getHabitById(id).asUiModel()
        }
    }

    private fun loadHabitWithLogs() {
        viewModelScope.launch {
            repository.getHabitWithLogs(id).collectLatest {
                _habitWithLogsFlow.value = when (it) {
                    is DBResult.Success -> UiState.Success(it.data.asUiModel())
                    is DBResult.Loading -> UiState.Loading
                    else -> UiState.Empty
                }
            }
        }
    }

    private fun loadHabitResult(date: LocalDate) {
        viewModelScope.launch {
            repository.getHabitResult(id, date).collectLatest {
                _habitResultFlow.value = when (it) {
                    is DBResult.Success -> UiState.Success(it.data.asUiModel())
                    is DBResult.Loading -> UiState.Loading
                    else -> UiState.Empty
                }
            }
        }
    }

    private fun loadHabitMemos() {
        viewModelScope.launch {
            repository.getHabitMemos(id, true).collectLatest {
                _memoFlow.value = when (it) {
                    is DBResult.Success -> UiState.Success(
                        it.data.mapIndexed { i, data ->
                            val prev = it.data.getOrNull(i - 1)?.date?.monthValue
                            data.asUiModel().copy(isHeader = prev != data.date.monthValue)
                        }
                    )
                    is DBResult.Loading -> UiState.Loading
                    else -> UiState.Empty
                }
            }
        }
    }

    val setDate: (LocalDate) -> Unit = {
        viewModelScope.launch {
            dateFlow.value = it
        }
    }

    fun showDeleteHabit() {
        viewModelScope.launch {
            _event.emit(DetailHabitEvent.ShowDeleteHabit(habitFlow.value))
        }
    }

    fun showUpdateHabit() {
        viewModelScope.launch {
            _event.emit(DetailHabitEvent.ShowUpdateHabit(habitFlow.value))
        }
    }

    fun saveHabitMemo(memoItem: HabitMemoItem, memo: String?) {
        viewModelScope.launch {
            repository.saveHabitMemo(memoItem.asDomain(), memo)
        }
    }

    fun deleteHabitMemo(memoItem: HabitMemoItem) {
        viewModelScope.launch {
            repository.deleteHabitMemo(memoItem.logId)
        }
    }
}
