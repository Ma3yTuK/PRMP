package com.example.rpmp

import android.graphics.*
import android.graphics.drawable.Drawable
import com.google.mlkit.vision.text.Text


class ScannerDrawable(private val blocks: List<Text.TextBlock>) : Drawable() {
    private val backgroudPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.TRANSPARENT
        alpha = 150
    }

    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.FILL
        blendMode = BlendMode.CLEAR
        alpha = 255
    }

    private val contentRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2F
        alpha = 150
    }

//    override fun draw(canvas: Canvas) {
//        canvas.drawRect(canvas.clipBounds, backgroudPaint)
//        for (block in blocks) {
//            for (line in block.lines) {
//                val matches = SEARCH_REGEX.findAll(line.text)
//
//                var currentElementIndex = 0
//                var currentSymbolIndex = 0
//                var currentCharacterIndex = 0
//
//                val startText = NON_WORD_REGEX.matchAt(line.text, currentCharacterIndex)?.value ?: ""
//
//                for (match in matches) {
//                    var rect: Rect? = null
//                    while (currentCharacterIndex != startText.length) {
//                        if (currentCharacterIndex == match.range.first) {
//                            rect = line.boundingBox!!
//                        }
//                        if (currentCharacterIndex == match.range.last) {
//                            if (line.elements.isNotEmpty())
//                                rect!!.right = line.elements.first().boundingBox!!.left
//                            else
//                                rect!!.right = line.boundingBox!!.right
//                            break
//                        }
//                        currentCharacterIndex++
//                    }
//                    if (currentCharacterIndex >= startText.length) {
//                        match_loop@ while (currentElementIndex < line.elements.size) {
//                            val element = line.elements[currentElementIndex]
//                            if (rect == null && currentCharacterIndex + element.text.length - currentSymbolIndex > match.range.first || rect != null && currentCharacterIndex + element.text.length > match.range.last) {
//                                while (currentSymbolIndex < element.symbols.size) {
//                                    val symbol = element.symbols[currentSymbolIndex]
//                                    if (currentCharacterIndex == match.range.first) {
//                                        rect = symbol.boundingBox!!
//                                    }
//                                    if (currentCharacterIndex == match.range.last) {
//                                        rect!!.right = symbol.boundingBox!!.right
//                                        break@match_loop
//                                    }
//                                    currentCharacterIndex++
//                                    currentSymbolIndex++
//                                }
//                            } else {
//                                currentCharacterIndex += element.symbols.size - currentSymbolIndex
//                            }
//
//                            val notWord = NON_WORD_REGEX.matchAt(line.text, currentCharacterIndex)
//                            for (character in notWord?.value ?: "") {
//                                if (currentCharacterIndex == match.range.first) {
//                                    rect = element.boundingBox!!
//                                    rect.left = rect.right
//                                }
//                                if (currentCharacterIndex == match.range.last) {
//                                    if (currentElementIndex + 1 == line.elements.size)
//                                        rect!!.right = line.boundingBox!!.right
//                                    else
//                                        rect!!.right = line.elements[currentElementIndex + 1].boundingBox!!.left
//                                    break@match_loop
//                                }
//                                currentCharacterIndex++
//                            }
//
//                            currentSymbolIndex = 0
//                            currentElementIndex++
//                        }
//                    }
//
//                    canvas.drawRect(rect!!, boundingRectPaint)
//                    canvas.drawRect(rect, contentRectPaint)
//                }
//            }
//        }
//    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(canvas.clipBounds, backgroudPaint)
        for (block in blocks) {
            for (line in block.lines) {
                canvas.drawRect(line.boundingBox!!, boundingRectPaint)
                canvas.drawRect(line.boundingBox!!, contentRectPaint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        boundingRectPaint.alpha = alpha
        contentRectPaint.alpha = alpha
    }

    override fun setColorFilter(colorFiter: ColorFilter?) {
        boundingRectPaint.colorFilter = colorFilter
        contentRectPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}