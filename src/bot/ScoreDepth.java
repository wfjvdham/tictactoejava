package bot;

public class ScoreDepth {
	private double score;
	private int depth;
	private double alpha;
	
	public ScoreDepth(double score2, int depth, double alpha2) {
		this.score = score2;
		this.depth = depth;
		this.alpha = alpha2;
	}
	
	public double getScore() {
		return score;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public double getAlpha() {
		return alpha;
	}
}
