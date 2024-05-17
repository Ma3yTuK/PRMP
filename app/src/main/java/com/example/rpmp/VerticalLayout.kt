package com.example.rpmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VerticalLayout(onUpdateValueInsert: (CalcObject) -> Unit, onUpdateValueBack: () -> Unit, onUpdateValueReset: () -> Unit, onCalculate: () -> Unit, modifier: Modifier = Modifier) {
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