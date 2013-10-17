/* MachinePlayer.java */

package player;

/**
 *  An implementation of an automatic Network player.  Keeps track of moves
 *  made by both players.  Can select a move for itself.
 */
public class MachinePlayer extends Player {
	protected GameNode[][] gameboard;
	private int whichSide;
	private int rounds;
	private int sDepth;
	private int howmanyPath;
	final static int BLACK = 0;
	final static int WHITE = 1;
	final static int EMPTY = -1;
	final static int SIZE = 8; //the width and height of the gameboard


	// Creates a machine player with the given color.  Color is either 0 (black)
	// or 1 (white).  (White has the first move.)
	public MachinePlayer(int color) {
		gameboard = new GameNode[SIZE][SIZE];
		for (int i = 0; i<SIZE; i++) {
			for (int j = 0; j<SIZE; j++) {
				gameboard[i][j] = new GameNode();
			}			
		}
		whichSide = color;
		/*if (color == 1) {
			whichSide = WHITE;
		} else {
			whichSide = BLACK;
		}*/
		sDepth = 3;
		rounds = 0;
	}

	// Creates a machine player with the given color and search depth.  Color is
	// either 0 (black) or 1 (white).  (White has the first move.)
	public MachinePlayer(int color, int searchDepth) {
		this(color);
		sDepth = searchDepth;
	}

