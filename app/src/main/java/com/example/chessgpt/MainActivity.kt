package com.example.chessgpt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Find the ChessboardView in your layout
        val chessboardView = findViewById<ChessboardView>(R.id.cv_chessboardView)

        // Create an instance of Chessboard or obtain an existing one
        val chessboard = Chessboard(this)

        // Load the drawables
        chessboardView.loadDrawables(this)

        // Update the ChessboardView and trigger a redraw
        chessboardView.updateChessboard(chessboard)
        chessboardView.invalidate()



    }


}