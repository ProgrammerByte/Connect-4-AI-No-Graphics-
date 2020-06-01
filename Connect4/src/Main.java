import java.util.Scanner;

public class Main {
	//All of the following is related to AI. In this case player "O" is the AI and all fitnesses are calculated relative to it
	static int currentTurn;
	
	public static int calculateFitness(String[][] board) {
		int fitness = 0;
		for (int i = 0; i < 6; i++) {
			if (board[i][3].equals("O")) { //if piece is in the centre
				fitness += 3;
			}
		}
		//max coord = 5, 6
		int[][] combinations = new int[][] {{1, 0, 0, 0, 2, 6},
											{0, 1, 0, 0, 5, 3}, 
											{1, 1, 0, 0, 2, 3}, 
											{1, -1, 0, 4, 2, 6}}; //iDir, xDir, iMin, xMin, iMax, xMax
											
		int iVal, xVal; //current coordinates
		int xCount, oCount; //Counts player tiles in a line
		
		for (int a = 0; a < 4; a++) { //every combination
			for (int i = combinations[a][2]; i <= combinations[a][4]; i++) { //for every valid start position for a line
				for (int x = combinations[a][3]; x <= combinations[a][5]; x++) {
					//this inner loop should be executed exactly 69 times from what I calculated previously
					
					xCount = 0;
					oCount = 0;
					for (int b = 0; b < 4; b++) { //checks a given line
						iVal = i + b * combinations[a][0];
						xVal = x + b * combinations[a][1];
						if (board[iVal][xVal].equals("O")) {
							oCount += 1;
						}
						else if (board[iVal][xVal].equals("X")) {
							xCount += 1;
						}
					}
					
					if (oCount == 2 && xCount == 0) { //line of two TODO - MAYBE CLEAN UP THIS PART OF THE CODE SO THAT IT LOOKS BETTER AND IS BETTER OPTIMISED
						fitness += 2;
					}
					else if (oCount == 3 && xCount == 0) { //line of three
						fitness += 7;
						
						for (int b = 0; b < 4; b++) {
							iVal = i + b * combinations[a][0];
							xVal = x + b * combinations[a][1];
							if (board[iVal][xVal].equals("-")) {
								if (iVal != 5) {
									if (board[iVal + 1][xVal].equals("-") == false) {
										fitness += 10;
									}
									break;
								}
								fitness += 10;
								break;
							}
						}
					}
					else if (oCount == 4) {
						fitness += 10000000;
					}
					else if (oCount == 0 && xCount == 2) {
						fitness -= 4;
					}
					else if (oCount == 0 && xCount == 3) { //Not necessarily winnable - come back to this later
						fitness -= 10; //if just 3 in a row
						
						for (int b = 0; b < 4; b++) {
							iVal = i + b * combinations[a][0];
							xVal = x + b * combinations[a][1];
							if (board[iVal][xVal].equals("-")) {
								if (iVal != 5) {
									if (board[iVal + 1][xVal].equals("-") == false) {
										fitness -= 100000;
									}
									break;
								}
								fitness -= 100000;
								break;
							}
						}
					}
					else if (xCount == 4) { //only used for min/max algorithm
						fitness -= 1000000;
					}
				}
			}
		}
		
		return fitness;
	}
	
	public static int AiMove(String[][] board, int turns) {
		String[][][] possibilities = generateBoards(board, turns);
		int[] fitnesses = new int[possibilities.length];
		for (int i = 0; i < possibilities.length; i++) {
			if (possibilities[i][0][0].equals("null")) {
				fitnesses[i] = -123456789; //indicates an impossible board hence should be avoided at all costs whether the player is max or min.
			}
			else {
				fitnesses[i] = calculateFitness(possibilities[i]); //turns all boards into a fitness value
			}
		}
		
		return minMax(fitnesses, 0);
	}
	
	public static int minMax(int[] fitnesses, int depth) {
		depth += 1; //depth starts at zero
		int length = fitnesses.length;
		int multiplier = new int[] {-1, 1}[depth % 2]; //relating to min, and max players respectively
		int[] result = new int[length / 7];
		int[] indexes = new int[length / 7];
		for (int i = 0; i < length / 7; i++) {
			result[i] = -123456789 * multiplier; //This was the cause of a previous problem
		}
		
		for (int i = 0; i < length; i++) {
			if (result[i / 7] * multiplier < fitnesses[i] * multiplier) {
				if (fitnesses[i] != -123456789) {
					result[i / 7] = fitnesses[i];
					indexes[i / 7] = i % 7;
				}
			}
		}
		
		for (int i = 0; i < length / 7; i++) {
			if (result[i] == 123456789) {
				result[i] = -123456789;
			}
		}
		
		
		if (result.length > 1) {
			return minMax(result, depth);
		}
		else {
			return indexes[0];
		}
	}
	
