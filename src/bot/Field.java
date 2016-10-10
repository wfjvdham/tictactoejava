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
	private int myID;
	private int playerWhoHasTurnID;

	private final int COLS = 9, ROWS = 9;
	private String mLastError = "";
	
	public Field() {
		mBoard = new int[COLS][ROWS];
		mMacroboard = new int[COLS / 3][ROWS / 3];
		clearBoard();
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
	}
	
	public int getScore() {
		int score = 0;
		int oppId = 1;
		if (myID==1) {
			oppId = 2;
		}
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (mMacroboard[x][y] == myID) {
					score++;
				}
				if (mMacroboard[x][y] == oppId) {
					score--;
				}
			}
		}
		return score;
	}

	public ArrayList<Move> getAvailableMoves() {
	  ArrayList<Move> moves = new ArrayList<Move>();
		
		for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (isInActiveMicroboard(x, y) && mBoard[x][y] == 0) {
                    moves.add(new Move(x, y));
                }
            }
        }

		return moves;
	}
	
	public ArrayList<Move> getBoardWinningMoves() {
		return getBoardWinningMoves(playerWhoHasTurnID,mBoard);
	}
	
	public ArrayList<Move> getOppBoardWinningMoves() {
		int id = 1;
		if (playerWhoHasTurnID == 1) {
			id = 2;
		}
		return getBoardWinningMoves(id,mBoard);
	}
	
	public ArrayList<Move> getBoardWinningMoves(int id, int[][] board) {
	  ArrayList<Move> moves = new ArrayList<Move>();
	  for (int x = 0; x < COLS; x=x+3) {
		  for (int y = 0; y < ROWS; y=y+3) {
		  	moves = getWinningMoves(moves,id,board,x,y);
		  }
	  }
		return moves;
	}
	
	public void gameWinningMoves(ArrayList<Move> moves) {
  	ArrayList<Move> macroMoves = new ArrayList<Move>();
    macroMoves = getWinningMoves(macroMoves,playerWhoHasTurnID,mMacroboard,0,0);
    for (int i = 0; i < moves.size(); i++) {
    	Move move = moves.get(i);
    	for (int j = 0; j < macroMoves.size(); j++) {
    		Move macroMove = macroMoves.get(j);
    		if(moveIsInMacroBoard(move,macroMove)) {
    			move.addNrOf3Extra(1000);
    		}
    	}
    }
	}
	
	public void calculateMacroScore(ArrayList<Move> moves) {
		ArrayList<Move> macroMoves = new ArrayList<Move>();
		macroMoves = getWinningMoves(macroMoves,playerWhoHasTurnID,mMacroboard,0,0);
		int scoreBefore = macroMoves.size();
		for (int x = 0; x < 3; x=x+1) {
		  for (int y = 0; y < 3; y=y+1) {
		  	if (mMacroboard[x][y]==-1) {
					int [][] newMacroboard = new int[mMacroboard.length][];
					for(int n = 0; n < mMacroboard.length; n++)
						newMacroboard[n] = mMacroboard[n].clone();
					newMacroboard[x][y] = playerWhoHasTurnID;
		  		macroMoves = new ArrayList<Move>();
		  		macroMoves = getWinningMoves(macroMoves,playerWhoHasTurnID,newMacroboard,0,0);
		  		int scoreAfter = macroMoves.size();
		  		for (int n = 0; n < moves.size(); n++) {
		  			if(moveIsInMacroBoard(moves.get(n),new Move(x,y))) {
		  				moves.get(n).addNrOf3Extra(20*(scoreAfter-scoreBefore));
		  			}
		  		}
		  	}
		  }
		}
	}
	
	public void uselessMacroBoards(ArrayList<Move> moves) {
		for (int i = 0; i < moves.size(); i++) {
			Move macroboard = getMacroboardForMove(moves.get(i));
			int xMacro = macroboard.getX();
			int yMacro = macroboard.getY();
			if (xMacro==0&&yMacro==0) {
				if (((mMacroboard[0][1]==2^mMacroboard[0][2]==2)||macroboardIsFull(0,1)||macroboardIsFull(0,2))&&
						((mMacroboard[1][1]==2^mMacroboard[2][2]==2)||macroboardIsFull(1,1)||macroboardIsFull(2,2))&&
						((mMacroboard[1][0]==2^mMacroboard[2][0]==2)||macroboardIsFull(1,0)||macroboardIsFull(2,0))	
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==1&&yMacro==0) {
				if (((mMacroboard[0][0]==2^mMacroboard[2][0]==2)||macroboardIsFull(0,0)||macroboardIsFull(2,0))&&
						((mMacroboard[1][1]==2^mMacroboard[2][1]==2)||macroboardIsFull(1,1)||macroboardIsFull(2,1))
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==2&&yMacro==0) {
				if (((mMacroboard[0][0]==2^mMacroboard[2][0]==2)||macroboardIsFull(0,0)||macroboardIsFull(2,0))&&
						((mMacroboard[1][1]==2^mMacroboard[2][1]==2)||macroboardIsFull(1,1)||macroboardIsFull(2,1))&&
						((mMacroboard[1][0]==2^mMacroboard[2][0]==2)||macroboardIsFull(1,0)||macroboardIsFull(2,0))	
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==0&&yMacro==1) {
				if (((mMacroboard[1][1]==2^mMacroboard[2][1]==2)||macroboardIsFull(1,1)||macroboardIsFull(2,1))&&
						((mMacroboard[0][0]==2^mMacroboard[0][2]==2)||macroboardIsFull(0,0)||macroboardIsFull(0,2))	
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==1&&yMacro==1) {
				if (((mMacroboard[0][0]==2^mMacroboard[2][2]==2)||macroboardIsFull(0,0)||macroboardIsFull(2,2))&&
						((mMacroboard[0][2]==2^mMacroboard[2][0]==2)||macroboardIsFull(0,2)||macroboardIsFull(2,0))&&
						((mMacroboard[1][0]==2^mMacroboard[1][2]==2)||macroboardIsFull(1,0)||macroboardIsFull(1,2))&&
						((mMacroboard[0][1]==2^mMacroboard[2][1]==2)||macroboardIsFull(0,1)||macroboardIsFull(2,1))
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==2&&yMacro==1) {
				if (((mMacroboard[1][1]==2^mMacroboard[0][1]==2)||macroboardIsFull(1,1)||macroboardIsFull(0,1))&&
						((mMacroboard[2][0]==2^mMacroboard[2][2]==2)||macroboardIsFull(2,0)||macroboardIsFull(2,2))	
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==0&&yMacro==2) {
				if (((mMacroboard[0][0]==2^mMacroboard[0][1]==2)||macroboardIsFull(0,0)||macroboardIsFull(0,1))&&
						((mMacroboard[1][1]==2^mMacroboard[2][0]==2)||macroboardIsFull(1,1)||macroboardIsFull(2,0))&&
						((mMacroboard[1][2]==2^mMacroboard[2][2]==2)||macroboardIsFull(1,2)||macroboardIsFull(2,2))	
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==1&&yMacro==2) {
				if (((mMacroboard[0][0]==2^mMacroboard[2][2]==2)||macroboardIsFull(0,0)||macroboardIsFull(2,2))&&
						((mMacroboard[0][2]==2^mMacroboard[2][0]==2)||macroboardIsFull(0,2)||macroboardIsFull(2,0))
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
			} else if (xMacro==2&&yMacro==2) {
				if (((mMacroboard[2][1]==2^mMacroboard[2][0]==2)||macroboardIsFull(2,1)||macroboardIsFull(2,0))&&
						((mMacroboard[1][2]==2^mMacroboard[0][2]==2)||macroboardIsFull(1,2)||macroboardIsFull(0,2))&&
						((mMacroboard[1][1]==2^mMacroboard[0][0]==2)||macroboardIsFull(1,1)||macroboardIsFull(2,2))	
						) {
					moves.get(i).addNrOf3Extra(-500);
				}
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
	
	public ArrayList<Move> getWinningMoves(ArrayList<Move> moves, int id, int[][] board, int x, int y) {
		if ((board[x+1][y] == id && board[x+2][y] == id)||
	  		(board[x][y+1] == id && board[x][y+2] == id)||
	  		(board[x+1][y+1] == id && board[x+2][y+2] == id)){
	  	if (board[x][y] == 0 || board[x][y] == -1) {moves.add(new Move(x,y));}
	  }
	  if ((board[x][y] == id && board[x+2][y] == id)||
	  		(board[x+1][y+1] == id && board[x+1][y+2] == id)){
	  	if (board[x+1][y] == 0 || board[x+1][y] == -1) {moves.add(new Move(x+1,y));}
	  }
	  if ((board[x][y] == id && board[x+1][y] == id)||
	  		(board[x+1][y+1] == id && board[x][y+2] == id)||
	  		(board[x+2][y+1] == id && board[x+2][y+2] == id)){
	  	if (board[x+2][y] == 0 || board[x+2][y] == -1) {moves.add(new Move(x+2,y));}
	  }
	  if ((board[x][y] == id && board[x][y+2] == id)||
	  		(board[x+1][y+1] == id && board[x+2][y+1] == id)){
	  	if (board[x][y+1] == 0 || board[x][y+1] == -1) {moves.add(new Move(x,y+1));}
	  }
	  if ((board[x][y] == id && board[x+2][y+2] == id)||
	  		(board[x+2][y] == id && board[x][y+2] == id)||
	  		(board[x][y+1] == id && board[x+2][y+1] == id)||
	  		(board[x+1][y] == id && board[x+1][y+2] == id)){
	  	if (board[x+1][y+1] == 0 || board[x+1][y+1] == -1) {moves.add(new Move(x+1,y+1));}
	  }
	  if ((board[x][y+1] == id && board[x+1][y+1] == id)||
	  		(board[x+2][y] == id && board[x+2][y+2] == id)){
	  	if (board[x+2][y+1] == 0 || board[x+2][y+1] == -1) {moves.add(new Move(x+2,y+1));}
	  }
	  if ((board[x][y] == id && board[x][y+1] == id)||
	  		(board[x+1][y+2] == id && board[x+2][y+2] == id)||
	  		(board[x+1][y+1] == id && board[x+2][y] == id)){
	  	if (board[x][y+2] == 0 || board[x][y+2] == -1) {moves.add(new Move(x,y+2));}
	  }
	  if ((board[x][y+2] == id && board[x+2][y+2] == id)||
	  		(board[x+1][y] == id && board[x+1][y+1] == id)){
	  	if (board[x+1][y+2] == 0 || board[x+1][y+2] == -1) {moves.add(new Move(x+1,y+2));}
	  }
	  if ((board[x][y+2] == id && board[x+1][y+2] == id)||
	  		(board[x+2][y] == id && board[x+2][y+1] == id)||
	  		(board[x+1][y+1] == id && board[x][y] == id)){
	  	if (board[x+2][y+2] == 0 || board[x+2][y+2] == -1) {moves.add(new Move(x+2,y+2));}
	  }
	  return moves;
	}
	
	public void calculateScore(ArrayList<Move> moves) {
		int bwmBefore = getBoardWinningMoves(playerWhoHasTurnID,mBoard).size();
		for (int i = 0; i < moves.size(); i++) {
			Move move = moves.get(i);
			int [][] newBoard = new int[mBoard.length][];
			for(int n = 0; n < mBoard.length; n++)
				newBoard[n] = mBoard[n].clone();
			newBoard[move.getX()][move.getY()] = playerWhoHasTurnID;
			int bwmAfter = getBoardWinningMoves(playerWhoHasTurnID,newBoard).size();
			move.addNrOf3Extra(bwmAfter-bwmBefore);
		}
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
		} else  if (board[x][y]==board[x+1][y+1]&&board[x+1][y+1]==board[x+2][y+2]&&board[x+2][y+2]!=0) {
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
	
	//public void filterOutOppHaveAllOptions(ArrayList<Move> moves) {
	//	for (int i = 0; i < moves.size(); i++) {
	//		Move move = moves.get(i);
	//		int x = move.getX();
	//		int y = move.getY();
	//		int status = getStatusNextMacroboard(x,y);
	//		if ((status == 0 || status == -1) && !nextMacroboardIsFull(x,y)) {
	//			move.addNrOf3Extra(100);
	//		}
	//	}
	//}
	
	public void filterOutOppCanMake3NextTurn(ArrayList<Move> moves, ArrayList<Move> oppBoardWinningMoves) {
		for (int i = 0; i < moves.size(); i++) {
			Move move = moves.get(i);
			int xMacro = move.getX()%3;
			int yMacro = move.getY()%3;
			boolean found = false;
			for (int j = 0; j < oppBoardWinningMoves.size(); j++) {
				Move oppBoardWinningMove = oppBoardWinningMoves.get(j);
				int xMacroOpp =oppBoardWinningMove.getX()/3;
				int yMacroOpp =oppBoardWinningMove.getY()/3;
				if ((xMacroOpp==xMacro)&&(yMacroOpp==yMacro)) {
					found = true;
					break;
				}
			}
			if (!found) {
				move.addNrOf3Extra(50);
			}
		}
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