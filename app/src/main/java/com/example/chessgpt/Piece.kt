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

open class Piece(val type: PieceType, val color: PieceColor) {
    var drawable: Drawable? = null

    fun loadDrawable(drawable: Drawable) {
        this.drawable = drawable
    }

    fun pieceColor(): PieceColor {
        return color
    }

}


class Pawn(type: PieceType, color: PieceColor) : Piece(type, color)
class Rook(type: PieceType, color: PieceColor) : Piece(type, color)
class Knight(type: PieceType, color: PieceColor) : Piece(type, color)
class Bishop(type: PieceType, color: PieceColor) : Piece(type, color)
class Queen(type: PieceType, color: PieceColor) : Piece(type, color)
class King(type: PieceType, color: PieceColor) : Piece(type, color)
