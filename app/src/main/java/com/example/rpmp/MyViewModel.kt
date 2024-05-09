package com.example.rpmp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


data class UiState (
    val result: String = "",
    val precision: Int = 128,
    val width: Int = 4096,
    val calcObjects: MutableList<CalcObject> = mutableListOf()
)


class MyViewModel : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private var lastThread: Thread? = null
    private var calculator = Calculator(uiState.precision, uiState.width)

    fun updateCalculator(precision: Int, width: Int) {
        lastThread?.interrupt()
        if (precision > 1 && width > 1) {
            calculator = Calculator(precision, width)
            uiState = uiState.copy(precision = precision, width = width)
        }
    }

    fun calculate(calcObjects: List<CalcObject>) {
        lastThread?.interrupt()
        uiState = uiState.copy(result = "")

        val callerThread = Thread.currentThread()

        lastThread = Thread {
            var newString: String

            try {
                newString = calculator.calc(calcObjects)
            } catch (e: InterruptedException) {
                return@Thread
            } catch (e: Exception) {
                newString = "Error"
            }

            val calledThread = Thread.currentThread()

            callerThread.run {
                if (!calledThread.isInterrupted)
                    uiState = uiState.copy(result = newString)
            }
        }
        lastThread?.start()
    }
}