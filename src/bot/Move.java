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

/**
 * Move class
 * 
 * Stores a move.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Move {
	int mX, mY, score;
	private int depth;
	
	public Move() {
	}
	
	public Move(int x, int y) {
		mX = x;
		mY = y;
		score = 0;
		depth = 0;
	}
	
	public int getX() { return mX; }
	public int getY() { return mY; }
	public int getScore() { return score; }
	public void addScore(int i) {score=score+i;}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public int getDepth() {
		return depth;
	}
	
}
