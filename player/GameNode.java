package player;

public class GameNode {
	private int x_cor;
	private int y_cor;
	protected GameNode[] neighbors;
	
	
	/* 
	 * white: 1
	 * black: 0
	 * empty: -1	
	 */
	private int color;
	
	//positions of the neighbors in the GameNode Array
	final static int UPLEFT = 0;
	final static int UPPER = 1;
	final static int UPRIGHT= 2;
	final static int RIGHT = 3;
	final static int LOWRIGHT= 4;
	final static int LOWER= 5;
	final static int LOWLEFT = 6;
	final static int LEFT = 7;
	
	//Determinant to ADD or DELETE a GameNode
	final static int ADD = 1;
	final static int DELETE = -1;
	
	//creates an empty gameboard space
	public GameNode() {
		color = MachinePlayer.EMPTY;
		neighbors = new GameNode[8];
	}
	
	public GameNode(int x, int y, int color) {
		this.x_cor = x;
		this.y_cor = y;
		this.color = color;
		neighbors = new GameNode[8];
	}
	
	public int get_x() {
		return x_cor;
	}
	
	public int get_y() {
		return y_cor;
	}
	
	public int get_color() {
		return color;
	}


}
