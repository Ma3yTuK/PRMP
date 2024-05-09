package com.example.rpmp

import android.content.res.Configuration
import android.graphics.Path.Op
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rpmp.ui.theme.RPMPTheme


enum class Numbers(val value: String) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9")
}

const val POINT = "."
const val RESET = "C"
const val BACK = "<-"
const val CALC = "="

const val CODE = "PiPi"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RPMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CalculatorLayout()
                }
            }
        }
    }
}

@Composable
fun CalculatorLayout(viewModel: MyViewModel = viewModel()) {
    var devMode by remember { mutableStateOf(false) }
    var scrollStateValue by remember {
        mutableStateOf(ScrollState(Int.MAX_VALUE))
    }

    val calcObjects = viewModel.uiState.calcObjects
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val onUpdateValueInsert = fun(it: CalcObject) {
        var offset = 0

        if (textFieldValue.selection.end == 0) {
            calcObjects.add(0, it)
        } else {
            for (index in 0 until calcObjects.size) {
                val calcObject = calcObjects[index]

                if (textFieldValue.selection.end > offset && textFieldValue.selection.end <= offset + calcObject.value.length) {
                    if (calcObject is Operand) {
                        val localOffset = textFieldValue.selection.end - offset
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
                        offset = textFieldValue.selection.end
                    } else {
                        offset += calcObject.value.length
                        calcObjects.add(index + 1, it)
                    }
                    break
                }

                offset += calcObject.value.length
            }
        }

        textFieldValue = textFieldValue.copy(
            text = textFieldValue.text.substring(0, offset) + it.value + textFieldValue.text.substring(offset),
            selection = TextRange(offset + it.value.length)
        )

        if (textFieldValue.selection.end == textFieldValue.text.length) {
            scrollStateValue = ScrollState(Int.MAX_VALUE)
        }

        if (textFieldValue.text == CODE) {
            devMode = !devMode
        }

        viewModel.calculate(calcObjects.toList())
    }

    val onUpdateValueBack = fun() {
        if (textFieldValue.text.isNotEmpty() && textFieldValue.selection.end > 0) {
            var offset = 0
            var size = 0

            for (index in 0 until calcObjects.size) {
                val calcObject = calcObjects[index]

                if (textFieldValue.selection.end > offset && textFieldValue.selection.end <= offset + calcObject.value.length) {
                    if (calcObject is Operand) {
                        size = 1
                        val localOffset = textFieldValue.selection.end - offset
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
                        offset = textFieldValue.selection.end
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

            textFieldValue = textFieldValue.copy(
                text = calcObjects.reduceOrNull { acc, calcObject -> Operand(acc.value + calcObject.value) }?.value ?: "",
                selection = TextRange(offset - size)
            )

            viewModel.calculate(calcObjects.toList())
        }
    }

    val onUpdateValueReset = fun() {
        textFieldValue = TextFieldValue("")
        calcObjects.clear()
        viewModel.calculate(calcObjects.toList())
    }

    val onCalculate = fun() {
        if (viewModel.uiState.result.isNotEmpty() && NUMBER_REGEX.matches(viewModel.uiState.result)) {
            textFieldValue = TextFieldValue(text = viewModel.uiState.result, selection = TextRange(viewModel.uiState.result.length))
            calcObjects.clear()
            calcObjects.add(Operand(textFieldValue.text))
            scrollStateValue = ScrollState(Int.MAX_VALUE)
        }
    }

    val onUpdateSettings = { precision: Int, width: Int -> viewModel.updateCalculator(precision, width) }

    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            CompositionLocalProvider(LocalTextInputService provides null) {
                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollStateValue),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Right,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
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
                textFieldValue = textFieldValue,
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
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    maxLines = 1,
                    modifier = Modifier
                        .weight(0.2f)
                        .fillMaxWidth()
                        .horizontalScroll(scrollStateValue),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Right,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
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
                textFieldValue = textFieldValue,
                onUpdateValueInsert = onUpdateValueInsert,
                onUpdateValueBack = onUpdateValueBack,
                onUpdateValueReset = onUpdateValueReset,
                onCalculate = onCalculate,
                onUpdateSettings = onUpdateSettings,
                currentPrecision = viewModel.uiState.precision,
                currentWidth = viewModel.uiState.width,
                devMode = devMode,
                modifier = Modifier
                    .weight(0.7f)
            )
        }
    }
}

