import java.util.ArrayList;
import java.util.Random;

// ======================================================================
// FILE:        MyAI.java
//
// AUTHOR:      Abdullah Younis
//
// DESCRIPTION: This file contains your agent class, which you will
//              implement. You are responsible for implementing the
//              'getAction' function and any helper methods you feel you
//              need.
//
// NOTES:       - If you are having trouble understanding how the shell
//                works, look at the other parts of the code, as well as
//                the documentation.
//
//              - You are only allowed to make changes to this portion of
//                the code. Any changes to other portions of the code will
//                be lost when the tournament runs your code.
// ======================================================================

public class MyAI extends Agent
{
	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

		for(int i = 0; i < 4; i++) {
			for (int j = 0; j < 7; j++) {
				myBoard[i][j] = new Tile();
			}
		}
		
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}
	
	public Action getAction
	(
		boolean stench,
		boolean breeze,
		boolean glitter,
		boolean bump,
		boolean scream
	)
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

		// If bumped, take one step back,
		// mark the border where we bumped,
		// and remove related log.
		if(bump) {
			if(dir == 0)
				x--;
			if(dir == 3)
				y--;
			setBorder();
			dirLog.remove(dirLog.size()-1);
		}

		// If glitter is perceived, we grab the gold,
		// and get ready to trace back to where we started.
		if(glitter) {
			traceBack = true;
			return Action.GRAB;
		}
		
		myBoard[x][y].visited = true;

		// If stench is detected at current tile,
		// we shoot 4 times at every direction.
		if(!completeShoot) {
			if(stench) {
				if(dir == 0) {
					completeShoot = true;
					return Action.SHOOT;
				} else if(dir == 3) {
					dir = 0;
					return Action.TURN_RIGHT;
				} else if(dir == 1) {
					dir = 0;
					return Action.TURN_LEFT;
				} else if(dir == 2) {
					dir = 1;
					return Action.TURN_LEFT;
				}
			}
		}

		if(!completeMove) {
			switch(goingTo) {
				case 0:
					return goEast();
				case 1:
					return goSouth();
				case 2:
					return goWest();
				case 3:
					return goNorth();
			}
		}

		if(scream) {
			isDead = true;
		}

		if(stepBack || traceBack) {
			return goHome(dirLog.size());
		}
		
		setNotPit(breeze);
		setNotWumpus(stench);
		
		return findSafeTile();
		
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}
	
	// ======================================================================
	// YOUR CODE BEGINS
	// ======================================================================
	
	// Initialization
	int x 	= 0;	// current x
	int y 	= 0;	// current y
	int dir = 0;	// current direction
	
	int goingTo = -1;
	boolean completeMove = true;
	boolean isDead	 = false;
	boolean completeShoot= false;
	boolean stepBack = false;
	boolean traceBack= false;
	
	ArrayList<Integer> dirLog = new ArrayList<Integer>();
	public Random rand = new Random();

	// Tile Structure
	public class Tile
	{
		boolean notPit		= false;
		boolean notWumpus	= false;
		boolean visited		= false;
		boolean isBrick		= false;
	}

	// Board Initialization
	Tile[][] myBoard = new Tile[4][7];
	
	public boolean isValidTile(int a, int b) {
		if(a < 0 || b < 0 || a > 3 || b > 6) {
			return false;
		}
		return true;
	}

	public void setBorder() {
		if(dir == 0) {
			for(int i = x + 1; i < 4; i++) {
				for(int j = 0; j < 7; j++) {
					myBoard[i][j].isBrick = true;
				}
			}
		} else if(dir == 3) {
			for(int i = 0; i < 4; i++) {
				for(int j = y + 1; j < 7; j++) {
					myBoard[i][j].isBrick = true;
				}
			}
		}
	}

	// If the current tile is not a pit and there is no breeze,
	// then adjacent tiles must not be a pit.
	public void setNotPit(boolean breeze) {
		myBoard[x][y].notPit = true;
		if(!breeze) {
			if(isValidTile(x, y+1)) {
				myBoard[x][y+1].notPit = true;
			}
			if(isValidTile(x, y-1)) {
				myBoard[x][y-1].notPit = true;
			}
			if(isValidTile(x+1, y)) {
				myBoard[x+1][y].notPit = true;
			}
			if(isValidTile(x-1, y)) {
				myBoard[x-1][y].notPit = true;
			}
		}
	}

	// If the current tile has no Wumpus and there is no stench,
	// then there is no Wumpus at adjacent tiles.
	public void setNotWumpus(boolean stench) {
		myBoard[x][y].notWumpus = true;
		if(!stench) {
			if(isValidTile(x, y+1)) {
				myBoard[x][y+1].notWumpus = true;
			}
			if(isValidTile(x, y-1)) {
				myBoard[x][y-1].notWumpus = true;
			}
			if(isValidTile(x+1, y)) {
				myBoard[x+1][y].notWumpus = true;
			}
			if(isValidTile(x-1, y)) {
				myBoard[x-1][y].notWumpus = true;
			}
		}
	}

	// A tile where there is no pit or Wumpus is considered safe.
	public boolean isSafe(int x, int y) {
		if(myBoard[x][y].notPit && myBoard[x][y].notWumpus)
			return true;
		return false;
	}

	// Randomly choose an adjacent tile.
	// If it is valid, safe, unvisited, and not a brick,
	// then we go there.
	// If all adjacent tiles do not meet the above criteria,
	// we step back once;
	// but if there is no way back (at tile(0, 0)), we climb up.
	public Action findSafeTile() {
		boolean c0 = true, c1 = true, c2 = true, c3 = true;
		
		while(c0 || c1 || c2 || c3) {
			int n = rand.nextInt() % 4;
			switch(n) {
			case 0:
				if(isValidTile(x+1, y) && isSafe(x+1, y) && !myBoard[x+1][y].visited && !myBoard[x+1][y].isBrick) {
					dirLog.add(0);
					return goEast();
				} else if(isValidTile(x+1, y) && myBoard[x+1][y].notPit && isDead && !myBoard[x+1][y].visited && !myBoard[x+1][y].isBrick) {
					dirLog.add(0);
					return goEast();
				} else {
					c0 = false;
				}
			case 1:
				if(isValidTile(x, y-1) && isSafe(x, y-1) && !myBoard[x][y-1].visited && !myBoard[x][y-1].isBrick) {
					dirLog.add(1);
					return goSouth();
				} else if(isValidTile(x, y-1) && myBoard[x][y-1].notPit && isDead && !myBoard[x][y-1].visited && !myBoard[x][y-1].isBrick) {
					dirLog.add(1);
					return goSouth();
				} else {
					c1 = false;
				}
			case 2:
				if(isValidTile(x-1, y) && isSafe(x-1, y) && !myBoard[x-1][y].visited && !myBoard[x-1][y].isBrick) {
					dirLog.add(2);
					return goWest();
				} else if(isValidTile(x-1, y) && myBoard[x-1][y].notPit && isDead && !myBoard[x-1][y].visited && !myBoard[x-1][y].isBrick) {
					dirLog.add(2);
					return goWest();
				} else {
					c2 = false;
				}
			case 3:
				if(isValidTile(x, y+1) && isSafe(x, y+1) && !myBoard[x][y+1].visited && !myBoard[x][y+1].isBrick) {
					dirLog.add(3);
					return goNorth();
				} else if(isValidTile(x, y+1) && myBoard[x][y+1].notPit && isDead && !myBoard[x][y+1].visited && !myBoard[x][y+1].isBrick) {
					dirLog.add(3);
					return goNorth();
				} else {
					c3 = false;
				}
			}
		}
		if(x == 0 && y == 0) {
			return Action.CLIMB;
		} else {
			stepBack = true;
			return goHome(dirLog.size());
		}
	}

	// Go east once.
	public Action goEast() {
		completeMove = false;
		goingTo = 0;
		if(dir == 0) {
			completeMove = true;
			x++;
			return Action.FORWARD;
		} else if(dir == 1) {
			dir = 0;
			return Action.TURN_LEFT;
		} else if(dir == 2) {
			dir = 1;
			return Action.TURN_LEFT;
		} else {
			dir = 0;
			return Action.TURN_RIGHT;
		}
	}

	// Go south once.
	public Action goSouth() {
		completeMove = false;
		goingTo = 1;
		if(dir == 1) {
			completeMove = true;
			y--;
			return Action.FORWARD;
		} else if(dir == 2) {
			dir = 1;
			return Action.TURN_LEFT;
		} else if(dir == 3) {
			dir = 2;
			return Action.TURN_LEFT;
		} else {
			dir = 1;
			return Action.TURN_RIGHT;
		}
	}

	// Go west once.
	public Action goWest() {
		completeMove = false;
		goingTo = 2;
		if(dir == 2) {
			completeMove = true;
			x--;
			return Action.FORWARD;
		} else if(dir == 3) {
			dir = 2;
			return Action.TURN_LEFT;
		} else if(dir == 0) {
			dir = 3;
			return Action.TURN_LEFT;
		} else {
			dir = 2;
			return Action.TURN_RIGHT;
		}
	}

	// Go north once.
	public Action goNorth() {
		completeMove = false;
		goingTo = 3;
		if(dir == 3) {
			completeMove = true;
			y++;
			return Action.FORWARD;
		} else if(dir == 0) {
			dir = 3;
			return Action.TURN_LEFT;
		} else if(dir == 1) {
			dir = 0;
			return Action.TURN_LEFT;
		} else {
			dir = 3;
			return Action.TURN_RIGHT;
		}
	}

	// Step back once and remove related log.
	public Action goHome(int size) {
		if(x == 0 && y == 0) {
			return Action.CLIMB;
		}
		int a = dirLog.get(size-1);
		dirLog.remove(size-1);
		stepBack = false;
		switch(a) {
			case 0:
				return goWest();
			case 1:
				return goNorth();
			case 2:
				return goEast();
			case 3:
				return goSouth();
		}
	}
	
	// ======================================================================
	// YOUR CODE ENDS
	// ======================================================================
}
