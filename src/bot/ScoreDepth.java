package bot;

public class ScoreDepth {
	private double score;
	private int depth;
	private double alpha;
	private double beta;
	
	public ScoreDepth(double score2, int depth, double alpha2, double beta) {
		this.score = score2;
		this.depth = depth;
		this.alpha = alpha2;
		this.beta = beta;
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
	
	public double getBeta() {
		return beta;
	}
}
