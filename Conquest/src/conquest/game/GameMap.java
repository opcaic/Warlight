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

public class GameMap implements Cloneable {
	
	public ArrayList<RegionData> regions;  // maps (id - 1) -> RegionData
	public ArrayList<ContinentData> continents;  // maps (id - 1) -> ContinentData
	
	public GameMap()
	{
		this.regions = new ArrayList<RegionData>();
		this.continents = new ArrayList<ContinentData>();
	}
	
	/**
	 * add a Region to the map
	 * @param region : Region to be added
	 */
	public void add(RegionData region)
	{
		if (region.getId() != regions.size() + 1)
			throw new Error("regions out of order");
		regions.add(region);
	}
	
	/**
	 * add a Continent to the map
	 * @param continent : Continent to be added
	 */
	public void add(ContinentData continent)
	{
		if (continent.getId() != continents.size() + 1)
			throw new Error("continents out of order");
		continents.add(continent);
	}
	
	/**
	 * @return : a new Map object exactly the same as this one
	 */
	@Override
	public GameMap clone() {
		GameMap newMap = new GameMap();
		for(ContinentData sr : continents) //copy continents
		{
			ContinentData newContinent = new ContinentData(Continent.forId(sr.getId()), sr.getId(), sr.getArmiesReward());
			newMap.add(newContinent);
		}
		for(RegionData r : regions) //copy regions
		{
			RegionData newRegion = new RegionData(Region.forId(r.getId()), r.getId(),
			        newMap.getContinent(r.getContinentData().getId()), r.getOwner(), r.getArmies());
			newMap.add(newRegion);
		}
		for(RegionData r : regions) //add neighbors to copied regions
		{
			RegionData newRegion = newMap.getRegion(r.getId());
			for(RegionData neighbor : r.getNeighbors())
				newRegion.addNeighbor(newMap.getRegion(neighbor.getId()));
		}
		return newMap;
	}
	
	/**
	 * @return : the list of all Regions in this map
	 */
	public ArrayList<RegionData> getRegions() {
		return regions;
	}
	
	/**
	 * @return : the list of all Continents in this map
	 */
	public ArrayList<ContinentData> getContinents() {
		return continents;
	}
	
	/**
	 * @param id : a Region id number
	 * @return : the matching Region object
	 */
	public RegionData getRegion(int id)
	{
		if (1 <= id && id <= regions.size())
		    return regions.get(id - 1);
		
		System.err.println("Could not find region with id " + id);
		return null;
	}
	
	public RegionData getRegionData(Region r) {
	    return getRegion(r.id);
	}
	
	/**
	 * @param id : a Continent id number
	 * @return : the matching Continent object
	 */
	public ContinentData getContinent(int id)
	{
		if (1 <= id && id <= continents.size())
			return continents.get(id - 1);

		System.err.println("Could not find continent with id " + id);
		return null;
	}
	
	public String getMapString()
	{
		String mapString = "";
		for(RegionData region : regions)
		{
			mapString = mapString.concat(region.getId() + ";" + region.getOwner() + ";" + region.getArmies() + " ");
		}
		return mapString;
	}
	
	public int numberRegionsOwned(int player) {
		int n = 0;
		
		for (RegionData r: regions)
			if (r.getOwner() == player)
				n += 1;
		
		return n;
	}
	
	//return all regions owned by given player
	public ArrayList<RegionData> ownedRegionsByPlayer(int player)
	{
		ArrayList<RegionData> ownedRegions = new ArrayList<RegionData>();
		
		for(RegionData region : this.getRegions())
			if(region.getOwner() == player)
				ownedRegions.add(region);

		return ownedRegions;
	}
	
	//fog of war
	//return all regions visible to given player
	public ArrayList<RegionData> visibleRegionsForPlayer(int player)
	{
		ArrayList<RegionData> visibleRegions = new ArrayList<RegionData>();
		ArrayList<RegionData> ownedRegions = ownedRegionsByPlayer(player);
		
		visibleRegions.addAll(ownedRegions);
		
		for(RegionData region : ownedRegions)	
			for(RegionData neighbor : region.getNeighbors())
				if(!visibleRegions.contains(neighbor))
					visibleRegions.add(neighbor);

		return visibleRegions;
	}
	
}
