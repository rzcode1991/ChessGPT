package com.example.chessgpt

import android.content.Context
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class Chessboard(context: Context) : View(context) {


    private val numRows = 8
    private val numColumns = 8
    private val board: Array<Array<Piece?>> = Array(8) { Array<Piece?>(8) { null } }



    private val whitePawn = Pawn(PieceType.WHITE_PAWN, PieceColor.White)
    private val blackPawn = Pawn(PieceType.BLACK_PAWN, PieceColor.Black)
    private val whiteRook = Rook(PieceType.WHITE_ROOK, PieceColor.White)
    private val blackRook = Rook(PieceType.BLACK_ROOK, PieceColor.Black)
    private val whiteKnight = Knight(PieceType.WHITE_KNIGHT, PieceColor.White)
    private val blackKnight = Knight(PieceType.BLACK_KNIGHT, PieceColor.Black)
    private val whiteBishop = Bishop(PieceType.WHITE_BISHOP, PieceColor.White)
    private val blackBishop = Bishop(PieceType.BLACK_BISHOP, PieceColor.Black)
    private val whiteQueen = Queen(PieceType.WHITE_QUEEN, PieceColor.White)
    private val blackQueen = Queen(PieceType.BLACK_QUEEN, PieceColor.Black)
    private val whiteKing = King(PieceType.WHITE_KING, PieceColor.White)
    private val blackKing = King(PieceType.BLACK_KING, PieceColor.Black)


    init {
        initializePieces()
    }


    private fun initializePieces() {

        // the black pieces will be placed at the bottom of the board (rows 0 and 1)
        // and the white pieces will be placed at the top of the board (rows 6 and 7)

        // Set up pawns
        for (column in 0 until 8) {
            setPiece(Coordinate(6, column), whitePawn)  // Place white pawns at row 6
            setPiece(Coordinate(1, column), blackPawn)  // Place black pawns at row 1
        }

        // Set up rooks
        setPiece(Coordinate(7, 0), whiteRook)  // Place white rook at row 7
        setPiece(Coordinate(7, 7), whiteRook)  // Place white rook at row 7
        setPiece(Coordinate(0, 0), blackRook)  // Place black rook at row 0
        setPiece(Coordinate(0, 7), blackRook)  // Place black rook at row 0

        // Set up knights
        setPiece(Coordinate(7, 1), whiteKnight)  // Place white knight at row 7
        setPiece(Coordinate(7, 6), whiteKnight)  // Place white knight at row 7
        setPiece(Coordinate(0, 1), blackKnight)  // Place black knight at row 0
        setPiece(Coordinate(0, 6), blackKnight)  // Place black knight at row 0

        // Set up bishops
        setPiece(Coordinate(7, 2), whiteBishop)  // Place white bishop at row 7
        setPiece(Coordinate(7, 5), whiteBishop)  // Place white bishop at row 7
        setPiece(Coordinate(0, 2), blackBishop)  // Place black bishop at row 0
        setPiece(Coordinate(0, 5), blackBishop)  // Place black bishop at row 0

        // Set up queens
        setPiece(Coordinate(7, 3), whiteQueen)  // Place white queen at row 7
        setPiece(Coordinate(0, 3), blackQueen)  // Place black queen at row 0

        // Set up kings
        setPiece(Coordinate(7, 4), whiteKing)  // Place white king at row 7
        setPiece(Coordinate(0, 4), blackKing)  // Place black king at row 0
    }



    fun movePiece(source: Coordinate, destination: Coordinate) {
        val piece = getPiece(source) ?: return  // Return if there's no piece at the source coordinate

        // Remove the piece from the source coordinate
        setPiece(source, null)

        // Move the piece to the destination coordinate
        setPiece(destination, piece)
    }


    fun removePiece(coordinate: Coordinate) {
        setPiece(coordinate, null)
    }



    fun getPiece(coordinate: Coordinate): Piece? {
        val row = coordinate.row
        val col = coordinate.col
        return if (isValidCoordinate(row, col)) {
            board[row][col]
        } else {
            null
        }
    }

    private fun setPiece(coordinate: Coordinate, piece: Piece?) {
        val row = coordinate.row
        val col = coordinate.col
        if (isValidCoordinate(row, col)) {
            board[row][col] = piece
        }
    }

    fun isOpponentPiece(selectedPiece: Piece, opponentPiece: Piece): Boolean {
        // Check if the types of the two pieces are different
        return selectedPiece.type != opponentPiece.type
    }



    private fun isValidCoordinate(row: Int, col: Int): Boolean {
        return row in 0..7 && col in 0..7
    }

    fun getEmptySquares(): List<Coordinate> {
        val emptySquares = mutableListOf<Coordinate>()

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val coordinate = Coordinate(row, col)
                if (getPiece(coordinate) == null) {
                    emptySquares.add(coordinate)
                }
            }
        }

        return emptySquares
    }

    fun getAlgebraicNotation(coordinate: Coordinate): String {
        val (row, col) = coordinate
        val file = ('a' + col).toString()  // Map column index to file letter (a-h)
        val rank = (row + 1).toString()  // Map row index to rank number (1-8)
        return "$file$rank"
    }


    fun isMoveValid(piece: Piece, source: Coordinate, destination: Coordinate): Boolean {
        // Check if the source position contains the specified piece
        if (getPiece(source) != piece) {
            return false
        }

        // Check if the source and destination positions are different
        if (source == destination) {
            return false
        }

        // Check if the destination position is within the valid bounds of the chessboard
        if (!isValidPosition(destination)) {
            return false
        }

        // Get the valid moves for the piece and check if the destination is one of them
        val validMoves = getValidMoves(piece, source)
        if (destination !in validMoves) {
            return false
        }

        if (isMoveBlocked(piece, source, destination)) {
            return false
        }

        // Additional checks specific to each piece can be added here

        // If all checks pass, the move is considered valid
        return true
    }



    private fun getValidMoves(piece: Piece, source: Coordinate): List<Coordinate> {
        return when (piece) {
            is Pawn -> getValidMovesForPawn(piece, source)
            is King -> getValidMovesForKing(piece, source)
            is Rook -> getValidMovesForRook(piece, source)
            is Knight -> getValidMovesForKnight(piece, source)
            is Bishop -> getValidMovesForBishop(piece, source)
            is Queen -> getValidMovesForQueen(piece, source)
            else -> emptyList()
        }
    }

    private fun getValidMovesForPawn(pawn: Pawn, source: Coordinate): List<Coordinate> {
        val validMoves = mutableListOf<Coordinate>()
        val (row, col) = source

        // Determine the direction the pawn should move based on its type
        val direction = if (pawn.type == PieceType.WHITE_PAWN) -1 else 1

        // Check the forward move
        val forwardMove = Coordinate(row + direction, col)
        if (isValidPosition(forwardMove) && getPiece(forwardMove) == null) {
            validMoves.add(forwardMove)

            // Check the double forward move on the pawn's first move
            if ((pawn.type == PieceType.WHITE_PAWN && row == 6) || (pawn.type == PieceType.BLACK_PAWN && row == 1)) {
                val doubleForwardMove = Coordinate(row + (2 * direction), col)
                if (isValidPosition(doubleForwardMove) && getPiece(doubleForwardMove) == null) {
                    validMoves.add(doubleForwardMove)
                }
            }
        }

        // Check the diagonal captures
        val leftCapture = Coordinate(row + direction, col - 1)
        if (isValidPosition(leftCapture) && getPiece(leftCapture)?.type != pawn.type && getPiece(leftCapture) != null) {
            validMoves.add(leftCapture)
        }

        val rightCapture = Coordinate(row + direction, col + 1)
        if (isValidPosition(rightCapture) && getPiece(rightCapture)?.type != pawn.type && getPiece(rightCapture) != null) {
            validMoves.add(rightCapture)
        }

        return validMoves
    }





    private fun getValidMovesForKing(king: King, source: Coordinate): List<Coordinate> {
        val validMoves = mutableListOf<Coordinate>()
        val (row, col) = source

        // Define the possible offsets for the king's moves
        val offsets = listOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1), /*(row, col)*/ Pair(0, 1),
            Pair(1, -1), Pair(1, 0), Pair(1, 1)
        )

        // Check each possible move
        for ((offsetRow, offsetCol) in offsets) {
            val destination = Coordinate(row + offsetRow, col + offsetCol)

            // Check if the destination is a valid position
            if (isValidPosition(destination)) {
                val piece = getPiece(destination)

                // If the destination is empty or contains an opponent's piece, it is a valid move
                if (piece == null || piece.type != king.type) {
                    validMoves.add(destination)
                }
            }
        }

        return validMoves
    }



    private fun getValidMovesForRook(rook: Rook, source: Coordinate): List<Coordinate> {
        val validMoves = mutableListOf<Coordinate>()
        val (row, col) = source

        // Define the possible offsets for the rook's moves (horizontal and vertical)
        val offsets = listOf(
            Pair(-1, 0), Pair(1, 0), // Vertical moves
            Pair(0, -1), Pair(0, 1) // Horizontal moves
        )

        // Check each possible move
        for ((offsetRow, offsetCol) in offsets) {
            var currentRow = row + offsetRow
            var currentCol = col + offsetCol

            // Continue moving in the same direction until a piece is encountered or the edge of the board is reached
            while (isValidPosition(Coordinate(currentRow, currentCol))) {
                val destination = Coordinate(currentRow, currentCol)
                val piece = getPiece(destination)

                // If the destination is empty or contains an opponent's piece, it is a valid move
                if (piece == null || piece.type != rook.type) {
                    validMoves.add(destination)
                }

                // Stop further movement if a piece is encountered
                if (piece != null) {
                    break
                }

                // Move to the next position in the same direction
                currentRow += offsetRow
                currentCol += offsetCol
            }
        }

        return validMoves
    }



    private fun getValidMovesForKnight(knight: Knight, source: Coordinate): List<Coordinate> {
        val validMoves = mutableListOf<Coordinate>()
        val (row, col) = source

        // Define the possible knight move offsets
        val offsets = listOf(
            Pair(-2, -1), Pair(-2, 1),
            Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2),
            Pair(2, -1), Pair(2, 1)
        )

        // Check each possible move
        for ((offsetRow, offsetCol) in offsets) {
            val destination = Coordinate(row + offsetRow, col + offsetCol)

            // Check if the destination is a valid position and if it's either empty or contains an opponent's piece
            if (isValidPosition(destination)) {
                val piece = getPiece(destination)
                if (piece == null || piece.type != knight.type) {
                    validMoves.add(destination)
                }
            }
        }

        return validMoves
    }



    private fun getValidMovesForBishop(bishop: Bishop, source: Coordinate): List<Coordinate> {
        val validMoves = mutableListOf<Coordinate>()
        val (row, col) = source

        // Define the possible move directions for a bishop (diagonals)
        val directions = listOf(
            Pair(-1, -1), Pair(-1, 1),
            Pair(1, -1), Pair(1, 1)
        )

        // Check each direction
        for ((offsetRow, offsetCol) in directions) {
            var currRow = row + offsetRow
            var currCol = col + offsetCol

            // Continue moving in the direction until an invalid position or a piece is encountered
            while (isValidPosition(Coordinate(currRow, currCol))) {
                val destination = Coordinate(currRow, currCol)
                val piece = getPiece(destination)

                if (piece == null) {
                    validMoves.add(destination)
                } else {
                    // If the destination contains an opponent's piece, it's a valid move
                    if (piece.type != bishop.type) {
                        validMoves.add(destination)
                    }
                    break
                }

                // Move to the next position in the direction
                currRow += offsetRow
                currCol += offsetCol
            }
        }

        return validMoves
    }



    private fun getValidMovesForQueen(queen: Queen, source: Coordinate): List<Coordinate> {
        val validMoves = mutableListOf<Coordinate>()
        val (row, col) = source

        // Check valid moves along the rows and columns (like Rook)
        for (c in col - 1 downTo 0) {
            val coordinate = Coordinate(row, c)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
        }

        for (c in col + 1 until 8) {
            val coordinate = Coordinate(row, c)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
        }

        for (r in row - 1 downTo 0) {
            val coordinate = Coordinate(r, col)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
        }

        for (r in row + 1 until 8) {
            val coordinate = Coordinate(r, col)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
        }

        // Check valid moves along the diagonals (like Bishop)
        var r = row - 1
        var c = col - 1
        while (r >= 0 && c >= 0) {
            val coordinate = Coordinate(r, c)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
            r--
            c--
        }

        r = row - 1
        c = col + 1
        while (r >= 0 && c < 8) {
            val coordinate = Coordinate(r, c)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
            r--
            c++
        }

        r = row + 1
        c = col - 1
        while (r < 8 && c >= 0) {
            val coordinate = Coordinate(r, c)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
            r++
            c--
        }

        r = row + 1
        c = col + 1
        while (r < 8 && c < 8) {
            val coordinate = Coordinate(r, c)
            val piece = getPiece(coordinate)
            if (piece == null) {
                validMoves.add(coordinate)
            } else {
                if (piece.color != queen.color) {
                    validMoves.add(coordinate)
                }
                break
            }
            r++
            c++
        }

        return validMoves
    }




    private fun isValidPosition(coordinate: Coordinate): Boolean {
        val (row, col) = coordinate
        return row in board.indices && col in board.indices
    }



    fun getBoardString(): String {
        val boardString = StringBuilder()
        for (row in board) {
            for (piece in row) {
                boardString.append("$piece ")
            }
            boardString.append("\n")
        }
        return boardString.toString()
    }


    private fun isMoveBlocked(piece: Piece, source: Coordinate, destination: Coordinate): Boolean {
        val rowChange = destination.row - source.row
        val colChange = destination.col - source.col

        when (piece) {
            is Pawn -> {
                if (colChange == 0) {
                    // Moving forward
                    if (rowChange == 1 && getPiece(destination) != null) {
                        // Destination is occupied
                        return true
                    } else if (rowChange == 2 && source.row == 1 && getPiece(Coordinate(source.row + 1, source.col)) != null) {
                        // Double-step move blocked by a piece in between
                        return true
                    }
                } else if (abs(colChange) == 1 && rowChange == 1) {
                    // Capturing diagonally
                    val capturedPiece = getPiece(destination)
                    if (capturedPiece == null || capturedPiece.type == piece.type) {
                        // No piece to capture or capturing own drawable
                        return true
                    }
                }
            }
            is Rook -> {
                // Logic specific to the Rook piece
                if (rowChange == 0) {
                    // Horizontal movement
                    val startCol = min(source.col, destination.col)
                    val endCol = max(source.col, destination.col)
                    for (col in startCol + 1 until endCol) {
                        if (getPiece(Coordinate(source.row, col)) != null) {
                            // Path is blocked
                            return true
                        }
                    }
                } else if (colChange == 0) {
                    // Vertical movement
                    val startRow = min(source.row, destination.row)
                    val endRow = max(source.row, destination.row)
                    for (row in startRow + 1 until endRow) {
                        if (getPiece(Coordinate(row, source.col)) != null) {
                            // Path is blocked
                            return true
                        }
                    }
                } else {
                    // Invalid movement (not horizontal or vertical)
                    return true
                }

                // Check if the destination is occupied by the player's own piece
                val destinationPiece = getPiece(destination)
                if (destinationPiece != null && destinationPiece.type == piece.type) {
                    // Destination is occupied by own piece
                    return true
                }
            }
            is Knight -> {
                // Logic specific to the Knight piece

                // Check if the destination is occupied by the player's own piece
                val destinationPiece = getPiece(destination)
                if (destinationPiece != null && destinationPiece.type == piece.type) {
                    return true  // Move is blocked
                }

                // Calculate the absolute difference in row and column positions
                val rowDiff = abs(destination.row - source.row)
                val colDiff = abs(destination.col - source.col)

                // Check if the move follows the knight's movement pattern
                return !((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))
            }

            is Bishop -> {
                // Logic specific to the Bishop piece
                val rowOffset = abs(source.row - destination.row)
                val colOffset = abs(source.col - destination.col)

                // Check if the destination is occupied by the player's own piece
                val destinationPiece = getPiece(destination)
                if (destinationPiece != null && destinationPiece.type == piece.type) {
                    return true  // Move is blocked
                }

                // Check if the move follows the Bishop's movement pattern (diagonal movement)
                if (rowOffset == colOffset) {
                    // Check if there are any pieces blocking the path
                    val rowDirection = if (destination.row > source.row) 1 else -1
                    val colDirection = if (destination.col > source.col) 1 else -1

                    var currentRow = source.row + rowDirection
                    var currentCol = source.col + colDirection
                    while (currentRow != destination.row && currentCol != destination.col) {
                        val currentCoordinate = Coordinate(currentRow, currentCol)
                        val currentPiece = getPiece(currentCoordinate)
                        if (currentPiece != null) {
                            return true  // Move is blocked
                        }
                        currentRow += rowDirection
                        currentCol += colDirection
                    }

                    // If all checks pass, the move is not blocked
                    return false
                }

                // If any of the checks fail, the move is blocked
                return true
            }

            is Queen -> {
                // Logic specific to the Queen piece
                val rowOffset = abs(source.row - destination.row)
                val colOffset = abs(source.col - destination.col)

                // Check if the destination is occupied by the player's own piece
                val destinationPiece = getPiece(destination)
                if (destinationPiece != null && destinationPiece.type == piece.type) {
                    return true  // Move is blocked
                }

                // Check if the move follows the Queen's movement pattern (horizontal, vertical, or diagonal movement)
                if (source.row == destination.row || source.col == destination.col || rowOffset == colOffset) {
                    // Check if there are any pieces blocking the path

                    // Check for horizontal movement
                    if (source.row == destination.row) {
                        val startCol = min(source.col, destination.col) + 1
                        val endCol = max(source.col, destination.col)
                        for (col in startCol until endCol) {
                            val currentCoordinate = Coordinate(source.row, col)
                            val currentPiece = getPiece(currentCoordinate)
                            if (currentPiece != null) {
                                return true  // Move is blocked
                            }
                        }
                    }

                    // Check for vertical movement
                    if (source.col == destination.col) {
                        val startRow = min(source.row, destination.row) + 1
                        val endRow = max(source.row, destination.row)
                        for (row in startRow until endRow) {
                            val currentCoordinate = Coordinate(row, source.col)
                            val currentPiece = getPiece(currentCoordinate)
                            if (currentPiece != null) {
                                return true  // Move is blocked
                            }
                        }
                    }

                    // Check for diagonal movement
                    if (rowOffset == colOffset) {
                        val rowDirection = if (destination.row > source.row) 1 else -1
                        val colDirection = if (destination.col > source.col) 1 else -1

                        var currentRow = source.row + rowDirection
                        var currentCol = source.col + colDirection
                        while (currentRow != destination.row && currentCol != destination.col) {
                            val currentCoordinate = Coordinate(currentRow, currentCol)
                            val currentPiece = getPiece(currentCoordinate)
                            if (currentPiece != null) {
                                return true  // Move is blocked
                            }
                            currentRow += rowDirection
                            currentCol += colDirection
                        }
                    }

                    // If all checks pass, the move is not blocked
                    return false
                }

                // If any of the checks fail, the move is blocked
                return true
            }

            is King -> {
                // Logic specific to the King piece

                // Check if the destination is occupied by the player's own piece
                val destinationPiece = getPiece(destination)
                if (destinationPiece != null && destinationPiece.type == piece.type) {
                    return true  // Move is blocked
                }

                // Check if the move is within the King's allowed range (1 square in any direction)
                val rowOffset = abs(source.row - destination.row)
                val colOffset = abs(source.col - destination.col)
                if (rowOffset <= 1 && colOffset <= 1) {
                    // If all checks pass, the move is not blocked
                    return false
                }

                // If any of the checks fail, the move is blocked
                return true
            }

        }

        return false // Replace with the appropriate return statement
    }







}
