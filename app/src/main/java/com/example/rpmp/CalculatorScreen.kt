package com.example.rpmp

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalculatorScreen(onScan: () -> Unit, onHist: () -> Unit, viewModel: MyViewModel = viewModel()) {
    val calcObjects = viewModel.uiState.calcObjects
    val currentValue = viewModel.uiState.text
    val isBroken = viewModel.uiState.isBroken

    var scrollStateValue by remember(viewModel.uiState.recomposeKey) {
        mutableStateOf(ScrollState(Int.MAX_VALUE))
    }
    var textRange by remember(viewModel.uiState.recomposeKey) {
        mutableStateOf(TextRange(currentValue.length))
    }

    val onUpdateValueInsert = fun(it: CalcObject) {
        var offset = 0

        if (viewModel.uiState.isBroken) {
            if (textRange.end == currentValue.length)
                scrollStateValue = ScrollState(Int.MAX_VALUE)

            viewModel.setValue(currentValue.substring(0, textRange.end) + it.value + currentValue.substring(textRange.end))
            textRange = TextRange(textRange.end + it.value.length)
            viewModel.calculate(viewModel.uiState.calcObjects)
            return
        }

        if (textRange.end == 0) {
            if (calcObjects.isNotEmpty() && it is Operand && calcObjects.first() is Operand) {
                calcObjects.add(0, Operand(it.value + calcObjects.removeAt(0).value))
            } else {
                calcObjects.add(0, it)
            }
        } else {
            for (index in 0 until calcObjects.size) {
                val calcObject = calcObjects[index]

                if (textRange.end > offset && textRange.end <= offset + calcObject.value.length) {
                    val atEnd = index == calcObjects.size - 1 && textRange.end - offset == calcObject.value.length
                    if (calcObject is Operand) {
                        val localOffset = textRange.end - offset
                        if (it is Operand) {
                            val newOperand = Operand(
                                calcObject.value.substring(
                                    0,
                                    localOffset
                                ) + it.value + calcObject.value.substring(localOffset)
                            )

                            if (!NUMBER_REGEX.matches(newOperand.value))
                                return

                            calcObjects.removeAt(index)
                            calcObjects.add(index, newOperand)
                        } else {
                            val newOperand1 = Operand(calcObject.value.substring(0, localOffset))
                            val newOperand2 = Operand(calcObject.value.substring(localOffset))

                            if (!NUMBER_REGEX.matches(newOperand1.value))
                                return

                            if (newOperand2.value.isNotEmpty()) {
                                if (!NUMBER_REGEX.matches(newOperand2.value))
                                    return
                                calcObjects.add(index + 1, newOperand2)
                            }

                            calcObjects.removeAt(index)
                            calcObjects.add(index, it)
                            calcObjects.add(index, newOperand1)
                        }
                        offset = textRange.end
                    } else {
                        offset += calcObject.value.length
                        calcObjects.add(index + 1, it)
                    }

                    if (atEnd)
                        scrollStateValue = ScrollState(Int.MAX_VALUE)

                    break
                }

                offset += calcObject.value.length
            }
        }

        viewModel.setText(calcObjects.reduceOrNull { acc, value -> Operand(acc.value + value.value) }?.value ?: "")
        textRange = TextRange(offset + it.value.length)

        viewModel.calculate(calcObjects.toList())
    }

    val onUpdateValueBack = fun() {
        if (textRange.end > 0) {

            if (viewModel.uiState.isBroken) {
                viewModel.setValue(currentValue.substring(0, textRange.end - 1) + currentValue.substring(textRange.end))
                textRange = TextRange(textRange.end - 1)
                viewModel.calculate(viewModel.uiState.calcObjects)
                return
            }

            var offset = 0
            var size = 0

            for (index in 0 until calcObjects.size) {
                val calcObject = calcObjects[index]

                if (textRange.end > offset && textRange.end <= offset + calcObject.value.length) {
                    if (calcObject is Operand) {
                        size = 1
                        val localOffset = textRange.end - offset
                        val oldOperand = calcObjects.removeAt(index)
                        val newOperand = Operand(oldOperand.value.substring(0, localOffset - size) + oldOperand.value.substring(localOffset))
                        if (newOperand.value.isNotEmpty()) {
                            if (!NUMBER_REGEX.matches(newOperand.value)) {
                                size = oldOperand.value.length
                                offset += oldOperand.value.length
                                break
                            }
                            calcObjects.add(index, newOperand)
                        }
                        offset = textRange.end
                    } else {
                        size = calcObject.value.length
                        offset += calcObject.value.length
                        calcObjects.removeAt(index)
                        if (index > 0 && index < calcObjects.size && calcObjects[index-1] is Operand && calcObjects[index] is Operand) {
                            val oldOperand1 = calcObjects.removeAt(index - 1).value
                            var oldOperand2 = calcObjects.removeAt(index - 1).value
                            if (oldOperand1.contains("."))
                                oldOperand2 = oldOperand2.replace(".", "")
                            calcObjects.add(index - 1, Operand(oldOperand1 + oldOperand2))
                        }
                    }
                    break
                }

                offset += calcObject.value.length
            }

            viewModel.setText(calcObjects.reduceOrNull { acc, value -> Operand(acc.value + value.value) }?.value ?: "")
            textRange = TextRange(offset - size)

            viewModel.calculate(calcObjects.toList())
        }
    }

    val onUpdateValueReset = fun() {
        textRange = TextRange(0)
        viewModel.setValue("")
    }

    val onCalculate = fun() {
        if (!isBroken && viewModel.uiState.result.isNotEmpty() && NUMBER_REGEX.matches(viewModel.uiState.result)) {
            textRange = TextRange(viewModel.uiState.result.length)
            calcObjects.clear()
            calcObjects.add(Operand(viewModel.uiState.result))
            viewModel.setText(calcObjects.reduceOrNull { acc, value -> Operand(acc.value + value.value) }?.value ?: "")
            scrollStateValue = ScrollState(Int.MAX_VALUE)
        }
    }

    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            TopBar(
                onScan = onScan,
                onHist = onHist,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
                    .padding(5.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CompositionLocalProvider(LocalTextInputService provides null) {
                TextField(
                    value = TextFieldValue(text = currentValue, selection = textRange),
                    onValueChange = { textRange = it.selection },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollStateValue),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Right,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        color = if (isBroken) Color.Red else Color.Unspecified
                    )
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = viewModel.uiState.result,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(ScrollState(Int.MAX_VALUE)),
                textAlign = TextAlign.End,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            VerticalLayout(
                onUpdateValueInsert = onUpdateValueInsert,
                onUpdateValueBack = onUpdateValueBack,
                onUpdateValueReset = onUpdateValueReset,
                onCalculate = onCalculate
            )
        }
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            CompositionLocalProvider(LocalTextInputService provides null) {
                TextField(
                    value = TextFieldValue(text = currentValue, selection = textRange),
                    onValueChange = { textRange = it.selection },
                    maxLines = 1,
                    modifier = Modifier
                        .weight(0.2f)
                        .fillMaxWidth()
                        .horizontalScroll(scrollStateValue),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Right,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = if (isBroken) Color.Red else Color.Unspecified
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = viewModel.uiState.result,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(ScrollState(Int.MAX_VALUE))
                    .weight(0.08f),
                textAlign = TextAlign.End,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            HorizontalLayout(
                onUpdateValueInsert = onUpdateValueInsert,
                onUpdateValueBack = onUpdateValueBack,
                onUpdateValueReset = onUpdateValueReset,
                onCalculate = onCalculate,
                modifier = Modifier
                    .weight(0.7f)
            )
        }
    }
}