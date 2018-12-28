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
import java.util.ArrayList;

import conquest.game.world.Continent;

public class ContinentData {
	
	private Continent continent;
	private int id;
	private int armiesReward;
	private ArrayList<RegionData> subRegions;
	
	public ContinentData(Continent continent, int id, int armiesReward)
	{
		this.continent = continent;
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new ArrayList<RegionData>();
	}
	
	public void addSubRegion(RegionData subRegion)
	{
		if(!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}
	
	/**
	 * @return The player that fully owns this continent, or 0 if none
	 */
	public int owner()
	{
		int player = subRegions.get(0).getOwner();
		for(RegionData region : subRegions)
		{
			if (player != region.getOwner())
				return 0;
		}
		return player;
	}
	
	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}
	
	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public ArrayList<RegionData> getSubRegions() {
		return subRegions;
	}

	public Continent getContinent() {
		return continent;
	}
	
}
