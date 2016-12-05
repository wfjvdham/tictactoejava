// // Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//  
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Field class
 * 
 * Handles everything that has to do with the field, such 
 * as storing the current state and performing calculations
 * on the field.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Field {
  private int[][] mBoard;
	private int[][] mMacroboard;
	private double[][] mMicroboardScore;
	private int [][] newMacroboard;
	private int myID;
	private int playerWhoHasTurnID;
	private final int COLS = 9, ROWS = 9;
	private String mLastError = "";
	
	public Field() {
		//mBoard = new int[COLS][ROWS];
		//mMacroboard = new int[COLS / 3][ROWS / 3];
		mMicroboardScore = new double[COLS / 3][ROWS / 3];
		clearMacroboardScores();
		//clearBoard();
	}
	
	public void createBoard() {
		mBoard = new int[COLS][ROWS];
		mMacroboard = new int[COLS / 3][ROWS / 3];
	}
	
	public int getMyID() {
		return myID;
	}
	
	/**
	 * Parse data about the game given by the engine
	 * @param key : type of data given
	 * @param value : value
	 */
	public void parseGameData(String key, String value) {
	    if (key.equals("round")) {
	        Integer.parseInt(value);
	    } else if (key.equals("move")) {
	        Integer.parseInt(value);
	    } else if (key.equals("field")) {
          parseFromString(value); /* Parse Field with data */
      } else if (key.equals("macroboard")) {
          parseMacroboardFromString(value); /* Parse macroboard with data */
      }
	}
	
	/**
	 * Initialise field from comma separated String
	 * @param String : 
	 */
	public void parseFromString(String s) {
	  //System.err.println("Move " + mMoveNr);
		s = s.replace(";", ",");
		String[] r = s.split(",");
		int counter = 0;
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				mBoard[x][y] = Integer.parseInt(r[counter]); 
				counter++;
			}
		}
	}
	
	/**
	 * Initialise macroboard from comma separated String
	 * @param String : 
	 */
	public void parseMacroboardFromString(String s) {
		String[] r = s.split(",");
		int counter = 0;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				mMacroboard[x][y] = Integer.parseInt(r[counter]);
				counter++;
			}
		}
	}
	
	public void clearBoard() {
		for (int x = 0; x < COLS; x++) {
			for (int y = 0; y < ROWS; y++) {
				mBoard[x][y] = 0;
			}
		}
		clearMacroboardScores();
	}
	
	public void clearMacroboardScores() {
		for (int x = 0; x < COLS/3; x++) {
			for (int y = 0; y < ROWS/3; y++) {
				mMicroboardScore[x][y] = 0;
			}
		}
	}
	
	public ScoreDepth getScore(int depth, double alpha, double beta) {
		int id = 1;
		if (playerWhoHasTurnID == 1) {
			id = 2;
		}
		//check if game is finished
		double score = 0;
		int status = calculateGameStatus();
		if (status!=0&&status!=-1) {
			if (status==myID) {
				score=Integer.MAX_VALUE;
				beta=Integer.MAX_VALUE;
			} else {
				alpha = Integer.MIN_VALUE;
				score=Integer.MIN_VALUE;
			}
			return new ScoreDepth(score,depth,alpha,beta);
		}	else {
			//check how much macroboard three in a rows can be made
			//macro boards
			double score1 = 0;
			score1 = score1 + 1000000 * getWinningMoves(id,mMacroboard,0,0,true);
			score1 = score1 + 10000 * getOptionsOnMacroboards(id);
			calculateMacroBoardScores(id);
			//microboards
			score1 = score1 + 100 * getBoardWinningMoves(id,mBoard);
			score1 = score1 + 1 * getOptionsOnMicroboards(id);
			
			//calculate minus scores for opponent
			double score2 = 0;
			clearMacroboardScores();
			score2 = score2 + 1000000 * getWinningMoves(playerWhoHasTurnID,mMacroboard,0,0,true);
			score2 = score2 + 10000 * getOptionsOnMacroboards(playerWhoHasTurnID);
			calculateMacroBoardScores(playerWhoHasTurnID);
			//microboards
			score2 = score2 + 100 * getBoardWinningMoves(playerWhoHasTurnID,mBoard);
			score2 = score2 + 1 * getOptionsOnMicroboards(playerWhoHasTurnID);
			
			//own succes is more important than opp failure
			if(id==myID) {
				score = score1 - (score2 * 0.95);
			} else {
				score = -(score1 * 0.95) + score2;
			}
		}
		if (depth==0) {
			return new ScoreDepth(score,depth,alpha,beta);
		} else if (depth<2) {
			if (playerWhoHasTurnID==myID) {
  			if (score <= alpha) {
  				alpha = score;
  				return goDeeper(depth,alpha,beta);
  			} else {
  				return new ScoreDepth(score,depth,alpha,beta);
  			}
  		} else {
  			if (score >= beta) {
  				beta = score;
  				return goDeeper(depth,alpha,beta);
  			} else {
  				return new ScoreDepth(score,depth,alpha,beta);
  			}
  		}
		} else {
			return goDeeper(depth,alpha,beta);
		}
	}
	
	private ScoreDepth goDeeper(int depth,double alpha,double beta) {
		//Recursively get score of next opp moves
		ArrayList<Move> availableMoves = getAvailableMoves();
		if (availableMoves.size()>0) {
  		//place move and calculate score of board
  		depth = depth-1;
  		for (int i = 0; i < availableMoves.size(); i++) {
  			Move move = availableMoves.get(i);
  			Field newField = playMove(move);
  			ScoreDepth sd = newField.getScore(depth,alpha,beta);
  			move.addScore(sd.getScore());
  			move.setDepth(sd.getDepth());
  			alpha = sd.getAlpha();
  			beta = sd.getBeta();
  			//print extra info for debugging
  			//System.out.println(
  			//	"Move X: " + move.getX() 
  			//  + " Y: " + move.getY() 
  			//  + " score: " + move.getScore()
  			//  + " depth: " + depth);
  		}
  		if (playerWhoHasTurnID==myID) {
    		Collections.sort(availableMoves, new Comparator<Move>(){
    		  public int compare(Move m1, Move m2)
    		  {
    		  	if(m1.getScore()>m2.getScore()) {
    					return -1;
    				} else if (m1.getScore()==m2.getScore()){
    					if (m1.getDepth()>m2.getDepth()) {
      					return -1;
      				} else if (m1.getDepth()==m2.getDepth()) {
      					return 0;
      				} else {
      					return 1;
      				}
    				} else {
    					return 1;
    				}
    		  }
    		});
  		} else {
    		Collections.sort(availableMoves, new Comparator<Move>(){
    		  public int compare(Move m1, Move m2)
    		  {
    		  	if(m1.getScore()<m2.getScore()) {
    					return -1;
    				} else if (m1.getScore()==m2.getScore()){
    					if (m1.getDepth()>m2.getDepth()) {
      					return -1;
      				} else if (m1.getDepth()==m2.getDepth()) {
      					return 0;
      				} else {
      					return 1;
      				}
    				} else {
    					return 1;
    				}
    		  }
    		});
  		}
  		return new ScoreDepth(availableMoves.get(0).getScore(),depth,alpha,beta);
		} else {
			//game is a draw
			return new ScoreDepth(0,depth,alpha,beta);
		}
	}
	
	private int getOptionsOnMacroboards(int id) {
		int score = 0, x=0, y=0;
		int[][] board = newMacroboard;
		if (board[x][y]==id&&board[x+1][y]==0&&board[x+2][y]==0||
				board[x][y]==0&&board[x+1][y]==id&&board[x+2][y]==0||
				board[x][y]==0&&board[x+1][y]==0&&board[x+2][y]==id) {
			score++;
		} 
		if (board[x][y+1]==id&&board[x+1][y+1]==0&&board[x+2][y+1]==0||
				board[x][y+1]==0&&board[x+1][y+1]==id&&board[x+2][y+1]==0||
				board[x][y+1]==0&&board[x+1][y+1]==0&&board[x+2][y+1]==id) {
			score++;
		} 
		if (board[x][y+2]==id&&board[x+1][y+2]==0&&board[x+2][y+2]==0||
				board[x][y+2]==0&&board[x+1][y+2]==id&&board[x+2][y+2]==0||
				board[x][y+2]==0&&board[x+1][y+2]==0&&board[x+2][y+2]==id) {
			score++;
		} 
		if (board[x][y]==id&&board[x][y+1]==0&&board[x][y+2]==0||
	 			board[x][y]==0&&board[x][y+1]==id&&board[x][y+2]==0||
	 			board[x][y]==0&&board[x][y+1]==0&&board[x][y+2]==id) {
			score++;
		} 
		if (board[x+1][y]==id&&board[x+1][y+1]==0&&board[x+1][y+2]==0||
	 			board[x+1][y]==0&&board[x+1][y+1]==id&&board[x+1][y+2]==0||
	 			board[x+1][y]==0&&board[x+1][y+1]==0&&board[x+1][y+2]==id) {
			score++;
		} 
		if (board[x+2][y]==id&&board[x+2][y+1]==0&&board[x+2][y+2]==0||
	 			board[x+2][y]==0&&board[x+2][y+1]==id&&board[x+2][y+2]==0||
	 			board[x+2][y]==0&&board[x+2][y+1]==0&&board[x+2][y+2]==id) {
			score++;
		} 
		if (board[x][y]==id&&board[x+1][y+1]==0&&board[x+2][y+2]==0||
	 			board[x][y]==0&&board[x+1][y+1]==id&&board[x+2][y+2]==0||
	 			board[x][y]==0&&board[x+1][y+1]==0&&board[x+2][y+2]==id) {
			score++;
		} 
		if (board[x+2][y]==id&&board[x+1][y+1]==0&&board[x][y+2]==0||
	 			board[x+2][y]==0&&board[x+1][y+1]==id&&board[x][y+2]==0||
	 			board[x+2][y]==0&&board[x+1][y+1]==0&&board[x][y+2]==id) {
			score++;
		} 
		return score;
	}
	
	private double getOptionsOnMicroboards(int id) {
		//TODO refine macroboard score based on internal probability of capturing the board
		double score = 0;
		int[][] board = mBoard;
		for (int x = 0; x < 9; x=x+3) {
			for (int y = 0; y < 9; y=y+3) {
				if(mMacroboard[x/3][y/3]!=1&&mMacroboard[x/3][y/3]!=2&&!macroboardIsFull(x/3, y/3)&&mMicroboardScore[x/3][y/3]!=0) {
  				//calculate the number of rows that have 1 of id and two empty
  				if (board[x][y]==id&&board[x+1][y]==0&&board[x+2][y]==0||
  						board[x][y]==0&&board[x+1][y]==id&&board[x+2][y]==0||
  						board[x][y]==0&&board[x+1][y]==0&&board[x+2][y]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
  				if (board[x][y+1]==id&&board[x+1][y+1]==0&&board[x+2][y+1]==0||
  						board[x][y+1]==0&&board[x+1][y+1]==id&&board[x+2][y+1]==0||
  						board[x][y+1]==0&&board[x+1][y+1]==0&&board[x+2][y+1]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
  				if (board[x][y+2]==id&&board[x+1][y+2]==0&&board[x+2][y+2]==0||
  						board[x][y+2]==0&&board[x+1][y+2]==id&&board[x+2][y+2]==0||
  						board[x][y+2]==0&&board[x+1][y+2]==0&&board[x+2][y+2]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
  				if (board[x][y]==id&&board[x][y+1]==0&&board[x][y+2]==0||
  			 			board[x][y]==0&&board[x][y+1]==id&&board[x][y+2]==0||
  			 			board[x][y]==0&&board[x][y+1]==0&&board[x][y+2]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
  				if (board[x+1][y]==id&&board[x+1][y+1]==0&&board[x+1][y+2]==0||
  			 			board[x+1][y]==0&&board[x+1][y+1]==id&&board[x+1][y+2]==0||
  			 			board[x+1][y]==0&&board[x+1][y+1]==0&&board[x+1][y+2]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
  				if (board[x+2][y]==id&&board[x+2][y+1]==0&&board[x+2][y+2]==0||
  			 			board[x+2][y]==0&&board[x+2][y+1]==id&&board[x+2][y+2]==0||
  			 			board[x+2][y]==0&&board[x+2][y+1]==0&&board[x+2][y+2]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
  				if (board[x][y]==id&&board[x+1][y+1]==0&&board[x+2][y+2]==0||
  			 			board[x][y]==0&&board[x+1][y+1]==id&&board[x+2][y+2]==0||
  			 			board[x][y]==0&&board[x+1][y+1]==0&&board[x+2][y+2]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
  				if (board[x+2][y]==id&&board[x+1][y+1]==0&&board[x][y+2]==0||
  			 			board[x+2][y]==0&&board[x+1][y+1]==id&&board[x][y+2]==0||
  			 			board[x+2][y]==0&&board[x+1][y+1]==0&&board[x][y+2]==id) {
  					score=score + mMicroboardScore[x/3][y/3];
  				} 
				}
			}
		}
		
		return score;
	}

	public boolean contains(Move a, ArrayList<Move> b) {
		for (int j = 0; j < b.size(); j++) {
			Move bw = b.get(j);
			if (a.getX()==bw.getX()&&
					a.getY()==bw.getY()) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<Move> getAvailableMoves() {
	  ArrayList<Move> moves = new ArrayList<Move>();
	  for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
      	if(mMacroboard[x][y] == -1) {
      		int i=1,j=1;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=0;j=1;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=2;j=1;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=1;j=0;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=1;j=2;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=0;j=0;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=2;j=0;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=0;j=2;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      		i=2;j=2;
      		if (mBoard[x+i][y+j] == 0) {
      			moves.add(new Move(x+i, y+j));
      		}
      	}
      }
	  }
		return moves;
	}
	
	public double getBoardWinningMoves(int id, int[][] board) {
		double score = 0;
	  for (int x = 0; x < COLS; x=x+3) {
		  for (int y = 0; y < ROWS; y=y+3) {
		  	score = score + (getWinningMoves(id,board,x,y,false)*mMicroboardScore[x/3][y/3]);
		  }
	  }
		return score;
	}
	
	public void calculateMacroBoardScores(int id) {		
		if (newMacroboard[0][0]==0&&newMacroboard[0][1]==0&&newMacroboard[0][2]==0) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.1;
			mMicroboardScore[0][1] = mMicroboardScore[0][1] + 0.1;
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.1;
		} 
		if (newMacroboard[1][0]==0&&newMacroboard[1][1]==0&&newMacroboard[1][2]==0) {
			mMicroboardScore[1][0] = mMicroboardScore[1][0] + 0.1;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.1;
			mMicroboardScore[1][2] = mMicroboardScore[1][2] + 0.1;
		} 
		if (newMacroboard[2][0]==0&&newMacroboard[2][1]==0&&newMacroboard[2][2]==0) {
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.1;
			mMicroboardScore[2][1] = mMicroboardScore[2][1] + 0.1;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.1;
		} 
		if (newMacroboard[0][0]==0&&newMacroboard[1][0]==0&&newMacroboard[2][0]==0) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.1;
			mMicroboardScore[1][0] = mMicroboardScore[1][0] + 0.1;
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.1;
		} 
		if (newMacroboard[0][1]==0&&newMacroboard[1][1]==0&&newMacroboard[2][1]==0) {
			mMicroboardScore[0][1] = mMicroboardScore[0][1] + 0.1;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.1;
			mMicroboardScore[2][1] = mMicroboardScore[2][1] + 0.1;
		} 
		if (newMacroboard[0][2]==0&&newMacroboard[1][2]==0&&newMacroboard[2][2]==0) {
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.1;
			mMicroboardScore[1][2] = mMicroboardScore[1][2] + 0.1;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.1;
		} 
		if (newMacroboard[0][0]==0&&newMacroboard[1][1]==0&&newMacroboard[2][2]==0) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.1;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.1;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.1;
		} 
		if (newMacroboard[2][0]==0&&newMacroboard[1][1]==0&&newMacroboard[0][2]==0) {
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.1;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.1;
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.1;
		} 
		if (newMacroboard[0][0]==id&&newMacroboard[0][1]==0&&newMacroboard[0][2]==0) {
			mMicroboardScore[0][1] = mMicroboardScore[0][1] + 0.2;
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.2;
		} else if (newMacroboard[0][0]==0&&newMacroboard[0][1]==id&&newMacroboard[0][2]==0) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.2;
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.2;
		} else if (newMacroboard[0][0]==0&&newMacroboard[0][1]==0&&newMacroboard[0][2]==id) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.2;
			mMicroboardScore[0][1] = mMicroboardScore[0][1] + 0.2;
		}
		if (newMacroboard[1][0]==id&&newMacroboard[1][1]==0&&newMacroboard[1][2]==0) {
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
			mMicroboardScore[1][2] = mMicroboardScore[1][2] + 0.2;
		} else if (newMacroboard[1][0]==0&&newMacroboard[1][1]==id&&newMacroboard[1][2]==0) {
			mMicroboardScore[1][0] = mMicroboardScore[1][0] + 0.2;
			mMicroboardScore[1][2] = mMicroboardScore[1][2] + 0.2;
		} else if (newMacroboard[1][0]==0&&newMacroboard[1][1]==0&&newMacroboard[1][2]==id) {
			mMicroboardScore[1][0] = mMicroboardScore[1][0] + 0.2;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
		}
		if (newMacroboard[2][0]==id&&newMacroboard[2][1]==0&&newMacroboard[2][2]==0) {
			mMicroboardScore[2][1] = mMicroboardScore[2][1] + 0.2;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.2;
		} else if (newMacroboard[2][0]==0&&newMacroboard[2][1]==id&&newMacroboard[2][2]==0) {
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.2;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.2;
		} else if (newMacroboard[2][0]==0&&newMacroboard[2][1]==0&&newMacroboard[2][2]==id) {
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.2;
			mMicroboardScore[2][1] = mMicroboardScore[2][1] + 0.2;
		}
		if (newMacroboard[0][0]==id&&newMacroboard[1][0]==0&&newMacroboard[2][0]==0) {
			mMicroboardScore[1][0] = mMicroboardScore[1][0] + 0.2;
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.2;
		} else if (newMacroboard[0][0]==0&&newMacroboard[1][0]==id&&newMacroboard[2][0]==0) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.2;
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.2;
		} else if (newMacroboard[0][0]==0&&newMacroboard[1][0]==0&&newMacroboard[2][0]==id) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.2;
			mMicroboardScore[1][0] = mMicroboardScore[1][0] + 0.2;
		}
		if (newMacroboard[0][1]==id&&newMacroboard[1][1]==0&&newMacroboard[2][1]==0) {
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
			mMicroboardScore[2][1] = mMicroboardScore[2][1] + 0.2;
		} else if (newMacroboard[0][1]==0&&newMacroboard[1][1]==id&&newMacroboard[2][1]==0) {
			mMicroboardScore[0][1] = mMicroboardScore[0][1] + 0.2;
			mMicroboardScore[2][1] = mMicroboardScore[2][1] + 0.2;
		} else if (newMacroboard[0][1]==0&&newMacroboard[1][1]==0&&newMacroboard[2][1]==id) {
			mMicroboardScore[0][1] = mMicroboardScore[0][1] + 0.2;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
		}
		if (newMacroboard[0][2]==id&&newMacroboard[1][2]==0&&newMacroboard[2][2]==0) {
			mMicroboardScore[1][2] = mMicroboardScore[1][2] + 0.2;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.2;
		} else if (newMacroboard[0][2]==0&&newMacroboard[1][2]==id&&newMacroboard[2][2]==0) {
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.2;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.2;
		} else if (newMacroboard[0][2]==0&&newMacroboard[1][2]==0&&newMacroboard[2][2]==id) {
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.2;
			mMicroboardScore[1][2] = mMicroboardScore[1][2] + 0.2;
		}
		if (newMacroboard[0][0]==id&&newMacroboard[1][1]==0&&newMacroboard[2][2]==0) {
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.2;
		} else if (newMacroboard[0][0]==0&&newMacroboard[1][1]==id&&newMacroboard[2][2]==0) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.2;
			mMicroboardScore[2][2] = mMicroboardScore[2][2] + 0.2;
		} else if (newMacroboard[0][0]==0&&newMacroboard[1][1]==0&&newMacroboard[2][2]==id) {
			mMicroboardScore[0][0] = mMicroboardScore[0][0] + 0.2;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
		}
		if (newMacroboard[2][0]==id&&newMacroboard[1][1]==0&&newMacroboard[0][2]==0) {
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.2;
		} else if (newMacroboard[2][0]==0&&newMacroboard[1][1]==id&&newMacroboard[0][2]==0) {
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.2;
			mMicroboardScore[0][2] = mMicroboardScore[0][2] + 0.2;
		} else if (newMacroboard[2][0]==0&&newMacroboard[1][1]==0&&newMacroboard[0][2]==id) {
			mMicroboardScore[2][0] = mMicroboardScore[2][0] + 0.2;
			mMicroboardScore[1][1] = mMicroboardScore[1][1] + 0.2;
		}
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (mMicroboardScore[x][y] > 1) {mMicroboardScore[x][y] = 1;}
			}
		}
	}
	
	public Move getMacroboardForMove(Move move) {
		return new Move(move.getX()/3,move.getY()/3);
	}
	
	public boolean moveIsInMacroBoard(Move move, Move macroMove) {
		if (move.getX()/3==macroMove.getX()&&
				move.getY()/3==macroMove.getY()) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getWinningMoves(int id, int[][] board, int x, int y, boolean clacMacroScore) {
		int n = 0;
		if (clacMacroScore) {
  		newMacroboard = new int[mMacroboard.length][];
  		for(int k = 0; k < mMacroboard.length; k++)
  			newMacroboard[k] = mMacroboard[k].clone();
  		for (int i=0;i<3;i++) {
  			for (int j=0;j<3;j++) {
  				if(newMacroboard[i][j]==-1) {newMacroboard[i][j]=0;}
  				if(newMacroboard[i][j]==0&&macroboardIsFull(i,j)) {newMacroboard[i][j]=-2;}
  			}
  		}
  		board = newMacroboard;
		}
		if ((board[x+1][y] == id && board[x+2][y] == id)||
	  		(board[x][y+1] == id && board[x][y+2] == id)||
	  		(board[x+1][y+1] == id && board[x+2][y+2] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3][y/3]==0) {
	  			mMicroboardScore[x/3][y/3] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y] == id && board[x+2][y] == id)||
	  		(board[x+1][y+1] == id && board[x+1][y+2] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3+1][y/3]==0) {
	  			mMicroboardScore[x/3+1][y/3] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y] == id && board[x+1][y] == id)||
	  		(board[x+1][y+1] == id && board[x][y+2] == id)||
	  		(board[x+2][y+1] == id && board[x+2][y+2] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3+2][y/3]==0) {
	  			mMicroboardScore[x/3+2][y/3] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y] == id && board[x][y+2] == id)||
	  		(board[x+1][y+1] == id && board[x+2][y+1] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3][y/3+1]==0) {
	  			mMicroboardScore[x/3][y/3+1] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y] == id && board[x+2][y+2] == id)||
	  		(board[x+2][y] == id && board[x][y+2] == id)||
	  		(board[x][y+1] == id && board[x+2][y+1] == id)||
	  		(board[x+1][y] == id && board[x+1][y+2] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3+1][y/3+1]==0) {
	  			mMicroboardScore[x/3+1][y/3+1] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y+1] == id && board[x+1][y+1] == id)||
	  		(board[x+2][y] == id && board[x+2][y+2] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3+2][y/3+1]==0) {
	  			mMicroboardScore[x/3+2][y/3+1] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y] == id && board[x][y+1] == id)||
	  		(board[x+1][y+2] == id && board[x+2][y+2] == id)||
	  		(board[x+1][y+1] == id && board[x+2][y] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3][y/3+2]==0) {
	  			mMicroboardScore[x/3][y/3+2] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y+2] == id && board[x+2][y+2] == id)||
	  		(board[x+1][y] == id && board[x+1][y+1] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3+1][y/3+2]==0) {
	  			mMicroboardScore[x/3+1][y/3+2] = 1;
	  		}
	  	}
	  }
	  if ((board[x][y+2] == id && board[x+1][y+2] == id)||
	  		(board[x+2][y] == id && board[x+2][y+1] == id)||
	  		(board[x+1][y+1] == id && board[x][y] == id)){
	  	if (board[x][y] == 0|| board[x][y] == -1) {
	  		n++;
	  		if (clacMacroScore&&newMacroboard[x/3+2][y/3+2]==0) {
	  			mMicroboardScore[x/3+2][y/3+2] = 1;
	  		}
	  	}
	  }
	  return n;
	}

	public Field playMove(Move move) {
		Field newField = new Field();
		int [][] newBoard = new int[mBoard.length][];
		for(int n = 0; n < mBoard.length; n++)
			newBoard[n] = mBoard[n].clone();
		newBoard[move.getX()][move.getY()] = playerWhoHasTurnID;
		newField.mBoard=newBoard;
		newField.myID=myID;
		if (playerWhoHasTurnID==1) {
			newField.playerWhoHasTurnID = 2;
		} else {
			newField.playerWhoHasTurnID = 1;
		}
		//update status current macroboard
		Move macromove = getMacroboardForMove(move);
		int[][] newMacroboard = calculateStatusMacroboard(macromove,newBoard);
		//determine next macroboard
		if(getStatusNextMacroboard(move.getX(),move.getY(),newMacroboard)==-1) {
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					if (newMacroboard[x][y]==-1) {
						newMacroboard[x][y]=0;
					}
				}
			}
			newMacroboard[move.getX()%3][move.getY()%3] = -1;
		} else if (getStatusNextMacroboard(move.getX(),move.getY(),newMacroboard)==0) {
			if (!macroboardIsFull(move.getX()%3,move.getY()%3,newBoard)) {
				for (int y = 0; y < 3; y++) {
					for (int x = 0; x < 3; x++) {
						if (newMacroboard[x][y]==-1) {
							newMacroboard[x][y]=0;
						}
					}
				}
				newMacroboard[move.getX()%3][move.getY()%3] = -1;
			} else {
				for (int y = 0; y < 3; y++) {
					for (int x = 0; x < 3; x++) {
						if (newMacroboard[x][y]==0&&!macroboardIsFull(x,y,newBoard)) {
							newMacroboard[x][y]=-1;
						}
					}
				}
			}
		} else if (getStatusNextMacroboard(move.getX(),move.getY(),newMacroboard)==2||
							 getStatusNextMacroboard(move.getX(),move.getY(),newMacroboard)==1) {
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					if (newMacroboard[x][y]==0&&!macroboardIsFull(x,y,newBoard)) {
						newMacroboard[x][y]=-1;
					}
				}
			}
		}
		newField.mMacroboard = newMacroboard;
		return newField;
	}
	
	public int calculateGameStatus() {
		return calculateStatusForSquare(mMacroboard,0,0);
	}
	
	public int calculateStatusForSquare(int[][] board,int x, int y) {
		int status = 0;
		if (board[x][y]==board[x+1][y]&&board[x+1][y]==board[x+2][y]&&board[x+2][y]!=0) {
			status = board[x][y];
		} else if (board[x][y+1]==board[x+1][y+1]&&board[x+1][y+1]==board[x+2][y+1]&&board[x+2][y+1]!=0) {
			status = board[x][y+1];
		} else if (board[x][y+2]==board[x+1][y+2]&&board[x+1][y+2]==board[x+2][y+2]&&board[x+2][y+2]!=0) {
			status = board[x][y+2];
		} else if (board[x][y]==board[x][y+1]&&board[x][y+1]==board[x][y+2]&&board[x][y+2]!=0) {
			status = board[x][y];
		} else if (board[x+1][y]==board[x+1][y+1]&&board[x+1][y+1]==board[x+1][y+2]&&board[x+1][y+2]!=0) {
			status = board[x+1][y];
		} else if (board[x+2][y]==board[x+2][y+1]&&board[x+2][y+1]==board[x+2][y+2]&&board[x+2][y+2]!=0) {
			status = board[x+2][y];
		} else if (board[x][y]==board[x+1][y+1]&&board[x+1][y+1]==board[x+2][y+2]&&board[x+2][y+2]!=0) {
			status = board[x][y];
		} else if (board[x+2][y]==board[x+1][y+1]&&board[x+1][y+1]==board[x][y+2]&&board[x][y+2]!=0) {
			status = board[x+2][y];
		}
		return status;
	}

	public int[][] calculateStatusMacroboard(Move move, int[][] board) {
		int x = move.getX()*3;
		int y = move.getY()*3;
		int [][] newMacroboard = new int[mMacroboard.length][];
		for(int n = 0; n < mMacroboard.length; n++)
			newMacroboard[n] = mMacroboard[n].clone();
		newMacroboard[x/3][y/3] = calculateStatusForSquare(board,x,y);	
		return newMacroboard;
	}
	
	public Boolean isInActiveMicroboard(int x, int y) {
	    return mMacroboard[(int) x/3][(int) y/3] == -1;
	}
	
	public int getStatusNextMacroboard(int x, int y, int[][] macroboard) {
		int xMacro = x%3;
		int yMacro = y%3;
		return macroboard[xMacro][yMacro];
	}
	
	public boolean nextMacroboardIsFull(int x, int y, int[][] board) {
		int xMacro = x%3;
		int yMacro = y%3;
		return macroboardIsFull(xMacro,yMacro,board);
	}
	
	public boolean macroboardIsFull(int x, int y) {
		return macroboardIsFull(x,y,null);
	}
	
	public boolean macroboardIsFull(int x, int y, int[][] board) {
		x = x*3;
		y = y*3;
		if(board==null) {
			board = mBoard;
		}
		boolean result = false;
		if (board[x][y]!=0&&board[x+1][y]!=0&&board[x+2][y]!=0&&
				board[x][y+1]!=0&&board[x+1][y+1]!=0&&board[x+2][y+1]!=0&&
				board[x][y+2]!=0&&board[x+1][y+2]!=0&&board[x+2][y+2]!=0) {
			result = true;
		}
		return result;
	}
	
	/**
	 * Returns reason why addMove returns false
	 * @param args : 
	 * @return : reason why addMove returns false
	 */
	public String getLastError() {
		return mLastError;
	}

	
	@Override
	/**
	 * Creates comma separated String with player ids for the microboards.
	 * @param args : 
	 * @return : String with player names for every cell, or 'empty' when cell is empty.
	 */
	public String toString() {
		String r = "";
		int counter = 0;
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				if (counter > 0) {
					r += ",";
				}
				r += mBoard[x][y];
				counter++;
			}
		}
		return r;
	}
	
	/**
	 * Checks whether the field is full
	 * @param args : 
	 * @return : Returns true when field is full, otherwise returns false.
	 */
	public boolean isFull() {
		for (int x = 0; x < COLS; x++)
		  for (int y = 0; y < ROWS; y++)
		    if (mBoard[x][y] == 0)
		      return false; // At least one cell is not filled
		// All cells are filled
		return true;
	}
	
	public int getNrColumns() {
		return COLS;
	}
	
	public int getNrRows() {
		return ROWS;
	}

	public boolean isEmpty() {
		for (int x = 0; x < COLS; x++) {
			  for (int y = 0; y < ROWS; y++) {
				  if (mBoard[x][y] > 0) {
					  return false;
				  }
			  }
		}
		return true;
	}
	
	/**
	 * Returns the player id on given column and row
	 * @param args : int column, int row
	 * @return : int
	 */
	public int getPlayerId(int column, int row) {
		return mBoard[column][row];
	}

	public void setMyId(int mBotId) {
		myID = mBotId;
	}
	
	public void setPlayerWhoHasTurnID(int mBotId) {
		playerWhoHasTurnID = mBotId;
	}

}