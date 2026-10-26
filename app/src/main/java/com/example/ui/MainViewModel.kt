package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.auth.GoogleAuthManager
import com.example.data.AppDatabase
import com.example.data.WheelRepository
import com.example.data.firestore.FirestoreWheelRepository
import com.example.model.SpinHistoryEntity
import com.example.model.WheelEntity
import com.example.model.WheelItemEntity
import com.example.model.WheelPaletteTheme
import com.example.model.WinnerStat
import com.example.util.SoundHapticManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WheelRepository
    private val firestoreRepository: FirestoreWheelRepository
    val soundHapticManager: SoundHapticManager = SoundHapticManager(application)

    private val auth: FirebaseAuth = Firebase.auth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _cloudSyncMessage = MutableStateFlow<String?>(null)
    val cloudSyncMessage: StateFlow<String?> = _cloudSyncMessage.asStateFlow()

    val wheels: StateFlow<List<WheelEntity>>
    private val _selectedWheelId = MutableStateFlow<Long?>(null)
    val selectedWheelId: StateFlow<Long?> = _selectedWheelId.asStateFlow()

    val currentWheel: StateFlow<WheelEntity?>
    val currentItems: StateFlow<List<WheelItemEntity>>
    val currentHistory: StateFlow<List<SpinHistoryEntity>>
    val currentStats: StateFlow<List<WinnerStat>>

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _winnerItem = MutableStateFlow<WheelItemEntity?>(null)
    val winnerItem: StateFlow<WheelItemEntity?> = _winnerItem.asStateFlow()

    private val _showWinnerDialog = MutableStateFlow(false)
    val showWinnerDialog: StateFlow<Boolean> = _showWinnerDialog.asStateFlow()

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            syncWheelsToCloud(user.uid)
        }
    }

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = WheelRepository(database.wheelDao())

        val dbId = application.getString(R.string.firestore_database_id)
        val firestore = FirebaseFirestore.getInstance(dbId)
        firestoreRepository = FirestoreWheelRepository(firestore)

        auth.addAuthStateListener(authStateListener)

        viewModelScope.launch {
            repository.ensureInitialData()
        }

        wheels = repository.allWheels
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        viewModelScope.launch {
            wheels.collect { wheelList ->
                if (_selectedWheelId.value == null && wheelList.isNotEmpty()) {
                    _selectedWheelId.value = wheelList.first().id
                }
            }
        }

        currentWheel = _selectedWheelId.flatMapLatest { id ->
            if (id != null) repository.getWheelById(id) else flowOf(null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        currentItems = _selectedWheelId.flatMapLatest { id ->
            if (id != null) repository.getItemsForWheel(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentHistory = _selectedWheelId.flatMapLatest { id ->
            if (id != null) repository.getHistoryForWheel(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentStats = _selectedWheelId.flatMapLatest { id ->
            if (id != null) repository.getWinnerStats(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun syncWheelsToCloud(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isCloudSyncing.value = true
                val allLocalWheels = wheels.value
                for (localWheel in allLocalWheels) {
                    val items = repository.getItemsForWheel(localWheel.id)
                    // Save or update wheel in Firestore
                    firestoreRepository.createWheel(
                        userId = userId,
                        title = localWheel.title,
                        description = localWheel.description,
                        themePalette = localWheel.themePalette
                    )
                }
                _cloudSyncMessage.value = "Synced with Cloud"
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync skipped: ${e.message}")
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        GoogleAuthManager.signOut(
            context = context,
            credentialManager = credentialManager,
            onSignOutComplete = {
                _cloudSyncMessage.value = "Signed out"
            },
            scope = viewModelScope
        )
    }

    fun clearCloudSyncMessage() {
        _cloudSyncMessage.value = null
    }

    fun selectWheel(wheel: WheelEntity) {
        _selectedWheelId.value = wheel.id
        _showWinnerDialog.value = false
        _showConfetti.value = false
    }

    fun createWheel(title: String, description: String) {
        viewModelScope.launch {
            val newWheel = WheelEntity(
                title = title,
                description = description,
                themePalette = WheelPaletteTheme.RAINBOW.name
            )
            val initialOptions = listOf("Option 1", "Option 2", "Option 3", "Option 4")
            val id = repository.createWheel(newWheel, initialOptions)
            _selectedWheelId.value = id

            // Also backup to cloud if logged in
            val uid = _currentUser.value?.uid
            if (uid != null) {
                try {
                    firestoreRepository.createWheel(
                        userId = uid,
                        title = title,
                        description = description,
                        themePalette = WheelPaletteTheme.RAINBOW.name,
                        initialItems = initialOptions
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Cloud backup error: ${e.message}")
                }
            }
        }
    }

    fun updateWheel(wheel: WheelEntity) {
        viewModelScope.launch {
            repository.updateWheel(wheel)
        }
    }

    fun deleteWheel(wheel: WheelEntity) {
        viewModelScope.launch {
            repository.deleteWheel(wheel)
            val currentList = wheels.value.filter { it.id != wheel.id }
            if (currentList.isNotEmpty()) {
                _selectedWheelId.value = currentList.first().id
            } else {
                _selectedWheelId.value = null
            }
        }
    }

    fun addItem(name: String) {
        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.addItem(wheelId, name)
        }
    }

    fun addBulkItems(rawText: String) {
        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.addBulkItems(wheelId, rawText)
        }
    }

    fun toggleItem(item: WheelItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isEnabled = !item.isEnabled))
        }
    }

    fun updateItem(item: WheelItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: WheelItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun shuffleItems() {
        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.shuffleItems(wheelId)
        }
    }

    fun sortItemsAZ() {
        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.sortItemsAlphabetically(wheelId)
        }
    }

    fun toggleAllItems(enable: Boolean) {
        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.toggleAllItems(wheelId, enable)
        }
    }

    fun clearAllItems() {
        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.clearItems(wheelId)
        }
    }

    fun clearHistory() {
        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.clearHistory(wheelId)
        }
    }

    fun onSpinStarted() {
        _isSpinning.value = true
        _showWinnerDialog.value = false
        _showConfetti.value = false
        val wheel = currentWheel.value
        soundHapticManager.playSpinStart(
            soundEnabled = wheel?.soundEnabled ?: true,
            hapticsEnabled = wheel?.hapticsEnabled ?: true
        )
    }

    fun onSpinTick() {
        val wheel = currentWheel.value
        soundHapticManager.playTick(
            soundEnabled = wheel?.soundEnabled ?: true,
            hapticsEnabled = wheel?.hapticsEnabled ?: true
        )
    }

    fun onSpinCompleted(winner: WheelItemEntity) {
        _isSpinning.value = false
        _winnerItem.value = winner
        _showWinnerDialog.value = true
        _showConfetti.value = true

        val wheel = currentWheel.value
        soundHapticManager.playCelebration(
            scope = viewModelScope,
            soundEnabled = wheel?.soundEnabled ?: true,
            hapticsEnabled = wheel?.hapticsEnabled ?: true
        )

        val wheelId = _selectedWheelId.value ?: return
        viewModelScope.launch {
            repository.recordWinner(
                wheelId = wheelId,
                winnerName = winner.name,
                autoRemoveWinner = wheel?.autoRemoveWinner ?: false,
                winningItemId = winner.id
            )

            // Also record to Firestore if user is authenticated
            val uid = _currentUser.value?.uid
            if (uid != null) {
                try {
                    firestoreRepository.recordSpin(
                        userId = uid,
                        wheelId = wheelId.toString(),
                        winnerName = winner.name,
                        autoRemoveWinner = wheel?.autoRemoveWinner ?: false,
                        winningItemId = winner.id.toString()
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Cloud history recording skipped: ${e.message}")
                }
            }
        }
    }

    fun dismissWinnerDialog() {
        _showWinnerDialog.value = false
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        soundHapticManager.release()
    }
}
