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
		ArrayList<Move> availableMoves = field.getAvailableMoves();
		//place move and calculate score of board
		int depth = 5;
		double alpha = Integer.MAX_VALUE;
		double beta = Integer.MIN_VALUE;
		for (int i = 0; i < availableMoves.size(); i++) {
			Move move = availableMoves.get(i);
			Field newField = field.playMove(move);
			ScoreDepth sd = newField.getScore(depth, alpha, beta);
			move.addScore(sd.getScore());
			move.setDepth(sd.getDepth());
			alpha = sd.getAlpha();
			beta = sd.getBeta();
//			System.out.println(
//					"Move X: " + move.getX() 
//					+ " Y: " + move.getY() 
//					+ " score: " + move.getScore()
//					+ " depth: " + depth);
		}		
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
		return availableMoves.get(0); /* get best score move */
	}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}
}
