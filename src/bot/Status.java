package bot;

import java.util.ArrayList;

public class Status {
	
	private Field mField;
	private int mRecursiveScore;
	private int mScore;
	private Move bestOption = null;
	private Move moveThatGotUsHere = null;
	
	public Status(Move move) {
		moveThatGotUsHere = move;
	}
	
	public Move getMoveThatGotUsHere() {
		return moveThatGotUsHere;
	}

	public int minMax(int depth, boolean needMax, int alpha, int beta) {
		if (depth == 0 || isTermalNode()) {
			mRecursiveScore = mScore;
      return mScore;
		}
		ArrayList<Status> statuses = getChilderen();
		for(int i = 0;i < statuses.size();i++) {
			Status current = statuses.get(i);
			int score = current.minMax(depth - 1, !needMax, alpha, beta);
			if (!needMax) {
				if (beta > score) {
					beta = score;
					bestOption = current.getMoveThatGotUsHere();
          if (alpha >= beta) {
          	break;
          }
        }
      } else {
        if (alpha < score) {
        	alpha = score;
        	bestOption = current.getMoveThatGotUsHere();
          if (alpha >= beta) {
          	break;
          }
        }
      }
		}
		
		mRecursiveScore = needMax ? alpha : beta;
    return mRecursiveScore;
	}
	
	public void setField(Field field) {
		mField = field;
	}
	
	public boolean isTermalNode() {
		int status = mField.calculateGameStatus();
		if (status==0||status==-1) {
			return false;
		} else {
			return true;
		}		
	}
	
	public ArrayList<Status> getChilderen() {
		ArrayList<Move> availableMoves = mField.getAvailableMoves();
		ArrayList<Status> statuses = new ArrayList<Status>();
		for (int i = 0; i < availableMoves.size();i++) {
			Move move = availableMoves.get(i);
			Status status = new Status(move);
			status.setField(mField.playMove(move));
			status.calculateScore();
			statuses.add(status);
		}
		return statuses;
	}

	private void calculateScore() {
		mScore = mField.getScore();
	}

	public Move getBestMove() {
		return bestOption;
	}
}