	/*
	 * MiniMax algorithm with alpha-beta pruning
	 * Return a BestStep object that contains the best Move and best score
	 */
	private BestStep bestMove(int color, int alpha, int beta, int depth) {
		BestStep myBest = new BestStep();
		BestStep reply;
		Move m;
		int scoreEval;
		if (depth == 0) {
			scoreEval = evaluate(whichSide);
			myBest.score = scoreEval;
			myBest.depth = 0;
			return myBest;
		}
		scoreEval = evaluate(whichSide);
		if (scoreEval == 100 || scoreEval == -100) {
			myBest.score = scoreEval;
			myBest.depth = depth;
			return myBest;
		}
		if (color == whichSide) {
			myBest.score = alpha;
		} else {
			myBest.score = beta;
		}
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (rounds < 20) {
					m = new Move(i,j);
					if (validMove(m, color)) {
						if(color == whichSide) {
							forceMove(m);
						} else {
							opponentMove(m);
						}
						reply = bestMove(1-color, alpha, beta, depth-1);
						undo(m, color);
						if ((color == whichSide) && (reply.score > myBest.score || (reply.score == myBest.score && reply.depth > myBest.depth))){
							myBest.m = m;
							myBest.score = reply.score;
							myBest.depth = reply.depth;
							alpha = reply.score;
						} else if ((color == 1 - whichSide) && (reply.score < myBest.score ||(reply.score == myBest.score && reply.depth < myBest.depth) )) {
							myBest.m = m;
							myBest.score = reply.score;
							myBest.depth = reply.depth;
							beta = reply.score;
						}
						if (alpha >= beta) {
							return myBest;
						}
					}
				} else {
					for (int x = 0; x < SIZE; x++) {
						for (int y = 0; y < SIZE; y++) {
							if (gameboard[x][y].get_color() == color) {
								m = new Move(i, j, x, y);
								if (validMove(m, color)) {
									if(color == whichSide) {
										forceMove(m);
									} else {
										opponentMove(m);
									}
									reply = bestMove(1-color, alpha, beta, depth-1);
									undo(m, color);
									if ((color == whichSide) && (reply.score > myBest.score || (reply.score == myBest.score && reply.depth > myBest.depth))) {
										myBest.m = m;
										myBest.score = reply.score;
										myBest.depth = reply.depth;
										alpha = reply.score;
									} else if ((color == 1 - whichSide) && (reply.score < myBest.score || (reply.score == myBest.score && reply.depth < myBest.depth) )) {
										myBest.m = m;
										myBest.score = reply.score;
										myBest.depth = reply.depth;
										beta = reply.score;
									}
									if (alpha >= beta) {
										return myBest;
									}
								}
							}
						}
					}
				}
			}
		}
		return myBest;
	}


	/*
	 * Restore the 'color' chip on the grid
	 */
	private void undo(Move m, int color) {
		removeChip(m.x1, m.y1, color);
		if (m.moveKind == Move.STEP) {
			addChip(m.x2, m.y2, color);
		}
	}

	/*
	 * Checks if a 'color' move m is a valid move and checks if it's ok to perform an Add or Step;
	 * if so, go ahead and perform the move.
	 * Return True if the move m can be done, False otherwise.
	 */
	private boolean enforceMove(Move m, int color) {
		if (validMove(m, color)) {
			if (m.moveKind == Move.ADD) {
				if (rounds >= 20) {
					return false;
				} 
				addChip(m.x1, m.y1, color);
			} else if (m.moveKind == Move.STEP) {
				if (rounds < 20){
					return false;
				}
				removeChip(m.x2, m.y2, color);
				addChip(m.x1, m.y1, color);
			} 
			return true;
		}
		return false;
	}

	/*
	 * Return true if a Move of 'color' follows the rule (a valid move)
	 * Return false otherwise
	 */
	private boolean validMove(Move m, int color) {
		if( m.x1 < 0 || m.x1 > 7 || m.x2 < 0 || m.x2 > 7 || m.y1 < 0 || m.y1 > 7 || m.y2 < 0|| m.y2 >7 ) {
			return false;			
		}
		/*
		 * new location should empty
		 */
		if(gameboard[m.x1][m.y1].get_color() != EMPTY)  {
			return false;			
		}
		/*
		 * goal area for white
		 */
		if(color == WHITE && (m.y1 == 0 || m.y1 == SIZE-1)) {
			return false;
		}
		/*
		 * goal area for black
		 */
		if(color == BLACK && (m.x1 == 0 || m.x1 == SIZE-1)){
			return false;
		}
		/*
		 * check if Move m  and neighbors contains 
		 * two or more chips in connected group  
		 */
		int connected = 0;
		int neighbor_x = 0;
		int neighbor_y = 0;
		for (int i = m.x1 - 1; i <= m.x1 + 1; i++ ) {
			for(int j= m.y1 - 1; j <= m.y1+1; j++) {
				if(i >= 0 && i <= 7 && j >= 0 && j <= 7 ) {
					if(gameboard[i][j].get_color() == color &&
							(m.moveKind != Move.STEP || m.x2!=i || m.y2 != j)) {
						connected++;
						neighbor_x = i;
						neighbor_y = j;
					}
				}
			}
		}
		if(connected > 1) {
			return false;
		}
		if(connected == 1) {
			for(int i1 = neighbor_x-1; i1 <= neighbor_x + 1; i1++) {
				for(int j1 = neighbor_y - 1; j1 <= neighbor_y + 1; j1++) {
					if(i1 >= 0 && i1 <= 7 && j1 >=0 && j1 <=7){
						if(gameboard[i1][j1].get_color() == color && 
								(neighbor_x != i1 || neighbor_y != j1) && 
								(m.moveKind != Move.STEP || m.x2 != i1 || m.y2 != j1) ) {
							return false;
						}
					}
				}

			}
		}
		return true;
	} 
	/*
	 * Helper method for depth first search
	 * Checks if the current node is visited in the previous paths
	 * Return True if it's visited, False otherwise
	 */
	public boolean isVisited(GameNode[] path, int depth, GameNode curr_loc) {
		for (int i = 1; i <= depth-1; i++) {
			if (path[i] == curr_loc) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Use depth first search to find network
	 * Will update the howmanyPath of the chip in the goal area
	 * Return True if a path exists and set the howmanyPath variable to 100 (winning indicator), False otherwise
	 */
	public boolean network_dfs (GameNode loc, int depth, int direction, GameNode[] path) {
		if (loc.get_x() == 7 || loc.get_y() == 7) {
			howmanyPath++;
		}
		if ((loc.get_x() == 7 || loc.get_y() == 7) && depth >= 6) {
			howmanyPath = 100;
			return true;
		}
		if (loc.get_x() != 7 && loc.get_y() != 7) {
			for (int i = 0; i < SIZE; i++) {
				if (loc.neighbors[i] != null && 
				loc.neighbors[i].get_x() != 0 && 
				loc.neighbors[i].get_y() != 0 && 
				isVisited(path, depth, loc.neighbors[i]) && 
				direction != i) {
					//System.out.println(depth);
					path[depth] = loc.neighbors[i];
					if (network_dfs(loc.neighbors[i], depth+1, i, path)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * Helper method for evaluate
	 * Return 100 if 'color' player wins, else will return the max number of paths
	 * between the two goal areas
	 */
	public int calcWin(int color) {
		int maxPath = 0;
		GameNode[] path = new GameNode[10], moves = new GameNode[10];

		//read-in the goal areas of 'color'
		if (color == WHITE) {
			for (int i = 0; i < 7; i++) {
				moves[i] = gameboard[0][i];
			}
		} else {
			for (int i = 0; i < 7; i++) {
				moves[i] = gameboard[i][0];
			}
		}
		//start find network with dfs from the goal areas
		for (int i = 1; i < 7; i++) {
			if (moves[i].get_color() != EMPTY) {
				path[0] = moves[i];
				howmanyPath = 0;
				if (network_dfs(moves[i], 1, -1, path)) { //if successfully finds a path, game ends
					maxPath = howmanyPath;
					break;
				}
				if (maxPath < howmanyPath) {
					maxPath = howmanyPath;
				}
			}
		}
		return maxPath;
	}

	/*
	 * Calculate the score using: 
	 * 1) the total number of all 'color''s neighbors
	 * 2) the number of paths between the 2 goal areas
	 * Return 100 means 'color' player wins, -100 means 'color' player loses
	 */
	public int evaluate(int color) {
		int currPath, otherPath, currNbr, otherNbr;
		/*
		 * currePath = 'this' player's number of paths
		 * otherPath = opponent's number of paths
		 * currNbr = 'this' player's total number of neighbors
		 * otherNbr = opponent's total number of neighbors
		 */

		currPath = calcWin(color);
		if (currPath == 100) {
			return currPath;
		}
		
		otherPath = calcWin(1 - color);
		if (otherPath == 100) {
			return -otherPath;
		}

		currNbr = howManyNeighbors(color);
		otherNbr = howManyNeighbors(1 - color);

		return currNbr / 2 + currPath * 4 - otherPath * 2 - otherNbr;
	}

	/* 
	 * Helper method for the evaluation method
	 * returns how many neighbor(s) all 'color' chips have
	 */
	public int howManyNeighbors(int color) {
		int count;
		//int k = 0;
		int total = 0;
		int colorLoc = 0;
		GameNode[] sameColorChip = new GameNode[10];

		//store all the 'color' chips into an array
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (gameboard[i][j].get_color() == color) {
					sameColorChip[colorLoc] = gameboard[i][j];
					colorLoc++;
				}
			}
		}

		for (int i = 0; i <= 9; i++) {
			if (sameColorChip[i] != null) {
				count = 0;
				for (int j = 0; j < SIZE; j++) {
					if (sameColorChip[i].neighbors[j] != null && sameColorChip[i].neighbors[j].get_color() != EMPTY) {
						count++;
					}
				}
				total = total + count;
			} else {
				break;
			}
		}

		
		/*while (sameColorChip[k] != null) {
			count = 0;
			for (int j = 0; j < SIZE; j++) {
				if (sameColorChip[k].neighbors[j] != null && sameColorChip[k].neighbors[j].get_color() != -1) {
					count++;
				}
			}
			total = total + count;
			k++;
		} */

		return total;
	}

	//All the neighbor update functions
	//neighbors are defined as a pair of same color
	/* 
	 * Update the upper and lower neighbor of GameNode at (x_cor, y_cor),
	 * It also updates upper and lower chips' neighbors to
	 * GameNode (x_cor, y_cor).  
	 */
	private void upperLowerUpdate(int x_cor, int y_cor, int color, int operation) {
		GameNode tempNode = new GameNode();

		//lower neighbor update
		int temp_y = y_cor - 1;
		while(0 <= x_cor && x_cor < SIZE && 0 <= temp_y && temp_y < SIZE) {
			if (gameboard[x_cor][temp_y].get_color() == color && x_cor != 0 && x_cor != 7 ) {
				if (operation == GameNode.ADD) {
					gameboard[x_cor][y_cor].neighbors[GameNode.LOWER] = gameboard[x_cor][temp_y];
					gameboard[x_cor][temp_y].neighbors[GameNode.UPPER] = gameboard[x_cor][y_cor];
				} else {
					gameboard[x_cor][temp_y].neighbors[GameNode.UPPER] = null;
				}
				tempNode = gameboard[x_cor][temp_y];
				break;
			}

			if (gameboard[x_cor][temp_y].get_color() == 1 - color) {
				tempNode = gameboard[x_cor][temp_y];
				break;
			}
			temp_y--;
		}

		//upper neighbor update
		temp_y = y_cor + 1;
		while(0 <= x_cor && x_cor < SIZE && 0 <= temp_y && temp_y < SIZE) {
			if (gameboard[x_cor][temp_y].get_color() == color && x_cor != 0 && x_cor != 7) {
				if (operation == GameNode.ADD) {
					gameboard[x_cor][y_cor].neighbors[GameNode.UPPER] = gameboard[x_cor][temp_y];
					gameboard[x_cor][temp_y].neighbors[GameNode.LOWER] = gameboard[x_cor][y_cor];
				} else {
					gameboard[x_cor][temp_y].neighbors[GameNode.LOWER] = null;
				}
				if (tempNode.get_color() == color) {
					if (operation == GameNode.DELETE) {
						tempNode.neighbors[GameNode.UPPER] = gameboard[x_cor][temp_y];
						gameboard[x_cor][temp_y].neighbors[GameNode.LOWER] = tempNode;
					}
				}
				break;
			}
			if (gameboard[x_cor][temp_y].get_color() == 1-color) {
				if (tempNode.get_color() == 1-color) {
					if (operation == GameNode.ADD) {
						tempNode.neighbors[GameNode.UPPER] = null;
						gameboard[x_cor][temp_y].neighbors[GameNode.LOWER] = null;
					} else {
						tempNode.neighbors[GameNode.UPPER] = gameboard[x_cor][temp_y];
						gameboard[x_cor][temp_y].neighbors[GameNode.LOWER] = tempNode;
					}
				}
				break;
			}
			temp_y++;
		}

	}

	/* 
	 * Update the left and right neighbor of GameNode at (x_cor, y_cor).
	 * It also updates the left and right chips' neighbors to
	 * GameNode at (x_cor, y_cor).
	 */
	private void leftRightUpdate(int x_cor, int y_cor, int color, int operation){
		GameNode tempNode = new GameNode();

		//update right neighbors
		int temp_x = x_cor + 1;
		while (0 <= temp_x && temp_x < SIZE && 0 <= y_cor && y_cor < SIZE){
			if (gameboard[temp_x][y_cor].get_color() == color && y_cor !=0 && y_cor != 7){
				if (operation == GameNode.ADD){
					gameboard[x_cor][y_cor].neighbors[GameNode.RIGHT] = gameboard[temp_x][y_cor];
					gameboard[temp_x][y_cor].neighbors[GameNode.LEFT] = gameboard[x_cor][y_cor];
				}else{
					gameboard[temp_x][y_cor].neighbors[GameNode.LEFT] = null;
				}
				tempNode = gameboard[temp_x][y_cor];
				break;
			}
			if (gameboard[temp_x][y_cor].get_color() == 1-color){
				tempNode = gameboard[temp_x][y_cor];
				break;
			}
			temp_x++;
		}

		//update left neighbors
		temp_x = x_cor - 1;
		while (0 <= temp_x && temp_x < SIZE && 0 <= y_cor && y_cor < SIZE){
			if (gameboard[temp_x][y_cor].get_color() == color && y_cor != 0 && y_cor != 7){
				if (operation == GameNode.ADD){
					gameboard[x_cor][y_cor].neighbors[GameNode.LEFT] = gameboard[temp_x][y_cor];
					gameboard[temp_x][y_cor].neighbors[GameNode.RIGHT] = gameboard[x_cor][y_cor];
				}else{
					gameboard[temp_x][y_cor].neighbors[GameNode.RIGHT] = null;
				}
				if (tempNode.get_color() == color){
					if (operation != GameNode.ADD){
						tempNode.neighbors[GameNode.LEFT] = gameboard[temp_x][y_cor];
						gameboard[temp_x][y_cor].neighbors[GameNode.RIGHT] = tempNode;
					}
				}
				break;
			}
			if (gameboard[temp_x][y_cor].get_color() == 1-color){
				if (tempNode.get_color() == 1-color){
					if (operation == GameNode.ADD){
						tempNode.neighbors[GameNode.LEFT] = null;
						gameboard[temp_x][y_cor].neighbors[GameNode.RIGHT] = null;
					}else{
						tempNode.neighbors[GameNode.LEFT] = gameboard[temp_x][y_cor];
						gameboard[temp_x][y_cor].neighbors[GameNode.RIGHT] = tempNode;
					}
				}
				break;
			}
			temp_x--;
		}
	}

	/*
	 * Update the neighbors of GameNode at (x_cor, y_cor) in
	 * LowerRight and UpperLeft direction.
	 * It also updates the affected upper left and lower right chips'
	 * neighbors to GameNode at (x_cor, y_cor).
	 */
	private void lRightuLeftUpdate(int x_cor, int y_cor, int color, int operation){
		GameNode tempNode = new GameNode();

		//update lower right neighbors
		int temp_x = x_cor + 1, temp_y = y_cor - 1;
		while (0 <= temp_x && temp_x < SIZE && 0<=temp_y && temp_y < SIZE){
			if (gameboard[temp_x][temp_y].get_color() == color){
				if (operation == GameNode.ADD){
					gameboard[x_cor][y_cor].neighbors[GameNode.LOWRIGHT] = gameboard[temp_x][temp_y];
					gameboard[temp_x][temp_y].neighbors[GameNode.UPLEFT] = gameboard[x_cor][y_cor];
				}else{
					gameboard[temp_x][temp_y].neighbors[GameNode.UPLEFT] = null;
				}
				tempNode = gameboard[temp_x][temp_y];
				break;
			}
			if (gameboard[temp_x][temp_y].get_color() == 1-color){
				tempNode = gameboard[temp_x][temp_y];
				break;
			}
			temp_x++;
			temp_y--;
		}

		//update upper left neighbors
		temp_x = x_cor - 1;
		temp_y = y_cor + 1;
		while (0 <= temp_x && temp_x < SIZE && 0<=temp_y && temp_y < SIZE){
			if (gameboard[temp_x][temp_y].get_color() == color){
				if (operation == GameNode.ADD){
					gameboard[x_cor][y_cor].neighbors[GameNode.UPLEFT] = gameboard[temp_x][temp_y];
					gameboard[temp_x][temp_y].neighbors[GameNode.LOWRIGHT] = gameboard[x_cor][y_cor];
				}else {
					gameboard[temp_x][temp_y].neighbors[GameNode.LOWRIGHT] = null;
				}
				if (tempNode.get_color() == color){
					if (operation != GameNode.ADD){
						tempNode.neighbors[GameNode.UPLEFT] = gameboard[temp_x][temp_y];
						gameboard[temp_x][temp_y].neighbors[GameNode.LOWRIGHT] = tempNode;
					}
				}
				break;
			}
			if (gameboard[temp_x][temp_y].get_color() == 1-color){
				if (tempNode.get_color() == 1-color){
					if (operation == GameNode.ADD ){
						tempNode.neighbors[GameNode.UPLEFT] = null;
						gameboard[temp_x][temp_y].neighbors[GameNode.LOWRIGHT] = null;
					}else{
						tempNode.neighbors[GameNode.UPLEFT] = gameboard[temp_x][temp_y];
						gameboard[temp_x][temp_y].neighbors[GameNode.LOWRIGHT] = tempNode;
					}
				}
				break;
			}
			temp_x--;
			temp_y++;
		}
	}

	/*
	 * Update the neighbors of GameNode at (x_cor, y_cor) in
	 * UpperRight and LowerLeft direction.
	 * It also updates the affected upper left and lower right chips'
	 * neighbors to GameNode at (x_cor, y_cor).
	 */
	private void uRightlLeftUpdate(int x_cor, int y_cor, int color, int operation){
		GameNode tempNode = new GameNode();

		//update upper right neighbors
		int temp_x = x_cor + 1, temp_y = y_cor + 1;
		while (0 <= temp_x && temp_x < SIZE && 0 <= temp_y && temp_y < SIZE){
			if (gameboard[temp_x][temp_y].get_color() == color){
				if (operation == GameNode.ADD){
					gameboard[x_cor][y_cor].neighbors[GameNode.UPRIGHT] = gameboard[temp_x][temp_y];
					gameboard[temp_x][temp_y].neighbors[GameNode.LOWLEFT] = gameboard[x_cor][y_cor];
				}else {
					gameboard[temp_x][temp_y].neighbors[GameNode.LOWLEFT] = null;
				}
				tempNode = gameboard[temp_x][temp_y];
				break;
			}
			if (gameboard[temp_x][temp_y].get_color() == 1-color){
				tempNode = gameboard[temp_x][temp_y];
				break;
			}
			temp_x++;
			temp_y++;
		}

		//update lower left neighbors
		temp_x = x_cor - 1;
		temp_y = y_cor - 1;
		while (0 <= temp_x && temp_x < SIZE && 0 <= temp_y && temp_y < SIZE){
			if (gameboard[temp_x][temp_y].get_color() == color){
				if (operation == GameNode.ADD){
					gameboard[x_cor][y_cor].neighbors[GameNode.LOWLEFT] = gameboard[temp_x][temp_y];
					gameboard[temp_x][temp_y].neighbors[GameNode.UPRIGHT] = gameboard[x_cor][y_cor];
				}else{
					gameboard[temp_x][temp_y].neighbors[GameNode.UPRIGHT] = null;
				}
				if (tempNode.get_color() == color){
					if (operation != GameNode.ADD){
						tempNode.neighbors[GameNode.LOWLEFT] = gameboard[temp_x][temp_y];
						gameboard[temp_x][temp_y].neighbors[GameNode.UPRIGHT] = tempNode;
					}
				}
				break;
			}
			if (gameboard[temp_x][temp_y].get_color() == 1-color){
				if (tempNode.get_color() == 1-color){
					if (operation == GameNode.ADD){
						tempNode.neighbors[GameNode.LOWLEFT] = null;
						gameboard[temp_x][temp_y].neighbors[GameNode.UPRIGHT] = null;
					}else{
						tempNode.neighbors[GameNode.LOWLEFT] = gameboard[temp_x][temp_y];
						gameboard[temp_x][temp_y].neighbors[GameNode.UPRIGHT] = tempNode;
					}
				}
				break;
			}
			temp_x--;
			temp_y--;
		}
	}

	//Add a 'color'chip to the location (x, y) on the gameboard
	private void addChip(int x, int y, int color) {
		rounds++;
		gameboard[x][y] = new GameNode(x, y, color);
		upperLowerUpdate(x, y, color, GameNode.ADD);
		leftRightUpdate(x, y, color, GameNode.ADD);
		lRightuLeftUpdate(x, y, color, GameNode.ADD);
		uRightlLeftUpdate(x, y, color, GameNode.ADD);
		
				
	}

	//Remove a 'color'chip to the location(x, y) from the gameboard
	private void removeChip(int x, int y, int color) {
		rounds--;
		upperLowerUpdate(x, y, color, GameNode.DELETE);
		leftRightUpdate(x, y, color, GameNode.DELETE);
		lRightuLeftUpdate(x, y, color, GameNode.DELETE);
		uRightlLeftUpdate(x, y, color, GameNode.DELETE);
		gameboard[x][y] = new GameNode(); //become empty after removal
		
		
	}

	// Returns a new move by "this" player.  Internally records the move (updates
	// the internal game board) as a move by "this" player.
	@Override
	public Move chooseMove() {
		Move m;
		if(rounds < 2) {
			if(whichSide == WHITE){
				m = new Move(0,3);
				forceMove(m);
				return m;
			} else {
				m = new Move(3, 0);
				forceMove(m);
				return m;
			}
		}
		BestStep best = bestMove(whichSide, -500, 500, sDepth);
		m = best.m;
		forceMove(m);
		return m;
	} 

	// If the Move m is legal, records the move as a move by the opponent
	// (updates the internal game board) and returns true.  If the move is
	// illegal, returns false without modifying the internal state of "this"
	// player.  This method allows your opponents to inform you of their moves.
	@Override
	public boolean opponentMove(Move m) {
		if(whichSide == BLACK){
			return (enforceMove(m, WHITE));
		}
		return (enforceMove(m, BLACK));
	}

	// If the Move m is legal, records the move as a move by "this" player
	// (updates the internal game board) and returns true.  If the move is
	// illegal, returns false without modifying the internal state of "this"
	// player.  This method is used to help set up "Network problems" for your
	// player to solve.
	@Override
	public boolean forceMove(Move m) {
		return (enforceMove(m, whichSide));
	}

}
