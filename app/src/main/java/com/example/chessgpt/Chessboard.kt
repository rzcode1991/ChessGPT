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
    private var whiteLeftRookMoved = false
    private var whiteRightRookMoved = false
    private var blackLeftRookMoved = false
    private var blackRightRookMoved = false



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

        // Special handling for castling
        if (piece is King && isCastlingMove(piece, source, destination)) {
            performCastlingMove(source, destination)
            return
        }

        // Normal move
        // Remove the piece from the source coordinate
        setPiece(source, null)

        // Move the piece to the destination coordinate
        setPiece(destination, piece)

        // Update the king moved flag if the moved piece is a king
        if (piece is King) {
            updateKingMovedFlag(piece.color)
        }

        // Check if the moved piece is a pawn and has reached the last rank
        if (piece is Pawn && (destination.row == 0 || destination.row == 7)) {
            // Promote the pawn to a queen of the same color
            val promotedPiece = Queen(getPromotedPieceType(piece.color), piece.color)
            setPiece(destination, promotedPiece)
        }
    }


    private fun getPromotedPieceType(color: PieceColor): PieceType {
        return if (color == PieceColor.White) {
            PieceType.WHITE_QUEEN
        } else {
            PieceType.BLACK_QUEEN
        }
    }





    private fun isCastlingMove(selectedPiece: Piece, sourceCoordinate: Coordinate, destinationCoordinate: Coordinate): Boolean {
        // Check if the selected piece is the king and it hasn't moved
        if (selectedPiece !is King || selectedPiece.hasMoved) {
            return false
        }

        // Get the row and column differences between source and destination coordinates
        val rowDiff = destinationCoordinate.row - sourceCoordinate.row
        val colDiff = destinationCoordinate.col - sourceCoordinate.col

        // Check if the king is moving two squares horizontally (castling)
        if (rowDiff == 0 && abs(colDiff) == 2) {
            val row = sourceCoordinate.row
            val colStart = min(sourceCoordinate.col, destinationCoordinate.col)
            val colEnd = max(sourceCoordinate.col, destinationCoordinate.col)

            // Check if there are no pieces between the king and the rook
            for (col in colStart + 1 until colEnd) {
                if (getPiece(Coordinate(row, col)) != null) {
                    return false
                }
            }

            // Check if the rook is at the correct position
            val rookCol = if (colDiff > 0) 7 else 0
            val rookCoordinate = Coordinate(row, rookCol)
            val rook = getPiece(rookCoordinate)
            if (rook !is Rook || rook.hasMoved) {
                return false
            }

            return true
        }

        return false
    }



    private fun isRookMovedFlagValid(color: PieceColor, rookSourceCol: Int, rookDestinationCol: Int): Boolean {
        when (color) {
            PieceColor.White -> {
                if (rookSourceCol == 0 && !whiteLeftRookMoved) {
                    return true
                } else if (rookSourceCol == 7 && !whiteRightRookMoved) {
                    return true
                }
            }
            PieceColor.Black -> {
                if (rookSourceCol == 0 && !blackLeftRookMoved) {
                    return true
                } else if (rookSourceCol == 7 && !blackRightRookMoved) {
                    return true
                }
            }
        }
        return false
    }




    private fun performCastlingMove(source: Coordinate, destination: Coordinate) {
        val piece = getPiece(source) ?: return
        val row = source.row
        val kingDestinationCol = destination.col
        val rookSourceCol: Int
        val rookDestinationCol: Int

        if (kingDestinationCol < source.col) {
            // Left castling
            rookSourceCol = 0
            rookDestinationCol = source.col - 1
        } else {
            // Right castling
            rookSourceCol = 7
            rookDestinationCol = source.col + 1
        }

        val rookSourceCoordinate = Coordinate(row, rookSourceCol)
        val rookDestinationCoordinate = Coordinate(row, rookDestinationCol)
        val rook = getPiece(rookSourceCoordinate)

        // Move the king
        setPiece(source, null)
        setPiece(destination, piece)

        // Move the rook
        setPiece(rookSourceCoordinate, null)
        setPiece(rookDestinationCoordinate, rook)

        // Update the king and rook moved flags
        updateKingMovedFlag(piece.color)
        updateRookMovedFlag(rook!!.color, rookDestinationCol)
    }


    private fun updateKingMovedFlag(color: PieceColor) {
        when (color) {
            PieceColor.White -> whiteKing.move()
            PieceColor.Black -> blackKing.move()
        }
    }


    private fun updateRookMovedFlag(color: PieceColor, rookDestinationCol: Int) {
        when (color) {
            PieceColor.White -> {
                if (rookDestinationCol == 0) {
                    whiteLeftRookMoved = true
                } else if (rookDestinationCol == 7) {
                    whiteRightRookMoved = true
                }
            }
            PieceColor.Black -> {
                if (rookDestinationCol == 0) {
                    blackLeftRookMoved = true
                } else if (rookDestinationCol == 7) {
                    blackRightRookMoved = true
                }
            }
        }
    }


    private fun isSquareAttacked(coordinate: Coordinate, attackingColor: PieceColor): Boolean {
        // Check if any opponent's piece can attack the specified square
        val opponentPieces = getOpponentPieces(attackingColor)
        for ((opponentPiece, opponentCoordinate) in opponentPieces) {
            if (isMoveValid(opponentPiece, opponentCoordinate, coordinate)) {
                return true
            }
        }
        return false
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

        // Check if the move is a valid castling move
        if (piece is King && isCastlingMove(piece, source, destination)) {
            return true
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
        if (isValidPosition(leftCapture) && getPiece(leftCapture)?.color != pawn.color && getPiece(leftCapture) != null) {
            validMoves.add(leftCapture)
        }

        val rightCapture = Coordinate(row + direction, col + 1)
        if (isValidPosition(rightCapture) && getPiece(rightCapture)?.color != pawn.color && getPiece(rightCapture) != null) {
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
                if (piece == null || piece.color != king.color) {
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
                if (piece == null || piece.color != rook.color) {
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
                if (piece == null || piece.color != knight.color) {
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
                    if (piece.color != bishop.color) {
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



    fun isKingInCheck(playerColor: PieceColor): Boolean {
        val opponentPieces = getOpponentPieces(playerColor)
        val kingCoordinate = getOwnKingCoordinate(playerColor)

        for ((opponentPiece, opponentCoordinate) in opponentPieces) {
            val validMoves = getValidMoves(opponentPiece, opponentCoordinate)
            if (kingCoordinate in validMoves) {
                return true  // King is in check
            }
        }

        return false  // King is not in check
    }


    fun isCheckmate(playerColor: PieceColor): Boolean {
        val kingCoordinate = getOwnKingCoordinate(playerColor)

        // Check if the king is in check
        if (!isKingInCheck(playerColor)) {
            return false  // King is not in check, so not in checkmate
        }

        // Check if the king has any valid moves to escape check
        val king = getPiece(kingCoordinate) as? King ?: return true  // Invalid board state
        val validKingMoves = getValidMoves(king, kingCoordinate)

        for (move in validKingMoves) {
            val simulatedBoard = simulateMove(king, kingCoordinate, move)
            if (!simulatedBoard.isKingInCheck(playerColor)) {
                return false  // King has at least one valid move to escape check
            }
        }

        // Check if any other piece can block the check or capture the attacking piece
        val opponentPieces = getOpponentPieces(playerColor)
        for ((opponentPiece, opponentCoordinate) in opponentPieces) {
            val validMoves = getValidMoves(opponentPiece, opponentCoordinate)

            for (move in validMoves) {
                val simulatedBoard = simulateMove(opponentPiece, opponentCoordinate, move)
                if (!simulatedBoard.isKingInCheck(playerColor)) {
                    return false  // Piece can be blocked or captured to escape check
                }
            }
        }

        // If none of the above conditions are met, it's a checkmate
        return true
    }


    fun isStalemate(playerColor: PieceColor): Boolean {
        val ownPieces = getOwnPieces(playerColor)
        val validMoves = mutableListOf<Coordinate>()

        // Collect all valid moves for the player's pieces
        for ((piece, coordinate) in ownPieces) {
            validMoves.addAll(getValidMoves(piece, coordinate))
        }

        // If the player has no valid moves and is not in check, it's a stalemate
        return validMoves.isEmpty() && !isKingInCheck(playerColor)
    }


    private fun getOpponentKingCoordinate(playerColor: PieceColor): Coordinate {
        // Iterate over the chessboard to find the opponent's king
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = getPiece(Coordinate(row, col))
                if (piece is King && piece.color != playerColor) {
                    return Coordinate(row, col)
                }
            }
        }
        // Return a default coordinate if the opponent's king is not found (shouldn't happen in a valid chess game)
        return Coordinate(-1, -1)
    }


    private fun getOwnKingCoordinate(playerColor: PieceColor): Coordinate {
        // Iterate over the chessboard to find the king of the specified player color
        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val piece = getPiece(Coordinate(row, col))
                if (piece is King && piece.color == playerColor) {
                    return Coordinate(row, col)
                }
            }
        }
        // Return a default coordinate if the king is not found (shouldn't happen in a valid chess game)
        return Coordinate(-1, -1)
    }



    private fun getOpponentPieces(playerColor: PieceColor): List<Pair<Piece, Coordinate>> {
        val opponentColor = if (playerColor == PieceColor.White) PieceColor.Black else PieceColor.White
        val opponentPieces = mutableListOf<Pair<Piece, Coordinate>>()

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val piece = getPiece(Coordinate(row, col))
                if (piece != null && piece.color == opponentColor) {
                    opponentPieces.add(Pair(piece, Coordinate(row, col)))
                }
            }
        }

        return opponentPieces
    }


    private fun getOwnPieces(playerColor: PieceColor): List<Pair<Piece, Coordinate>> {
        val ownPieces = mutableListOf<Pair<Piece, Coordinate>>()

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val piece = getPiece(Coordinate(row, col))
                if (piece != null && piece.color == playerColor) {
                    ownPieces.add(Pair(piece, Coordinate(row, col)))
                }
            }
        }

        return ownPieces
    }


    private fun getPiecesOfType(piece: Piece): List<Pair<Piece, Coordinate>> {
        val piecesOfType = mutableListOf<Pair<Piece, Coordinate>>()

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val coordinate = Coordinate(row, col)
                val boardPiece = getPiece(coordinate)

                if (boardPiece != null && boardPiece::class == piece::class) {
                    piecesOfType.add(Pair(boardPiece, coordinate))
                }
            }
        }

        return piecesOfType
    }

    fun findSourceOfValidMove(pieceInfo: Pair<Piece?, Coordinate>?, board: Chessboard): Coordinate? {
        val (piece, destination) = pieceInfo ?: return null

        // Get list of all pieces of same type as the input piece
        val piecesOfSameType = piece?.let { board.getPiecesOfType(it) }

        // Search for a piece that has the destination as a valid move
        if (piecesOfSameType != null) {
            for ((otherPiece, source) in piecesOfSameType) {
                if (board.isMoveValid(otherPiece, source, destination)) {
                    // Found a piece where the destination is a valid move, return its source
                    return source
                }
            }
        }

        // No matching piece found
        return null
    }


    private fun isMoveBlocked(piece: Piece, source: Coordinate, destination: Coordinate): Boolean {
        val rowChange = destination.row - source.row
        val colChange = destination.col - source.col

        // Simulate the move on a temporary board
        val simulatedBoard = simulateMove(piece, source, destination)

        // Check if the move puts the own king in check
        val playerColor = piece.color
        if (simulatedBoard.isKingInCheck(playerColor)) {
            return true  // Move is blocked
        }

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
                    if (capturedPiece == null || capturedPiece.color == piece.color) {
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
                if (destinationPiece != null && destinationPiece.color == piece.color) {
                    // Destination is occupied by own piece
                    return true
                }
            }
            is Knight -> {
                // Logic specific to the Knight piece

                // Check if the destination is occupied by the player's own piece
                val destinationPiece = getPiece(destination)
                if (destinationPiece != null && destinationPiece.color == piece.color) {
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
                if (destinationPiece != null && destinationPiece.color == piece.color) {
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
                if (destinationPiece != null && destinationPiece.color == piece.color) {
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
                // Check if the destination is occupied by the player's own piece
                val destinationPiece = getPiece(destination)
                if (destinationPiece != null && destinationPiece.color == piece.color) {
                    return true  // Move is blocked
                }

                // Check if the move is within the King's allowed range (1 square in any direction)
                val rowOffset = abs(rowChange)
                val colOffset = abs(colChange)
                if (rowOffset <= 1 && colOffset <= 1) {
                    // Check if the destination square is adjacent to an opponent's king
                    val opponentKingCoordinate = getOpponentKingCoordinate(piece.color)
                    val isAdjacentToOpponentKing = abs(destination.row - opponentKingCoordinate.row) <= 1 &&
                            abs(destination.col - opponentKingCoordinate.col) <= 1
                    if (isAdjacentToOpponentKing) {
                        return true  // Move is blocked
                    }



                    // If all checks pass, the move is not blocked
                    return false
                }

                // If any of the checks fail, the move is blocked
                return true
            }


        }

        return false // Replace with the appropriate return statement
    }


    private fun simulateMove(piece: Piece, source: Coordinate, destination: Coordinate): Chessboard {
        val simulatedBoard = Chessboard(context)
        // Copy the current board state to the simulated board
        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val coordinate = Coordinate(row, col)
                val pieceAtCoordinate = getPiece(coordinate)
                simulatedBoard.setPiece(coordinate, pieceAtCoordinate)
            }
        }
        // Move the piece on the simulated board
        simulatedBoard.movePiece(source, destination)
        return simulatedBoard
    }









}
