package com.alphadoer.trader.presentation.viewmodel.process

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.usecase.process.ProcessManager
import kotlinx.coroutines.launch

/**
 * 步骤基础ViewModel
 * 注意：抽象类不能使用@Inject，子类需要自己处理依赖注入
 */
abstract class BaseStepViewModel(
    protected val processManager: ProcessManager
) : ViewModel() {
    
    protected var currentStepId: String? = null
    
    fun setStepId(stepId: String) {
        currentStepId = stepId
    }
    
    fun completeStep(notes: String? = null, data: Map<String, String>? = null) {
        currentStepId?.let { stepId ->
            viewModelScope.launch {
                processManager.completeStep(stepId, notes, data)
            }
        }
    }
    
    fun skipStep(reason: String? = null) {
        currentStepId?.let { stepId ->
            viewModelScope.launch {
                processManager.skipStep(stepId, reason)
            }
        }
    }
}
