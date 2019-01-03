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
import conquest.game.world.Region;


public class RegionData {
	
	private Region region;
	private int id;
	private ArrayList<RegionData> neighbors;
	private ContinentData continent;
	private int armies;
	private int owner;
	
	public RegionData(Region region, int id, ContinentData superRegion)
	{
		this.region = region;
		this.id = id;
		this.continent = superRegion;
		this.neighbors = new ArrayList<RegionData>();
		this.owner = 0;
		this.armies = 0;
		if (superRegion != null) {
			superRegion.addRegion(this);
		}
	}
	
	public RegionData(Region region, int id, ContinentData superRegion, int owner, int armies)
	{
		this.region = region;
		this.id = id;
		this.continent = superRegion;
		this.neighbors = new ArrayList<RegionData>();
		this.owner = owner;
		this.armies = armies;
		
		superRegion.addRegion(this);
	}
	
	public void addNeighbor(RegionData neighbor)
	{
		if(!neighbors.contains(neighbor))
		{
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}
	
	/**
	 * @param region a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(RegionData region)
	{
		return neighbors.contains(region);
	}

	/**
	 * @param player
	 * @return True if this region is owned by the given player, false otherwise
	 */
	public boolean ownedByPlayer(int player)
	{
		return owner == player;
	}
	
	/**
	 * @param armies Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies) {
		this.armies = armies;
	}
	
	/**
	 * @param playerName Sets the player that this Region belongs to
	 */
	public void setOwner(int owner) {
		this.owner = owner;
	}
	
	/**
	 * @return The id of this Region
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public ArrayList<RegionData> getNeighbors() {
		return neighbors;
	}

	/**
	 * @return The continent this Region is part of
	 */
	public ContinentData getContinentData() {
		return continent;
	}
	
	/**
	 * @return The number of armies on this region
	 */
	public int getArmies() {
		return armies;
	}
	
	/**
	 * @return The player that owns this region
	 */
	public int getOwner() {
		return owner;
	}
	
	public boolean isNeutral() {
	    return owner == 0;
	}

	public Region getRegion() {
		return region;
	}

	public Continent getContinent() {
		return region.continent;
	}
	
    public boolean isVisible(int player) {
        if (ownedByPlayer(player))
            return true;
        
        for (RegionData s : getNeighbors())
            if (s.ownedByPlayer(player))
                return true;
        
        return false;
    }
	
	@Override
	public String toString() {
		return region.name() + "[" + owner + "|" + armies + "]";
	}
}
