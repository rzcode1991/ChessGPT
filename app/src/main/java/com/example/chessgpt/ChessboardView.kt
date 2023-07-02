package com.example.chessgpt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect


class ChessboardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var chessboard: Chessboard? = null
    private val numRows = 8
    private val numColumns = 8
    private var cellSize = 0f
    private val chessPiecePositions: MutableMap<Coordinate, Int?> = mutableMapOf()
    private var pieceSize = 0f

    private val pieceDrawables: Map<PieceType, Int> = mapOf(
        PieceType.WHITE_PAWN to R.drawable.white_pawn,
        PieceType.BLACK_PAWN to R.drawable.black_pawn,
        PieceType.WHITE_ROOK to R.drawable.white_rook,
        PieceType.BLACK_ROOK to R.drawable.black_rook,
        PieceType.WHITE_KNIGHT to R.drawable.white_knight,
        PieceType.BLACK_KNIGHT to R.drawable.black_knight,
        PieceType.WHITE_BISHOP to R.drawable.white_bishop,
        PieceType.BLACK_BISHOP to R.drawable.black_bishop,
        PieceType.WHITE_QUEEN to R.drawable.white_queen,
        PieceType.BLACK_QUEEN to R.drawable.black_queen,
        PieceType.WHITE_KING to R.drawable.white_king,
        PieceType.BLACK_KING to R.drawable.black_king
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // Calculate the size of each cell based on the available width and height
        val size = measuredWidth.coerceAtMost(measuredHeight)
        cellSize = size.toFloat() / numRows.coerceAtLeast(numColumns)
        pieceSize = cellSize * 0.8f
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Custom drawing code for the chessboard and pieces
        drawChessboard(canvas)
        drawChessPieces(canvas)
    }


    private fun drawChessboard(canvas: Canvas) {
        val darkCellColor = ContextCompat.getColor(context, R.color.darkCellColor)
        val lightCellColor = ContextCompat.getColor(context, R.color.lightCellColor)

        val paint = Paint()
        paint.style = Paint.Style.FILL

        // Iterate over each row and column to draw the cells
        for (row in 0 until numRows) {
            for (column in 0 until numColumns) {
                val cellLeft = column * cellSize
                val cellTop = row * cellSize
                val cellRight = cellLeft + cellSize
                val cellBottom = cellTop + cellSize

                // Determine the color for the current cell based on row and column indices
                val cellColor = if ((row + column) % 2 == 0) lightCellColor else darkCellColor

                // Set the color of the paint object
                paint.color = cellColor

                // Draw the cell rectangle using canvas.drawRect()
                canvas.drawRect(cellLeft, cellTop, cellRight, cellBottom, paint)
            }
        }
    }

    private fun drawChessPieces(canvas: Canvas) {
        // Iterate over the chess piece positions and draw each piece on the canvas
        for ((coordinate, drawableResId) in chessPiecePositions) {
            val row = coordinate.row
            val column = coordinate.col

            // Calculate the position of the chess piece within the cell
            val pieceLeft = column * cellSize + (cellSize - pieceSize) / 2
            val pieceTop = row * cellSize + (cellSize - pieceSize) / 2
            val pieceRight = pieceLeft + pieceSize
            val pieceBottom = pieceTop + pieceSize

            // Retrieve the drawable resource ID for the piece
            val drawable = drawableResId?.let { ContextCompat.getDrawable(context, it) }

            // Only draw the chess piece if the drawable is not null
            drawable?.let {
                // Draw the chess piece drawable on the canvas
                it.bounds = RectF(pieceLeft, pieceTop, pieceRight, pieceBottom).toRect()
                it.draw(canvas)
            }
        }
    }



    private fun getDrawableResIdForPiece(pieceType: PieceType): Int? {
        return when (pieceType) {
            PieceType.WHITE_PAWN -> R.drawable.white_pawn
            PieceType.BLACK_PAWN -> R.drawable.black_pawn
            PieceType.WHITE_ROOK -> R.drawable.white_rook
            PieceType.BLACK_ROOK -> R.drawable.black_rook
            PieceType.WHITE_KNIGHT -> R.drawable.white_knight
            PieceType.BLACK_KNIGHT -> R.drawable.black_knight
            PieceType.WHITE_BISHOP -> R.drawable.white_bishop
            PieceType.BLACK_BISHOP -> R.drawable.black_bishop
            PieceType.WHITE_QUEEN -> R.drawable.white_queen
            PieceType.BLACK_QUEEN -> R.drawable.black_queen
            PieceType.WHITE_KING -> R.drawable.white_king
            PieceType.BLACK_KING -> R.drawable.black_king
        }
    }

    private fun createPiece(pieceType: PieceType): Piece {
        return when (pieceType) {
            PieceType.WHITE_PAWN, PieceType.BLACK_PAWN -> Pawn(pieceType)
            PieceType.WHITE_ROOK, PieceType.BLACK_ROOK -> Rook(pieceType)
            PieceType.WHITE_KNIGHT, PieceType.BLACK_KNIGHT -> Knight(pieceType)
            PieceType.WHITE_BISHOP, PieceType.BLACK_BISHOP -> Bishop(pieceType)
            PieceType.WHITE_QUEEN, PieceType.BLACK_QUEEN -> Queen(pieceType)
            PieceType.WHITE_KING, PieceType.BLACK_KING -> King(pieceType)
        }
    }

    fun loadDrawables(context: Context) {
        for (pieceType in PieceType.values()) {
            val drawableResId = getDrawableResIdForPiece(pieceType)
            val drawable = drawableResId?.let {
                ContextCompat.getDrawable(context, it)
            }
            val piece = createPiece(pieceType)
            drawable?.let {
                piece.loadDrawable(it)
            }
        }
    }

    fun updateChessboard(chessboard: Chessboard) {
        loadDrawables(context) // Load the drawable resources for the chess pieces

        this.chessboard = chessboard
        chessPiecePositions.clear()

        val emptySquares = chessboard.getEmptySquares()

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val coordinate = Coordinate(row, col)
                val piece = chessboard.getPiece(coordinate)
                val drawableResId = if (piece != null) {
                    pieceDrawables[piece.type]
                } else {
                    null
                }
                chessPiecePositions[coordinate] = drawableResId
            }
        }

        for (coordinate in emptySquares) {
            chessPiecePositions[coordinate] = null
        }

        invalidate()
    }



}