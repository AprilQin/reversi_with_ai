/**
 * This is a complete implementation of the game reversi.
 * Includes functions allowing human playing aginst different AI implementations 
 * 
 *          AI implementation # 1 - Monte Carlo Tree search:
 * 
 * 10,000 random playouts for each valid move. The best move is determined by 
 * the number of wins + the number of draws - the number of loses of each move
 * 
 *          AI implementation # 2 - Heuristic search AI
 * 
 * For the first 40 moves, the AI bias a move based on the weights associated with that location, 
 * when the game is approaching the last 25 moves, this AI bias toward the locations that leads to the maximum flips
 */


import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;


public class Reversi {
    public int boardSize = 8;
    public char[][] board = new char[boardSize][boardSize];
    public char[][] positions;
    public boolean player_turn = true;

    // hardcoded chosen weights for calculating the best move
    public int[][] weights = new int[][]{
        {100,-20,10,5,5,10,-20,100},
        {-20,-50,-2,-2,-2,-2,-50,-20},
        {10,-2,-1,-1,-1,-1,-2,10},
        {5,-2,-1,-1,-1,-1,-2,5},
        {-1,-1,-1,-1,-1,-1,-2,5},
        {-1,-1,-1,-1,-1,-1,-2,10},
        {-1,-1,-1,-1,-1,-1,-50,-20},
        {100,-1,-1,-1,-1,-1,-20,100},
    };  

    char player = 'B';
    char ai = 'W';
    char draw = 'D';
    char winner;
    int mct = 1;
    int heuristic = 2;
    int maxPlayout = 10000;
    int spaceRemain = boardSize * boardSize - 4;

    public Reversi() {
        // constructor
        boardInit();
    }


