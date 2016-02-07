README for Chess AI

Author: Edrei Chua
Created on: 02/07/2016

*********** DIRECTORY STRUCTURE ***********

There are a few important files in this directory:

Report (directory for report)
    chessAI.pdf (detailed documentation of the code)
    chessAI.tex (tex file)
src (directory for source code)
    > AIMoveTask.java
    > AlphaBetaAI.java
    > BoardView.java
    > ChessAI.java
    > ChessClient.java
    > ChessGame.java
    > MinimaxAI.java
    > MoveMaker.java
    > RandomAI.java

book.pgn


*********** HOW TO START THE DEFAULT PROGRAM ***********

To start the program, compile all the .java files. Run ChessClient.java. The default setup will
have the AlphaBetaAI (without transposition table and with MAX_DEPTH = 7) as the BLACK player,
competing against RandomAI as the WHITE player. The default starting position is
"r5k1/p3Qpbp/2p3p1/1p6/q3bN2/6PP/PP3P2/K2RR3 b - - 0 1" and BLACK should be able to win in 3 moves.

*********** ADDITIONAL FUNCTIONALITY ***********

To change the chess game setup, change the constructor of ChessGame.java. By default, the setup is
"r5k1/p3Qpbp/2p3p1/1p6/q3bN2/6PP/PP3P2/K2RR3 b - - 0 1"

To change the players, modify line 74 - 75 of ChessClient.java. By default, the BLACK player is AlphaBetaAI
and the WHITE player is RandomAI. To play against the AI, change the WHITE player to
moveMaker[Chess.WHITE] = new TextFieldMoveMaker();

To include the transposition table implementation, change the value of the constant from
private final static boolean transpose = false to transpose = true
