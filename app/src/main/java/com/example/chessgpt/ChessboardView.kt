package com.example.chessgpt

import android.util.Log
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
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
    private var selectedPieceAI: Piece? = null
    private var aiTurn: Boolean = false
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


    private fun addMoveToHistory(attackingPiece: String, isCapture: Boolean, destinationCoordinate: Coordinate, sourceCoordinate: Coordinate) {

        if (selectedPiece == null || chessboard == null) {
            return
        }

        val targetPieceSymbol = if (isCapture) "x" else ""

        // Check for additional features
        val promotion = if (selectedPiece is Pawn) {
            if ((selectedPiece as Pawn).pieceColor() == PieceColor.White && destinationCoordinate.row == 0 ||
                (selectedPiece as Pawn).pieceColor() == PieceColor.Black && destinationCoordinate.row == 7
            ) {
                "=Q" // Promotion to Queen
            } else {
                ""
            }
        } else {
            ""
        }

        val check = if (chessboard?.isKingInCheck(selectedPiece!!.pieceColor().opposite()) == true) "+" else ""

        val castling = if (selectedPiece is King) {
            if ((selectedPiece as King).pieceColor() == PieceColor.White) {
                if (destinationCoordinate.col == 6 && destinationCoordinate.row == 7) "O-O" // Castling kingside for white
                else if (destinationCoordinate.col == 2 && destinationCoordinate.row == 7) "O-O-O" // Castling queenside for white
                else ""
            } else {
                if (destinationCoordinate.col == 6 && destinationCoordinate.row == 0) "O-O" // Castling kingside for black
                else if (destinationCoordinate.col == 2 && destinationCoordinate.row == 0) "O-O-O" // Castling queenside for black
                else ""
            }
        } else {
            ""
        }

        val destinationCoordinateNotation = if (castling.isEmpty()) destinationCoordinate.getAlgebraicNotation() else ""

        val attackerPiece = if (castling.isNotEmpty()){
            ""
        }else if (selectedPiece is Pawn && isCapture){
            sourceCoordinate.getAlgebraicNotation()[0]
        }else{
            attackingPiece
        }


        moveNumber++
        val moveNotation = if (moveNumber % 2 == 0) {
            "${moveNumber / 2}. $attackerPiece$targetPieceSymbol$destinationCoordinateNotation$promotion$check$castling"
        } else {
            "$attackerPiece$targetPieceSymbol$destinationCoordinateNotation$promotion$check$castling"
        }

        moveHistory.add(moveNotation)
    }


    private fun addMoveToHistoryAI(attackingPieceAI: String, isCaptureAI: Boolean, destinationCoordinateAI: Coordinate, sourceCoordinateAI: Coordinate) {

        if (selectedPieceAI == null || chessboard == null) {
            return
        }

        val targetPieceSymbolAI = if (isCaptureAI) "x" else ""

        // Check for additional features
        val promotionAI = if (selectedPieceAI is Pawn) {
            if ((selectedPieceAI as Pawn).pieceColor() == PieceColor.White && destinationCoordinateAI.row == 0 ||
                (selectedPieceAI as Pawn).pieceColor() == PieceColor.Black && destinationCoordinateAI.row == 7
            ) {
                "=Q" // Promotion to Queen
            } else {
                ""
            }
        } else {
            ""
        }

        val checkAI = if (chessboard?.isKingInCheck(selectedPieceAI!!.pieceColor().opposite()) == true) "+" else ""

        val castlingAI = if (selectedPieceAI is King) {
            if ((selectedPieceAI as King).pieceColor() == PieceColor.White) {
                if (destinationCoordinateAI.col == 6 && destinationCoordinateAI.row == 7) "O-O" // Castling kingside for white
                else if (destinationCoordinateAI.col == 2 && destinationCoordinateAI.row == 7) "O-O-O" // Castling queenside for white
                else ""
            } else {
                if (destinationCoordinateAI.col == 6 && destinationCoordinateAI.row == 0) "O-O" // Castling kingside for black
                else if (destinationCoordinateAI.col == 2 && destinationCoordinateAI.row == 0) "O-O-O" // Castling queenside for black
                else ""
            }
        } else {
            ""
        }

        val destinationCoordinateNotationAI = if (castlingAI.isEmpty()) destinationCoordinateAI.getAlgebraicNotation() else ""

        val attackerPieceAI = if (castlingAI.isNotEmpty()){
            ""
        }else if (selectedPieceAI is Pawn && isCaptureAI){
            sourceCoordinateAI.getAlgebraicNotation()[0]
        }else{
            attackingPieceAI
        }


        moveNumber++
        val moveNotationAI = if (moveNumber % 2 == 0) {
            "${moveNumber / 2}. $attackerPieceAI$targetPieceSymbolAI$destinationCoordinateNotationAI$promotionAI$checkAI$castlingAI"
        } else {
            "$attackerPieceAI$targetPieceSymbolAI$destinationCoordinateNotationAI$promotionAI$checkAI$castlingAI"
        }

        moveHistory.add(moveNotationAI)
    }



    private fun handleTouchUp(x: Float, y: Float) {
        if (selectedPiece != null && selectedPieceCoordinate != null) {
            val sourceCoordinate = selectedPieceCoordinate
            val destinationCoordinate = getCoordinateFromPosition(x, y)

            if (sourceCoordinate != null && destinationCoordinate != null) {
                Log.d("Human MOve", "Human MOve: from $sourceCoordinate to $destinationCoordinate")
                val isMoveValid = chessboard?.isMoveValid(selectedPiece!!, sourceCoordinate, destinationCoordinate)

                if (isMoveValid == true) {
                    val targetPiece = chessboard?.getPiece(destinationCoordinate)

                    if (targetPiece == null || targetPiece.pieceColor() != selectedPiece!!.pieceColor()) {
                        // Move the piece to the valid destination
                        chessboard?.movePiece(sourceCoordinate, destinationCoordinate)
                        invalidate() // Redraw the chessboard to reflect the updated position

                        // Add algebraic notation of the move to the moveHistory list
                        val attackingPiece = selectedPiece?.getPieceNotation() ?: "" // Get the notation of the attacking piece
                        val isCapture = targetPiece != null
                        addMoveToHistory(attackingPiece, isCapture, destinationCoordinate, sourceCoordinate)

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
            triggerAI()

        }


    }

    private fun triggerAI(){
        coroutineScope.launch {

            val boardState = moveHistory.joinToString(" ")

            Log.d("Board State", "Previous Moves: $boardState")

            // Call suggestMove with boardState
            val aiMove = aiPlayer?.suggestMove(boardState)
            if (aiMove != null) {
                Log.d("AI Move", "Suggested Move: $aiMove")
                val aiMOveShortened = extractAIMoveFromMessage(aiMove)
                Log.d("aiMOveShortened", "aiMOveShortened: $aiMOveShortened")
                val moveResult = aiMOveShortened?.let { extractMoveInfo(it) }
                Log.d("AI Move Result", "AI Piece & Destination: $moveResult")

                val board = chessboard
                if (board != null) {
                    val sourceCoordinateFromAI = board.findSourceOfValidMove(moveResult, board)
                    Log.d("AI SOURCE", "AI SOURCE: $sourceCoordinateFromAI")

                    if (moveResult != null && sourceCoordinateFromAI != null) {
                        val destinationCoordinateAI = moveResult.second

                        selectedPieceAI = chessboard?.getPiece(sourceCoordinateFromAI)
                        if (selectedPieceAI != null && selectedPieceAI?.pieceColor() == PieceColor.Black && sourceCoordinateFromAI != null && destinationCoordinateAI != null) {
                            val isMoveValidAI = chessboard?.isMoveValid(selectedPieceAI!!, sourceCoordinateFromAI, destinationCoordinateAI)

                            if (isMoveValidAI == true) {
                                val targetPieceAI = chessboard?.getPiece(destinationCoordinateAI)

                                if (targetPieceAI == null || targetPieceAI.pieceColor() != selectedPieceAI!!.pieceColor()) {
                                    // Move the piece to the valid destination
                                    chessboard?.movePiece(sourceCoordinateFromAI, destinationCoordinateAI)
                                    // Redraw the chessboard to reflect the updated position
                                    invalidate()

                                    // Add algebraic notation of the AI move to the moveHistory list
                                    val attackingPieceAI = selectedPieceAI?.getPieceNotation() ?: "null" // Get the notation of the attacking piece
                                    val isCaptureAI = targetPieceAI != null
                                    addMoveToHistoryAI(attackingPieceAI, isCaptureAI, destinationCoordinateAI, sourceCoordinateFromAI)

                                    Log.i("adding AI move to list", "adding AI move to list: $attackingPieceAI $isCaptureAI $destinationCoordinateAI $sourceCoordinateFromAI")

                                    // Reset the selected piece and coordinate
                                    //selectedPieceAI = null
                                    //selectedPieceCoordinateAI = null

                                    // Highlight the selected piece's coordinate
                                    highlightedCoordinate = destinationCoordinateAI

                                    invalidate() // Redraw the chessboard to highlight the selected piece
                                }
                            }
                        }
                    }

                }
            }


        }
    }


    private fun extractAIMoveFromMessage(aiMessage: String): String? {
        val regex = Regex("\\.\\.\\.\\s*(.*)")
        val matchResult = regex.find(aiMessage)

        return matchResult?.groupValues?.get(1)?.trim()
    }


    private fun getPieceFromSymbol(symbol: String): Piece? {
        return when (symbol) {
            "pawn" -> Pawn(PieceType.BLACK_PAWN, PieceColor.Black)
            "r" -> Rook(PieceType.BLACK_ROOK, PieceColor.Black)
            "n" -> Knight(PieceType.BLACK_KNIGHT, PieceColor.Black)
            "b" -> Bishop(PieceType.BLACK_BISHOP, PieceColor.Black)
            "q" -> Queen(PieceType.BLACK_QUEEN, PieceColor.Black)
            "k" -> King(PieceType.BLACK_KING, PieceColor.Black)
            else -> null
        }
    }

    private fun extractMoveInfo(move: String): Pair<Piece?, Coordinate>? {
        val type1Regex = Regex("([a-h])(\\d)")
        val type2Regex = Regex("([a-h])x([a-h])(\\d)")
        val type3Regex = Regex("([RNBQK])([a-h])(\\d)")
        val type4Regex = Regex("([RNBQK])(x)([a-h])(\\d)")
        val type5Regex = Regex("([a-h])(\\d)(=Q)")
        val type6Regex = Regex("(.+)\\+")
        val type7KingsideRegex = Regex("O-O")
        val type7QueensideRegex = Regex("O-O-O")
        val additionalType1Regex = Regex("([RN])([a-h])x([a-h])(\\d)")
        val additionalType2Regex = Regex("([RN])([a-h])([a-h])(\\d)")

        val type1Match = type1Regex.matchEntire(move)
        val type2Match = type2Regex.matchEntire(move)
        val type3Match = type3Regex.matchEntire(move)
        val type4Match = type4Regex.matchEntire(move)
        val type5Match = type5Regex.matchEntire(move)
        val type6Match = type6Regex.matchEntire(move)
        val type7KingsideMatch = type7KingsideRegex.matchEntire(move)
        val type7QueensideMatch = type7QueensideRegex.matchEntire(move)
        val additionalType1Match = additionalType1Regex.matchEntire(move)
        val additionalType2Match = additionalType2Regex.matchEntire(move)

        return when {
            type1Match != null -> {
                val destinationColumn = type1Match.groups[1]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationRow = 8 - type1Match.groups[2]?.value?.toInt()!!
                Pair(getPieceFromSymbol("pawn"), Coordinate(destinationRow, destinationColumn!!))
            }
            type2Match != null -> {
                val sourceColumn = type2Match.groups[1]?.value?.get(0)?.toInt()?.minus('a'.toInt())
                val destinationColumn = type2Match.groups[2]?.value?.get(0)?.toInt()?.minus('a'.toInt())
                val destinationRow = 8 - type2Match.groups[3]?.value?.toInt()!!
                Pair(getPieceFromSymbol("pawn"), Coordinate(destinationRow, destinationColumn!!))
            }
            type3Match != null -> {
                val pieceSymbol = type3Match.groups[1]?.value?.toLowerCase() ?: ""
                val destinationColumn = type3Match.groups[2]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationRow = 8 - type3Match.groups[3]?.value?.toInt()!!
                Pair(getPieceFromSymbol(pieceSymbol), Coordinate(destinationRow, destinationColumn!!))
            }
            type4Match != null -> {
                val pieceSymbol = type4Match.groups[1]?.value?.toLowerCase() ?: ""
                val destinationColumn = type4Match.groups[3]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationRow = 8 - type4Match.groups[4]?.value?.toInt()!!
                Pair(getPieceFromSymbol(pieceSymbol), Coordinate(destinationRow, destinationColumn!!))
            }
            type5Match != null -> {
                val destinationColumn = type5Match.groups[1]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationRow = 8 - type5Match.groups[2]?.value?.toInt()!!
                Pair(getPieceFromSymbol("pawn"), Coordinate(destinationRow, destinationColumn!!))
            }
            type6Match != null -> {
                val matchedMove = type6Match.groups[1]?.value?.trim() ?: ""
                extractMoveInfo(matchedMove)
            }
            type7KingsideMatch != null -> Pair(getPieceFromSymbol("k"), Coordinate(0, 6)) // Kingside castling
            type7QueensideMatch != null -> Pair(getPieceFromSymbol("k"), Coordinate(0, 2)) // Queenside castling
            additionalType1Match != null -> {
                val pieceSymbol = additionalType1Match.groups[1]?.value?.toLowerCase() ?: ""
                val sourceColumn = additionalType1Match.groups[2]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationColumn = additionalType1Match.groups[3]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationRow = 8 - additionalType1Match.groups[4]?.value?.toInt()!!
                Pair(getPieceFromSymbol(pieceSymbol), Coordinate(destinationRow, destinationColumn!!))
            }
            additionalType2Match != null -> {
                val pieceSymbol = additionalType2Match.groups[1]?.value?.toLowerCase() ?: ""
                val sourceColumn = additionalType2Match.groups[2]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationColumn = additionalType2Match.groups[3]?.value?.get(0)?.toInt()
                    ?.minus('a'.toInt())
                val destinationRow = 8 - additionalType2Match.groups[4]?.value?.toInt()!!
                Pair(getPieceFromSymbol(pieceSymbol), Coordinate(destinationRow, destinationColumn!!))
            }
            else -> null
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