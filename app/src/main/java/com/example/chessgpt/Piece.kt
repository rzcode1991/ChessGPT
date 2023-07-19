package com.example.chessgpt

import android.graphics.drawable.Drawable

enum class PieceType {
    WHITE_PAWN,
    BLACK_PAWN,
    WHITE_ROOK,
    BLACK_ROOK,
    WHITE_KNIGHT,
    BLACK_KNIGHT,
    WHITE_BISHOP,
    BLACK_BISHOP,
    WHITE_QUEEN,
    BLACK_QUEEN,
    WHITE_KING,
    BLACK_KING
}

enum class PieceColor {
    White,
    Black
}

abstract class Piece(val type: PieceType, val color: PieceColor) {

    abstract fun getPieceNotation(): String


    var drawable: Drawable? = null

    var hasMoved: Boolean = false
        private set

    fun move() {
        hasMoved = true
    }

    fun loadDrawable(drawable: Drawable) {
        this.drawable = drawable
    }

    fun pieceColor(): PieceColor {
        return color
    }

}


class Pawn(type: PieceType, color: PieceColor) : Piece(type, color) {
    override fun getPieceNotation(): String {
        return "" // Pawns do not have a specific notation
    }
}

class Rook(type: PieceType, color: PieceColor) : Piece(type, color) {
    override fun getPieceNotation(): String {
        return "R"
    }
}

class Knight(type: PieceType, color: PieceColor) : Piece(type, color) {
    override fun getPieceNotation(): String {
        return "N"
    }
}

class Bishop(type: PieceType, color: PieceColor) : Piece(type, color) {
    override fun getPieceNotation(): String {
        return "B"
    }
}

class Queen(type: PieceType, color: PieceColor) : Piece(type, color) {
    override fun getPieceNotation(): String {
        return "Q"
    }
}

class King(type: PieceType, color: PieceColor) : Piece(type, color) {
    override fun getPieceNotation(): String {
        return "K"
    }
}