	public static String[][][] generateBoards(String[][] board, int turns) { //how many turns to look ahead
		int[] powers = new int[turns];
		int[] indexes = new int[turns];
		powers[0] = 1;
		for (int i = 1; i < turns; i++) {
			powers[i] = 7 * powers[i - 1];
		}
		
		String[][][] allBoards = new String[powers[turns - 1] * 7][][];
		for (int i = 0; i < allBoards.length; i++) {
			allBoards[i] = new String[][] {{"null"}}; //"null" indicates a move which is not possible
		}
		recursiveRoutine(allBoards, board, turns, 0, indexes, powers);
		
		return allBoards;
	}
	
	public static void recursiveRoutine(String[][][] allBoards, String[][] currentBoard, int turns, int depth, int[] indexes, int[] powers) {
		depth += 1;
		String[] players = {"X", "O"}; //ASSUMES DEPTH STARTS AT 0
		String[][] nextBoard;
		boolean verification;
		for (int i = 0; i < 7; i++) {
			indexes[turns - depth] = i;
			
			nextBoard = cloneBoard(currentBoard);
			verification = false;
			//TODO - currentTurn is newly added to stop new boards from being generated if a tie has occured TODO - IF AN ERROR OCCURS THEN IT IS MOST LIKELY RELATED TO THE if (currentTurn + depth == 43) statement
			if (winCheck(currentBoard, "O") == true || winCheck(currentBoard, "X") == true || currentTurn + depth == 43) { //if a player has won then add no more pieces, however count the board as valid
				verification = true;
			}
			else if (addPiece(nextBoard, players[depth % 2], i) == true) { //if the column is full then the board is no longer valid and it should be discarded
				verification = true;
			}
			
			if (verification == true) { //if no player has won - DONT ADD A NEW PIECE IF A PLAYER HAS WON
				if (depth < turns) {
					recursiveRoutine(allBoards, nextBoard, turns, depth, indexes, powers);
				}
				else { //depth == turns
					int currentIndex = 0;
					for (int x = 0; x < turns; x++) {
						currentIndex += indexes[x] * powers[x];
					}
					allBoards[currentIndex] = cloneBoard(nextBoard);
				}
			}
		}
	}
	
	
	
	public static String[][] cloneBoard(String[][] board) {
		String[][] result = new String[6][7];
		for (int i = 0; i < 6; i++) {
			result[i] = board[i].clone();
		}
		return result;
	}
	
	
	
	
	

	//All of the following is the base game
	public static void outputBoard(String[][] board) {
		for (int i = 0; i < 6; i++) {
			for (int x = 0; x < 7; x++) {
				System.out.print(board[i][x] + " ");
			}
			System.out.println("");
		}
	}
	
	public static boolean addPiece(String[][] board, String piece, int index) {
		if (index >= 0 && index < 7) {
			for (int i = 5; i >= 0; i--) {
				if (board[i][index].equals("-")) {
					board[i][index] = piece;
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean winCheck(String[][] board, String piece) {
		int[][] combinations = new int[][] {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
		int iVal, xVal;
		for (int i = 0; i < 6; i++) {
			for (int x = 0; x < 7; x++) {
				if (board[i][x].equals(piece)) { //locates pieces
					
					for (int a = 0; a < 4; a++) { //checks if won
						for (int b = 1; b < 4; b++) {
							iVal = i + (b * combinations[a][0]);
							xVal = x + (b * combinations[a][1]);
							if (iVal >= 0 && iVal < 6 && xVal >= 0 && xVal < 7) {
								if (board[iVal][xVal].equals(piece) == false) {
									break;
								}
							}
							else {
								break;
							}
							if (b == 3) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		currentTurn = 0;
		String currentPlayer;
		String[] players = new String[] {"X", "O"};
		String[][] board = new String[6][7];
		for (int i = 0; i < 6; i++) {
			for (int x = 0; x < 7; x++) {
				board[i][x] = "-";
			}
		}
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Enter a number between 1 and 7 to place your piece in the respective column!");
		outputBoard(board);
		while (true) {
			
			currentPlayer = players[currentTurn % 2];
			System.out.print("It's player " + currentPlayer + "'s turn: ");
			
			if (currentPlayer.equals("X")) { //if player
				while (true) {
					if (addPiece(board, currentPlayer, scan.nextInt() - 1) == true) {
						break;
					}
					System.out.println("Invalid Input! ");
				}
			}
			else { //if AI
				System.out.println("");
				addPiece(board, currentPlayer, AiMove(board, 7));
			}
			outputBoard(board);
			
			if (winCheck(board, currentPlayer) == true) {
				System.out.println("Player " + currentPlayer + " wins!");
				break;
			}
			
			currentTurn += 1;
			
			if (currentTurn == 42) {
				System.out.println("Tie - Nobody wins this time!");
				break;
			}
		}
		scan.close();
	}
}
