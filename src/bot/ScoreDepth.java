package bot;

public class ScoreDepth {
	private int score;
	private int depth;
	private int alpha;
	
	public ScoreDepth(int score, int depth, int alpha) {
		this.score = score;
		this.depth = depth;
		this.alpha = alpha;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public int getAlpha() {
		return alpha;
	}
}
