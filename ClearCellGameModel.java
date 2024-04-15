package model;

import java.util.Random;

/**
 * This class extends GameModel and implements the logic of the clear cell game,
 * specifically.
 * 
 */

public class ClearCellGameModel extends GameModel {
	
	private final int[] H_DISP = {0, -1, 0, 1, -1, 1, -1, 0, 1}; //Horizontal indexes of all possible neighbors of the cell being cleared
	private final int[] V_DISP = {0, -1, -1, -1, 0, 0, 1, 1, 1}; //Vertical indexes of all possible neighbors of the cell being cleared
	private Random randInt; //Random number generator to be used when rows are randomly created
	int numRows;
	int numCols;
	int points; //A point is awarded for each cell that is cleared
	
	/**
	 * Defines a board with empty cells.  It relies on the
	 * super class constructor to define the board.
	 * 
	 * @param rows number of rows in board
	 * @param cols number of columns in board
	 * @param random random number generator to be used during game when
	 * rows are randomly created
	 */
	public ClearCellGameModel(int rows, int cols, Random random) {
		
		super(rows,cols);
		numRows = rows;
		numCols = cols;
		randInt = random;
	}

	/**
	 * The game is over when the last row (the one with index equal
	 * to board.length-1) contains at least one cell that is not empty.
	 */
	public boolean isGameOver() {
		
		//Traverses through the last row to if there is at least one cell not empty
		for(int col = 0; col < numCols; col++) {
			if(board[numRows - 1][col] != BoardCell.EMPTY) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the player's score.  The player should be awarded one point
	 * for each cell that is cleared.
	 * 
	 * @return player's score
	 */
	public int getScore() {
		
		return points;
	}

	
	/**
	 * This method must do nothing in the case where the game is over.
	 * 
	 * As long as the game is not over yet, this method will do 
	 * the following:
	 * 
	 * 1. Shift the existing rows down by one position.
	 * 2. Insert a row of random BoardCell objects at the top
	 * of the board. The row will be filled from left to right with cells 
	 * obtained by calling BoardCell.getNonEmptyRandomBoardCell().  (The Random
	 * number generator passed to the constructor of this class should be
	 * passed as the argument to this method call.)
	 */
	public void nextAnimationStep() {
		
		if(!isGameOver()) {
			BoardCell[][] boardCopy = board.clone();
			board = new BoardCell[numRows][numCols]; //Makes new board
			
			//Generates a random row at the top
			for(int col = 0; col < numCols; col++) {
				board[0][col] = BoardCell.getNonEmptyRandomBoardCell(randInt);
			}
			
			//Copies the previous board's rows in the new board and shifts them down by one position
			for(int row = 1; row < numRows; row++) {
				for(int col = 0; col < numCols; col++) {
					board[row][col] = boardCopy[row - 1][col];
				}
			}
		}
	}

	/**
	 * This method is called when the user clicks a cell on the board.
	 * If the selected cell is not empty, it will be set to BoardCell.EMPTY, 
	 * along with any adjacent cells that are the same color as this one.  
	 * (This includes the cells above, below, to the left, to the right, and 
	 * all in all four diagonal directions.)
	 * 
	 * If any rows on the board become empty as a result of the removal of 
	 * cells then those rows will "collapse", meaning that all non-empty 
	 * rows beneath the collapsing row will shift upward. 
	 * 
	 * @throws IllegalArgumentException with message "Invalid row index" for 
	 * invalid row or "Invalid column index" for invalid column.  We check 
	 * for row validity first.
	 */
	public void processCell(int rowIndex, int colIndex) {
		
		//If the row index is out of bounds
		if(rowIndex < 0 || rowIndex >= numRows) {
			throw new IllegalArgumentException("Invalid row index");
		}
		//If the column index is out of bounds
		if(colIndex < 0 || colIndex >= numCols) {
			throw new IllegalArgumentException("Invalid column index");
		}
		
		String clickedCell = getBoardCell(rowIndex, colIndex).getName();
		
		//If the clicked cell is not empty then proceed
		if(!clickedCell.equals(".")) {
			
			//Traverses throw the possible neighbors of the clicked cell
			for(int i = 0; i < H_DISP.length; i++) {
				
				int curRow = rowIndex + V_DISP[i];
				int curCol = colIndex + H_DISP[i];
				
				//If neighboring cell is not out of bounds
				if (canMove(curRow, curCol)) {
					
					//If the neighboring cell is the same color as the clicked cell, turn it to an empty cell
					if(board[curRow][curCol].getName().equals(clickedCell)) {
						board[curRow][curCol] = BoardCell.EMPTY;
						points++; //Updates points by +1
					}
				}
			}
			
			boolean emptyRow = true;
			int lastColoredRow = lastColoredRow();  
			
			//Traverses through all the rows to the last colored row
			for(int row = 0; row < lastColoredRow; row++) {
				for(int col = 0; col < numCols; col++) {
					
					//If the row has a non empty cell, the row is not empty
					if(board[row][col] != BoardCell.EMPTY) {
						emptyRow = false;
						col = numCols;
					}
				}
				
				if(emptyRow == true) {
					
					BoardCell[][] boardCopy = board.clone();
					
					//Starting at the row index with the empty row, shift all the rows below up by one
					for(int copyRow = row; copyRow < lastColoredRow; copyRow++) {
						for(int col = 0; col < numCols; col++) {
							board[copyRow][col] = boardCopy[copyRow + 1][col];
						}
					}
					//Traverses through the last row to make all the cells empty
					for(int col = 0; col < numCols; col++) {
						board[lastColoredRow][col] = BoardCell.EMPTY;
					}
					lastColoredRow--;	
				}else {
					emptyRow = true;
				}
			}
		}
	}
	
	//Checks if the next move is out of bounds or if the panel is empty
	public boolean canMove(int nextRow, int nextCol) {
		
		if(nextRow < 0 || nextCol < 0 || nextRow >= board.length || nextCol >= board[0].length) {
			return false;
		}
		else if(board[nextRow][nextCol] == BoardCell.EMPTY) {
			return false;
		}
		else {
			return true;
		}
	}
	
	//Traverses the board from the bottom row to the top row to find the first occurring row with at least one non empty cell  
	public int lastColoredRow() {
		
		boolean ifLastColoredRow = false;
		
		//Traverse the board from the bottom row to the very first row
		for(int row = numRows - 1; row >= 0; row--) {
			for(int col = 0; col < numCols; col++) {
				if(board[row][col] != BoardCell.EMPTY) { //If the row contains a non empty cell, it is the last colored row
					ifLastColoredRow = true;
					col = numCols;
				}
			}
			if(ifLastColoredRow == true) {
				return row;
			}
		}
		return 0;
	}
}
