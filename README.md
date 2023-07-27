# ChessGPT
this project is made with the help of ChatGPT to be an android chess game to play chess with chatGPT inside it.
Its the first prompt which I gave ChatGPT for creating this app: "hi, I want to create a chess game for android with kotlin using android studio, then I want to use chatGPT APIs so I could play chess with you. for example I make a move and then you based on what you know about playing chess, guessing the best possible move. but I dont know if its possible and if yes how I can make your move implemented in the app, I mean I know that you can guess a good possible move with knowing the situation of the chess pieces on the board, but how could I write my app so I can play with you."
and then ChatGPT answered that it is possible to make such an app. it gave me some general steps to do in case of creating that app.
after a month of Q&A with ChatGPT In the following about how to create each part of the app and how to connect different parts together, adjusting the chess board, the rules of chess, how to write chess moves in algebraic notation, and send them to ChatGPT, how to read ChatGPT suggested moves and turn them into actual move on the board, debugging, etc.. finally I created the desired chess game and played chess with ChatGPT.

How to play:
Put your own OpenAI API in the Constants file.
Launch the app on an android phone/tablet with android API level 21 (android version 5) or higher.
You play as white and ChatGPT plays as black.
Drag and drop your piece from source to destination.
After that you made your move, if ChatGPT did not made its move (it could happen due to connection error, busy OpenAI API servers or suggesting invalid move from ChatGPT), wait 5-10 seconds and then simply just tap on one of your pieces so ChatGPT receives a new request to make a move.
