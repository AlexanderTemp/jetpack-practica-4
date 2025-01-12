package com.example.watercoutner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.example.watercoutner.ui.theme.WaterCoutnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaterCoutnerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(Modifier.padding(innerPadding)) {
                        GraphicDraw()
                    }
                }
            }
        }
    }
}

data class Balance(
    val x: Float,
    val y: Float
)

val graphData: List<Balance> = listOf(
    Balance(5.1f, 3.8f),
    Balance(7.7f, 3.4f),
    Balance(11.0f, 4.5f),
    Balance(13.8f, 9.2f),
    Balance(18.3f, 6.7f),
    Balance(20.6f, 9.5f),
    Balance(25.0f, 7.8f),
    Balance(35.0f, 14.0f),
    Balance(40.0f, 10.5f),
    Balance(55.0f, 13.9f),
    Balance(70.0f, 10.0f)
)

fun generatePath(data: List<Balance>, size: Size): Path {
    val minX = data.minOf { it.x }
    val maxX = data.maxOf { it.x }
    val minY = data.minOf { it.y }
    val maxY = data.maxOf { it.y }

    val path = Path()

    data.forEachIndexed { i, balance ->
        val normalizedX = (balance.x - minX) / (maxX - minX) * size.width
        val normalizedY = size.height - (balance.y - minY) / (maxY - minY) * size.height

        if (i == 0) {
            path.moveTo(normalizedX, normalizedY)
        } else {
            path.lineTo(normalizedX, normalizedY)
        }
    }

    return path
}

fun generateCurvePath(data: List<Balance>, size: Size): Path {
    val minX = data.minOf { it.x }
    val maxX = data.maxOf { it.x }
    val minY = data.minOf { it.y }
    val maxY = data.maxOf { it.y }

    val path = Path()

    val points = data.map { balance ->
        val normalizedX = (balance.x - minX) / (maxX - minX) * size.width
        val normalizedY = size.height - (balance.y - minY) / (maxY - minY) * size.height
        Offset(normalizedX, normalizedY)
    }

    // Generate the smooth path
    points.forEachIndexed { i, point ->
        if (i == 0) {
            path.moveTo(point.x, point.y)
        } else {
            // Use cubicTo for a smooth curve
            val prevPoint = points[i - 1]
            val controlPoint1 =
                Offset((prevPoint.x + point.x) / 2, prevPoint.y)
            val controlPoint2 = Offset((prevPoint.x + point.x) / 2, point.y)
            path.cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                point.x, point.y
            )
        }
    }

    return path
}

@Composable
fun GraphicDraw(modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.outline

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = graphData, block = {
        animationProgress.animateTo(1f, tween(3000))
    })

    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF222222)), contentAlignment = Alignment.Center
    ) {
        Spacer(
            modifier = Modifier
                .padding(8.dp)
                .aspectRatio(3 / 2f)
                .fillMaxSize()
                .drawWithCache {
                    val path = generateCurvePath(graphData, size)

                    val filledPath = Path()
                    filledPath.addPath(path)
                    filledPath.lineTo(size.width, size.height)
                    filledPath.lineTo(0f, size.height)
                    filledPath.close()

                    val brush = Brush.verticalGradient(
                        listOf(
                            Color.Green.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )

                    onDrawBehind {
                        val barWidthPx = 1.dp.toPx()
                        drawRect(barColor, style = Stroke(barWidthPx))

                        val verticalLines = 4
                        val verticalSize = size.width / (verticalLines + 1)
                        repeat(verticalLines) { i ->
                            val startX = verticalSize * (i + 1)
                            drawLine(
                                barColor,
                                start = Offset(startX, 0f),
                                end = Offset(startX, size.height),
                                strokeWidth = barWidthPx
                            )
                        }

                        val horizontalLines = 3
                        val horizontalSize = size.height / (horizontalLines + 1)
                        repeat(horizontalLines) { i ->
                            val startY = horizontalSize * (i + 1)
                            drawLine(
                                barColor,
                                start = Offset(0f, startY),
                                end = Offset(size.width, startY),
                                strokeWidth = barWidthPx

                            )

                        }

                        clipRect(right = size.width * animationProgress.value) {
                            drawPath(path, Color.Green, style = Stroke(2.dp.toPx()))
                            drawPath(filledPath, brush, style = Fill)
                        }
                    }
                }
        )
    }
}

@Composable
fun EjemploGridSolo(modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.outline

    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .padding(8.dp)
                .aspectRatio(3 / 2f)
                .fillMaxSize()
        ) {
            val barWidthPx = 1.dp.toPx()
            drawRect(barColor, style = Stroke(barWidthPx))

            val verticalLines = 4
            val verticalSize = size.width / (verticalLines + 1)
            repeat(verticalLines) { i ->
                val startX = verticalSize * (i + 1)
                drawLine(
                    barColor,
                    start = Offset(startX, 0f),
                    end = Offset(startX, size.height),
                    strokeWidth = barWidthPx
                )
            }

            val horizontalLines = 3
            val horizontalSize = size.height / (horizontalLines + 1)
            repeat(horizontalLines) { i ->
                val startY = horizontalSize * (i + 1)
                drawLine(
                    barColor,
                    start = Offset(0f, startY),
                    end = Offset(size.width, startY),
                    strokeWidth = barWidthPx

                )

            }


            // Chart logic


        }

    }
}