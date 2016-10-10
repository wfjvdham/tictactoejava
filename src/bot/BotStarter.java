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
 * BotStarter class
 * 
 * Magic happens here. You should edit this file, or more specifically
 * the makeTurn() method to make your bot do more than random moves.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotStarter {
    /**
     * Makes a turn. Edit this method to make your bot smarter.
     * Currently does only random moves.
     *
     * @return The column where the turn was made.
     */
	public Move makeTurn(Field field) {	
		//ArrayList<Move> availableMoves = field.getAvailableMoves();
		//ArrayList<Move> boardWinningMoves = field.getBoardWinningMoves();
		//ArrayList<Move> oppBoardWinningMoves = field.getOppBoardWinningMoves();	
		//do not give option of putting anywhere on the map
		//field.filterOutOppHaveAllOptions(availableMoves);
		//do not give opponent chance to make 3 in a row in the next move
		//field.filterOutOppCanMake3NextTurn(availableMoves, oppBoardWinningMoves);
		//block 3 in a row
		//contains(availableMoves, oppBoardWinningMoves, 10);
		//add game winning moves
		//field.gameWinningMoves(availableMoves);
		//score macro boards
		//field.calculateMacroScore(availableMoves);
		//negative score useless macroboards
		//field.uselessMacroBoards(availableMoves);
		//make 3 in a row
		//contains(availableMoves, boardWinningMoves, 100);
		//add scoring to each move: nr of options 3 in a row can be made
		//TODO do not give other player possibility to block
		//TODO if macro is usefull prefer making 3 in a row
		//TODO if oponent macro is dangerous prefer blocking
		//TODO calculate opp macro score
		//TODO when a move blocks three in and the next move is in the same macro
			//alg thinks this is bad because opp can make 3 in a row, but this is blocked..
		//field.calculateScore(availableMoves);
		//Collections.sort(availableMoves, new Comparator<Move>(){
		//  public int compare(Move m1, Move m2)
		//  {
		//  	if(m1.getNrOf3Extra()>m2.getNrOf3Extra()) {
		//			return -1;
		//		} else {
		//			return 1;
		//		}
		//  }
		//});
		Status status = new Status(null);
		status.setField(field);
		status.minMax(3, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
		Move move = status.getBestMove();
		//return availableMoves.get(0); /* get best score move */
		return move;
	}

	public void contains(ArrayList<Move> a, ArrayList<Move> b, int points) {
		for (int i = 0; i < a.size(); i++) {
			for (int j = 0; j < b.size(); j++) {
				Move av = a.get(i);
				Move bw = b.get(j);
				if (av.getX()==bw.getX()&&
						av.getY()==bw.getY()) {
					if (av.getNrOf3Extra()>0) {
						av.addNrOf3Extra(points);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}
}
