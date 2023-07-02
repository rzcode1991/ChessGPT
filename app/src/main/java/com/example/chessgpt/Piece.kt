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

open class Piece(val type: PieceType) {
    var drawable: Drawable? = null

    fun loadDrawable(drawable: Drawable) {
        this.drawable = drawable
    }

}

class Pawn(type: PieceType) : Piece(type)
class Rook(type: PieceType) : Piece(type)
class Knight(type: PieceType) : Piece(type)
class Bishop(type: PieceType) : Piece(type)
class Queen(type: PieceType) : Piece(type)
class King(type: PieceType) : Piece(type)