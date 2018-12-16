// Copyright 2014 theaigames.com (developers@theaigames.com)

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

package conquest.game;

public class PlayerInfo {
	private String id;
	private String name;
	
	private int armiesPerTurn;
	
	public PlayerInfo(String id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	/**
	 * @param n Sets the number of armies this player can place each turn
	 */
	public void setArmiesPerTurn(int n) {
		armiesPerTurn = n;
	}
	
	/**
	 * @return The String ID of this Player (typically PLR1 or PLR2)
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return Human-readable name of this Player
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return The number of armies this Player receives each turn
	 */
	public int getArmiesPerTurn() {
		return armiesPerTurn;
	}

}