    /**
     * Create an empty new board
     */
    public char[][] createNewBoard() {
        char[][] b = new char[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++)
                b[i][j] = ' ';
        }
        return b;
    }

    /**
     * set the middle 4 positions to be 2 white and 2 black
     */
    public char[][] boardInit(){
        board = createNewBoard();
        board[3][3] = player;
        board[4][4] = player;
        board[3][4] = ai;
        board[4][3] = ai;
        return board;
    }

    /**
     * make a clone of the current board state
     */
    public char[][] cloneGameState() {
        char[][] clone = new char[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++)
                clone[i][j] = board[i][j];
        }
        return clone;
    }

    /**
     * print the current board state in console
     */
    public void printBoard(char[][] b) {
        System.out.println("-".repeat(20));
        System.out.println("   1 2 3 4 5 6 7 8");
        for (int i = 0; i < boardSize; i++) {
            String row = i + 1 + " |";
            for (int j = 0; j < boardSize; j++) {
                row += b[i][j];
                row += "|";
            }
            System.out.println(row);
        }
        System.out.println("-".repeat(20));
    }

    public void printBoard() {
        printBoard(board);
        System.out.println("Current count for Black tokens: " + countCoins(player, board));
        System.out.println("Current count for White tokens: " + countCoins(ai, board)); 
        System.out.println(); 
    }

    /**
     * validate an index 
     */
    public boolean validIndex(int row, int col) {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }


    /**
     * find valid positions for the current player based on reversi game logic
     */
    public ArrayList<char[][]> findValidPositions(char who, char game[][]) {
        char opponent = ai;
        if (who == ai) {
            opponent = player;
        }
        ArrayList<char[][]> validLocations = new ArrayList<char[][]>();
        positions = cloneGameState();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (game[i][j] == ' ') {
                    // empty slot
                    boolean validPos = false;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) {
                                continue;
                            }
                            int neighbourX = i + dr;
                            int neighbourY = j + dc;                           
                            char[][] b = createNewBoard();
                            b[i][j] = '*';
                            
                            // when neighbour on this direction is opponent
                            if (validIndex(neighbourX, neighbourY) && game[neighbourX][neighbourY] == opponent) {    
                                b[neighbourX][neighbourY] = 'F';
                                neighbourX += dr;
                                neighbourY += dc;
                                // check this direction leads to itself
                                while (validIndex(neighbourX, neighbourY)) {
                                    if (game[neighbourX][neighbourY] == ' ') {
                                        break;
                                    }       
                                    // mark the opponent index to be F
                                    else if (game[neighbourX][neighbourY] == opponent) {
                                        b[neighbourX][neighbourY] = 'F';
                                        neighbourX += dr;
                                        neighbourY += dc;
                                    } 
                                    else if(game[neighbourX][neighbourY] == who){
                                        validLocations.add(b);
                                        validPos = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (validPos) {
                        positions[i][j] = '*';
                    }
                }
            }
        }
        return validLocations;
    }

    public void changeTurn() {
        player_turn = !player_turn;
    }

    public void flip(char who, char[][] b, char[][] game) {
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (b[x][y] == 'F' || b[x][y] == '*') {
                    game[x][y] = who;
                }
            }
        }
    }

    public void makeMove(char who, int row, int col, ArrayList<char[][]> validLocations) {
        // return: successfully find the location and flip the opponents
        for (int i = 0; i < validLocations.size(); i++) {

            // find the index in validLications
            char[][] b = validLocations.get(i);
            if (b[row][col] == '*') {
                flip(who, b, board);
            }
        }
        changeTurn();
    }

    public boolean checkWinner(int spaceLeft, char[][] game) {
        boolean gameEnd = false;
        // 1: every space is occupied 2. no one can make another move
        if (spaceLeft == 0) {
            gameEnd = true;
        }else{
            ArrayList<char[][]> validLocations = findValidPositions(ai, game);
            int ai_choices = validLocations.size();
            validLocations = findValidPositions(player, game);
            int player_choices = validLocations.size();
        
            if (ai_choices == 0 && player_choices == 0){
                gameEnd = true;
            }
        }
        
        if (gameEnd){
            int blackCount = countCoins(player, game);
            int whiteCount = countCoins(ai, game);

            if (blackCount > whiteCount){
                winner = player;
            }else if(blackCount < whiteCount){
                winner = ai;
            }else{
                winner = draw;
            }
        }
        return gameEnd;
    }

    public int countCoins(int who, char game[][]) {
		int coins = 0;
		for (int i = 0; i < boardSize; i++){
			for (int j = 0; j < boardSize; j++) {
				if (game[j][i] == who)
					coins++;
            }
        }
		return coins;
	}

    public int randomChoice(int min, int max) {
        // max not included
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public void randomPlayout(char[][] game) {
        boolean gameEnd = false;
        char who = player;
        int spaceLeft = spaceRemain-1;
        while (!gameEnd) {
            if (who == player) {
                ArrayList<char[][]> validLocations = findValidPositions(player, game);
                int choice = 0;
                int numChoices = validLocations.size();
                if (numChoices == 1){
                    flip(player, validLocations.get(choice), game);
                    // printBoard(game);
                    spaceLeft --;
                }
                else if (numChoices > 1){
                    choice = randomChoice(0, numChoices);
                    flip(player, validLocations.get(choice), game);
                    // printBoard(game);
                    spaceLeft --;
                }
                who = ai;
            } else {
                ArrayList<char[][]> validLocations = findValidPositions(ai, game);

                int choice = 0;
                int numChoices = validLocations.size();
                if (numChoices == 1){
                    flip(ai, validLocations.get(choice), game);
                    // printBoard(game);
                    spaceLeft --;
                }
                else if (numChoices > 1){
                    choice = randomChoice(0, numChoices);
                    flip(ai, validLocations.get(choice), game);
                    // printBoard(game);
                    spaceLeft --;
                }
                who = player;

            }
            gameEnd = checkWinner(spaceLeft, game);
        }
    }

    public int runPlayouts(char[][] game) {
        int win = 0;
        int loss = 0;
        int draw = 0;
        for (int i = 0; i < maxPlayout; i++) {
            randomPlayout(game);
            if (winner == ai) {
                win++;
            } else if (winner == player) {
                loss++;
            } else {
                draw++;
            }
        }
        return win + draw - loss;
    }

    public int MCTSearch(ArrayList<char[][]> validLocations) {
        int bestMove = 0;
        int maxValue = 0;

        long startTime = System.nanoTime();

        for (int i = 0; i < validLocations.size(); i++) {
            char[][] clone = cloneGameState();
            flip(ai, validLocations.get(i), clone);
            // printBoard(clone);
            int value = runPlayouts(clone);
            
            if (value > maxValue) {
                maxValue = value;
                bestMove = i;
            }
        }

        long stopTime = System.nanoTime();
        long time = (stopTime - startTime)/1000000;
        long seconds = time / 1000;
        System.out.println("valid locations: " + validLocations.size());
        System.out.println("Total time elapsed = " + seconds);

        return bestMove;
    }

    public boolean checkCorners(int x, int y){
        if (x==0 && y == 0 || 
        x==boardSize-1 && y ==boardSize-1 ||
        x==0 && y ==boardSize-1 ||
        x==boardSize-1 && y==0){
            return true;
        }else{
            return false;
        }
    }

    int absoluteSearch(char who, char opponent){
        int bestMove = 0;
        int maxCoins = 0;

        // maximum flippable coins
        ArrayList<char[][]> validLocations = findValidPositions(who, board);
        for(int i=0; i<validLocations.size(); i++){
            char b[][] = validLocations.get(i);
            int coins = 0;
            for (int x = 0; x < boardSize; x++){
                for (int y = 0; y < boardSize; y++) {
                    if (b[x][y]=='*' || b[x][y] == 'F'){
                        coins++;
                    }
                }
            }
            if (coins > maxCoins){
                maxCoins = coins;
                bestMove = i;
            }
        }
        return bestMove;
    }

    public int positionalSearch(char who, char opponent){
        int maxWeight = -100;
        int bestMove = 0;
        ArrayList<char[][]> validLocations = findValidPositions(who, board);
        for(int i=0; i<validLocations.size(); i++){
            int weight = 0;
            char b[][] = validLocations.get(i);

            // calculate weights if place the move here
            for (int x = 0; x < boardSize; x++){
                for (int y = 0; y < boardSize; y++) {
                    if (b[x][y]=='*' || b[x][y] == 'F' || board[x][y] == who){
                        weight += weights[x][y];
                    }
                    if (board[x][y] == opponent){
                        weight -= weights[x][y];
                    }
                }
            }
            if (weight > maxWeight){
                maxWeight = weight;
                bestMove = i;
            }
        }
        return bestMove;
    }

    public int heuristicSearch(char who){
        char opponent = player;
        if (who == player){
            opponent = ai;
        }
        int bestMove = 0;
        System.out.println("Heuristic AI making a move");
        // beginning to middle phase of the game, use positional strategy
        findValidPositions(who, board);
        if (spaceRemain > 25){
            bestMove = positionalSearch(who, opponent);
        }   
        // end phase: use absolute
        else{
            bestMove = absoluteSearch(who, opponent);
        }
        return bestMove;
    }

    public void AIMakeMove(int heuristic, char who) {
        // make a move & change turn
        ArrayList<char[][]> validLocations = findValidPositions(who, board);
        int bestMove = 0;
        
        if (validLocations.size() != 0){
            if (validLocations.size() > 1) {
                if (heuristic == mct) {
                    System.out.println("Pure MCT AI making a move");
                    bestMove = MCTSearch(validLocations);
                }
                else{
                    bestMove = heuristicSearch(who);
                }
            }
            flip(who, validLocations.get(bestMove), board);
        }
        changeTurn();
    }

    public void runAIcompetition(int rounds){
        // printBoard();
        int mct_win = 0;
        int heuristic_win = 0;
        int ai_draw = 0;
        for (int i = 0; i < rounds; i++){
            boolean gameOver = false;
            board = boardInit();
            while(!gameOver){
                if(player_turn){
                    AIMakeMove(heuristic, player);
                    // printBoard();
                }
                else{
                    AIMakeMove(mct, ai);
                    // printBoard();
                }
                gameOver = checkWinner(spaceRemain, board);
                if (gameOver){
                    if (winner == player){
                        System.out.println("The winner is Heuristic AI");
                    }else if (winner == ai){
                        System.out.println("The winner is Pure MCT AI");
                    }else{
                        System.out.println("It's a draw");
                    }
                }
            }
            if (winner == ai){
                mct_win++;
            }else if(winner == player){
                heuristic_win ++;
            }else{
                ai_draw++;
            }
        }
        System.out.println("MCT wins: " + mct_win + " times");   
        System.out.println("Heuristic wins: " + heuristic_win + " times");
        System.out.println("Draw: " + ai_draw + " times");   
    }

    public void runHumanAIcompetition(Scanner sc, int choice){
        boolean game_over = false;
        while (!game_over) {
            if (player_turn) {
                System.out.println("Player making a move: ");
                ArrayList<char[][]> validLocations = findValidPositions(player, board);
                
                boolean validSeleciton = false;
                int row = 0;
                int col = 0;
                while (!validSeleciton) {
                    try {
                        printBoard(positions);
                        System.out.println("Please enter your move, row number first, then colum number, such as 11 for first row, first column, no space pls:");
                        String str = sc.nextLine();
                        row = Integer.parseInt(String.valueOf(str.charAt(0))) -1;
                        col = Integer.parseInt(String.valueOf(str.charAt(1))) -1;
                        if (validIndex(row, col) && positions[row][col] == '*'){
                            validSeleciton = true;
                            makeMove(player, row, col, validLocations);
                            game_over = checkWinner(spaceRemain, board);
                            printBoard();
                        }else{
                            System.out.println("Invalid input, please try again.");
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid input format, please try again.");
                    }
                }
            } else {
                AIMakeMove(choice, ai);
                game_over = checkWinner(spaceRemain, board);
            }
            if (game_over){
                printBoard();
                if (winner == player){
                    System.out.println("The winner is the Human");
                }else if (winner == ai){
                    System.out.println("The winner is AI");
                }else{
                    System.out.println("It's a draw");
                }
            }
        }
    }
}