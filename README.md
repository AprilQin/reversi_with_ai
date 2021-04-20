# reversi_with_ai
An implementation of the reversi game playing against AI

Includes functions allowing human playing aginst different AI implementations 
 
AI implementation # 1 - Monte Carlo Tree search:

10,000 random playouts for each valid move. The best move is determined by 
the number of wins + the number of draws - the number of loses of each move

AI implementation # 2 - Heuristic search AI

For the first 40 moves, the AI bias a move based on the weights associated with that location, 
when the game is approaching the last 25 moves, this AI bias toward the locations that leads to the maximum flips