@Composable
fun VerticalLayout(textFieldValue: TextFieldValue, onUpdateValueInsert: (CalcObject) -> Unit, onUpdateValueBack: () -> Unit, onUpdateValueReset: () -> Unit, onCalculate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = onUpdateValueReset,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = RESET, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = onUpdateValueBack,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = BACK, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.SEVEN.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.SEVEN.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.EIGHT.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.EIGHT.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.NINE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.NINE.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.ADD) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.ADD.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.FOUR.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.FOUR.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.FIVE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.FIVE.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.SIX.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.SIX.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.SUB) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.SUB.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.ONE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.ONE.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.TWO.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.TWO.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.THREE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.THREE.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.MUL) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.MUL.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = CALC, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.ZERO.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.ZERO.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(POINT)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = POINT, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.DIV) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.DIV.value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun HorizontalLayout(textFieldValue: TextFieldValue, onUpdateValueInsert: (CalcObject) -> Unit, onUpdateValueBack: () -> Unit, onUpdateValueReset: () -> Unit, onCalculate: () -> Unit, onUpdateSettings: (Int, Int) -> Unit, currentPrecision: Int, currentWidth: Int, devMode: Boolean, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (devMode && (textFieldValue.text.toIntOrNull() ?: 0) > 1) {
                Button(
                    onClick = { onUpdateSettings(textFieldValue.text.toInt(), currentWidth) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp, 1.dp)
                        .weight(1f)
                ) {
                    Text(text = "SetP", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                }
                Button(
                    onClick = { onUpdateSettings(currentPrecision, textFieldValue.text.toInt()) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp, 1.dp)
                        .weight(1f)
                ) {
                    Text(text = "SetW", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                }
            }

            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = CALC, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = onUpdateValueReset,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = RESET, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = onUpdateValueBack,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = BACK, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onUpdateValueInsert(NonBinaryOperation.PI) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = NonBinaryOperation.PI.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(NonBinaryOperation.PAR_O) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = NonBinaryOperation.PAR_O.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(NonBinaryOperation.PAR_C) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = NonBinaryOperation.PAR_C.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.SEVEN.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.SEVEN.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.EIGHT.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.EIGHT.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.NINE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.NINE.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.ADD) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.ADD.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onUpdateValueInsert(NonBinaryOperation.E) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = NonBinaryOperation.E.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.POW) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.POW.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.LOG) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.LOG.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.FOUR.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.FOUR.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.FIVE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.FIVE.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.SIX.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.SIX.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.SUB) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.SUB.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onUpdateValueInsert(UnaryOperation.SIN) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = UnaryOperation.SIN.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(UnaryOperation.COS) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = UnaryOperation.COS.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(UnaryOperation.TAN) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = UnaryOperation.TAN.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.ONE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.ONE.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.TWO.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.TWO.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.THREE.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.THREE.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.MUL) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.MUL.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onUpdateValueInsert(UnaryOperation.ASIN) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = UnaryOperation.ASIN.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(UnaryOperation.ACOS) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = UnaryOperation.ACOS.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(UnaryOperation.ATAN) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = UnaryOperation.ATAN.value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(NonBinaryOperation.FACT) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = NonBinaryOperation.FACT.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(Numbers.ZERO.value)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = Numbers.ZERO.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(Operand(POINT)) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = POINT, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            Button(
                onClick = { onUpdateValueInsert(BinaryOperation.DIV) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp, 1.dp)
                    .weight(1f)
            ) {
                Text(text = BinaryOperation.DIV.value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}