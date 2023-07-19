package com.example.chessgpt

data class Coordinate(val row: Int, val col: Int)

fun Coordinate.getAlgebraicNotation(): String {
    val file = ('a' + col).toString()
    val rank = ('1' + 7 - row).toString()
    return "$file$rank"
}
