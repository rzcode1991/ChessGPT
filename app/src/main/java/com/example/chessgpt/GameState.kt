package com.example.chessgpt

data class GameState(
    val chessboardState: List<List<Piece?>>, // 2D array representing the chessboard state
    val moveHistory: List<String>, // List of algebraic notations of moves
    val currentPlayer: PieceColor // Current player's turn
)
