package com.example.chessgpt

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var aiPlayer: ChatGPTAIPlayer
    private lateinit var tvGameState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Find the ChessboardView in your layout
        val chessboardView = findViewById<ChessboardView>(R.id.cv_chessboardView)

        // Create an instance of Chessboard or obtain an existing one
        val chessboard = Chessboard(this)

        // Initialize the AI player
        chessboardView.initAIPlayer(Constants.API)


        // Create an instance of ChatGPTAIPlayer with your API key
        aiPlayer = ChatGPTAIPlayer(Constants.API)

        // Load the drawables
        chessboardView.loadDrawables(this)

        // Set the initial text of tvGameState
        tvGameState = findViewById(R.id.tv_gameState)

        // Update the ChessboardView and trigger a redraw
        chessboardView.updateChessboard(chessboard)
        chessboardView.invalidate()

        // Set a callback for ChessboardView to update tvGameState whenever a move is made
        chessboardView.setOnMoveMadeListener { moveHistory, aiMove ->
            updateGameState(moveHistory, aiMove)
        }

    }


    // Function to update the tvGameState with the latest moveHistory
    private fun updateGameState(moveHistory: List<String>, aiMove: String?) {
        val gameStateText = "Game State:\n${moveHistory.joinToString(" ")}\nChatGPT Messages:\n$aiMove"
        tvGameState.text = gameStateText
    }


}