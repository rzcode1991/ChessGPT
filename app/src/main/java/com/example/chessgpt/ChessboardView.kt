package com.example.chessgpt

import android.util.Log
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChessboardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var chessboard: Chessboard? = null
    private val numRows = 8
    private val numColumns = 8
    private var cellSize = 0f
    private val chessPiecePositions: MutableMap<Coordinate, Int?> = mutableMapOf()
    private var pieceSize = 0f
    private var selectedPiece: Piece? = null
    private var selectedPieceCoordinate: Coordinate? = null
    private var highlightedCoordinate: Coordinate? = null
    private var aiPlayer: ChatGPTAIPlayer? = null
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private var moveNumber = 1
    private val moveHistory: MutableList<String> = mutableListOf()



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



    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.x
        val y = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // User touched down on the chessboard
                handleTouchDown(x, y)
            }
            MotionEvent.ACTION_UP -> {
                // User released touch on the chessboard
                handleTouchUp(x, y)
            }
        }

        // Return true to indicate that the event has been handled
        return true
    }


    private fun handleTouchDown(x: Float, y: Float) {
        val coordinate = getCoordinateFromPosition(x, y)
        val piece = chessboard?.getPiece(coordinate)

        if (piece != null && selectedPiece == null) {
            // Select the piece and highlight the square
            selectedPiece = piece
            selectedPieceCoordinate = coordinate
            invalidate() // Redraw the chessboard to highlight the selected square
        }
    }



    fun initAIPlayer(apiKey: String) {
        aiPlayer = ChatGPTAIPlayer(apiKey)
    }



    private fun handleTouchUp(x: Float, y: Float) {
        if (selectedPiece != null && selectedPieceCoordinate != null) {
            val sourceCoordinate = selectedPieceCoordinate
            val destinationCoordinate = getCoordinateFromPosition(x, y)

            if (sourceCoordinate != null && destinationCoordinate != null) {
                val isMoveValid = chessboard?.isMoveValid(selectedPiece!!, sourceCoordinate, destinationCoordinate)

                if (isMoveValid == true) {
                    val targetPiece = chessboard?.getPiece(destinationCoordinate)

                    if (targetPiece == null || targetPiece.pieceColor() != selectedPiece!!.pieceColor()) {
                        // Move the piece to the valid destination
                        chessboard?.movePiece(sourceCoordinate, destinationCoordinate)
                        invalidate() // Redraw the chessboard to reflect the updated position

                        // Add algebraic notation of the move to the moveHistory list
                        val attackingPiece = selectedPiece?.getPieceNotation() ?: "" // Get the notation of the attacking piece
                        moveNumber++
                        val moveNotation = if (moveNumber % 2 == 0) {
                            "${moveNumber / 2}. $attackingPiece${if(targetPiece != null) "x" else ""}${destinationCoordinate.getAlgebraicNotation()}"
                        } else {
                            "$attackingPiece${if(targetPiece != null) "x" else ""}${destinationCoordinate.getAlgebraicNotation()}"
                        }
                        moveHistory.add(moveNotation)
                    }
                }
            }

            // Reset the selected piece and coordinate
            selectedPiece = null
            selectedPieceCoordinate = null

            // Highlight the selected piece's coordinate
            highlightedCoordinate = sourceCoordinate

            invalidate() // Redraw the chessboard to highlight the selected piece

            // Trigger the AI player to suggest its move
            coroutineScope.launch {

                val boardState = moveHistory.joinToString(" ")

                Log.d("Board State", "Previous Moves: $boardState")

                // Call suggestMove with boardState
                val aiMove = aiPlayer?.suggestMove(boardState)

                if (aiMove != null) {
                    Log.d("AI Move", "Suggested Move: $aiMove")
                    // Process the AI move and update the chessboard state
                    // ...

                    // Redraw the chessboard
                    invalidate()
                }
            }

        }
    }






    private fun getCoordinateFromPosition(x: Float, y: Float): Coordinate {
        val col = (x / cellSize).toInt()
        val row = (y / cellSize).toInt()
        return Coordinate(row, col)
    }



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
        drawChessboard(canvas)
        drawChessPieces(canvas)

        // Highlight the selected square if a piece is selected
        if (selectedPieceCoordinate != null) {
            val selectedCellLeft = selectedPieceCoordinate!!.col * cellSize
            val selectedCellTop = selectedPieceCoordinate!!.row * cellSize
            val selectedCellRight = selectedCellLeft + cellSize
            val selectedCellBottom = selectedCellTop + cellSize

            val highlightColor = ContextCompat.getColor(context, R.color.highlightColor)
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = highlightColor
            canvas.drawRect(
                selectedCellLeft,
                selectedCellTop,
                selectedCellRight,
                selectedCellBottom,
                paint
            )
        }
    }




    private fun drawChessboard(canvas: Canvas) {
        val darkCellColor = ContextCompat.getColor(context, R.color.darkCellColor)
        val lightCellColor = ContextCompat.getColor(context, R.color.lightCellColor)
        val highlightColor = ContextCompat.getColor(context, R.color.highlightColor)

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

                if (highlightedCoordinate != null && highlightedCoordinate == Coordinate(row, column)) {
                    // Draw the highlight color around the selected square
                    paint.color = highlightColor
                    val highlightPadding = cellSize * 0.005f
                    canvas.drawRect(
                        cellLeft - highlightPadding,
                        cellTop - highlightPadding,
                        cellRight + highlightPadding,
                        cellBottom + highlightPadding,
                        paint
                    )
                }
            }
        }
    }


    private fun drawChessPieces(canvas: Canvas) {
        chessboard?.let {
            for (row in 0 until numRows) {
                for (col in 0 until numColumns) {
                    val coordinate = Coordinate(row, col)
                    val piece = it.getPiece(coordinate)

                    if (piece != null) {
                        // Calculate the position of the chess piece within the cell
                        val cellSize = measuredWidth.toFloat() / numColumns.coerceAtLeast(numRows)
                        val pieceLeft = col * cellSize + (cellSize - pieceSize) / 2
                        val pieceTop = row * cellSize + (cellSize - pieceSize) / 2
                        val pieceRight = pieceLeft + pieceSize
                        val pieceBottom = pieceTop + pieceSize

                        // Draw the chess piece on the canvas
                        val drawableResId = pieceDrawables[piece.type]
                        val drawable = drawableResId?.let { ContextCompat.getDrawable(context, it) }
                        drawable?.let {
                            it.bounds = RectF(pieceLeft, pieceTop, pieceRight, pieceBottom).toRect()
                            it.draw(canvas)
                        }
                    }
                }
            }
        }
    }





    // Helper extension function to convert RectF to Rect
    private fun RectF.toRect(): Rect {
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
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

    private fun createPiece(pieceType: PieceType, pieceColor: PieceColor): Piece {
        return when (pieceType) {
            PieceType.WHITE_PAWN, PieceType.BLACK_PAWN -> Pawn(pieceType, pieceColor)
            PieceType.WHITE_ROOK, PieceType.BLACK_ROOK -> Rook(pieceType, pieceColor)
            PieceType.WHITE_KNIGHT, PieceType.BLACK_KNIGHT -> Knight(pieceType, pieceColor)
            PieceType.WHITE_BISHOP, PieceType.BLACK_BISHOP -> Bishop(pieceType, pieceColor)
            PieceType.WHITE_QUEEN, PieceType.BLACK_QUEEN -> Queen(pieceType, pieceColor)
            PieceType.WHITE_KING, PieceType.BLACK_KING -> King(pieceType, pieceColor)
        }
    }


    fun loadDrawables(context: Context) {
        for (pieceType in PieceType.values()) {
            val drawableResId = getDrawableResIdForPiece(pieceType)
            val drawable = drawableResId?.let {
                ContextCompat.getDrawable(context, it)
            }
            val pieceColor = getPieceColorForPieceType(pieceType) // Assuming you have a way to determine the color based on piece type
            val piece = createPiece(pieceType, pieceColor)
            drawable?.let {
                piece.loadDrawable(it)
            }
        }
    }

    private fun getPieceColorForPieceType(pieceType: PieceType): PieceColor {
        return when (pieceType) {
            PieceType.WHITE_PAWN, PieceType.WHITE_ROOK, PieceType.WHITE_KNIGHT,
            PieceType.WHITE_BISHOP, PieceType.WHITE_QUEEN, PieceType.WHITE_KING -> PieceColor.White
            PieceType.BLACK_PAWN, PieceType.BLACK_ROOK, PieceType.BLACK_KNIGHT,
            PieceType.BLACK_BISHOP, PieceType.BLACK_QUEEN, PieceType.BLACK_KING -> PieceColor.Black
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