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

package conquest.bot;

import java.util.ArrayList;

import conquest.game.GameMap;
import conquest.game.Phase;
import conquest.game.RegionData;
import conquest.game.ConquestGame;
import conquest.game.ContinentData;
import conquest.game.GameConfig;
import conquest.game.world.Continent;
import conquest.game.world.Region;

public class BotState {
	// This map is known from the start, contains all the regions and how they are connected,
	// doesn't change after initialization
	private final GameMap fullMap = new GameMap(); 
	
	// This map represents everything the player can see, updated at the end of each round.
	private GameMap visibleMap; 
	
	//2 randomly chosen regions from each continent are given, which the bot can choose to start with
	private ArrayList<Region> pickableStartingRegions;

	private int startingArmies; //number of armies the player can place on map
	
	private int roundNumber;
	private int playerNumber;
	private Phase phase;
	
	public BotState()
	{
		pickableStartingRegions = new ArrayList<Region>();
		roundNumber = 0;
		phase = Phase.STARTING_REGIONS;
	}
	
	public void updateSettings(String key, String value)
	{
		if (key.equals("your_player_number"))
		    playerNumber = Integer.parseInt(value);
		else if (key.equals("starting_armies")) 
		    startingArmies = Integer.parseInt(value);
	}
	
	public void nextRound() {
	    roundNumber++;
	}
	
	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	
	//initial map is given to the bot with all the information except for player and armies info
	public void setupMap(String[] mapInput)
	{
		int i, regionId, continentId, reward;
		
		if(mapInput[1].equals("continents"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					continentId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new ContinentData(Continent.forId(continentId), continentId, reward));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Continents");
				}
			}
		}
		else if(mapInput[1].equals("regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					continentId = Integer.parseInt(mapInput[i]);
					ContinentData continent = fullMap.getContinent(continentId);
					fullMap.add(new RegionData(Region.forId(regionId), regionId, continent));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		}
		else if(mapInput[1].equals("neighbors"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					RegionData region = fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for(int j=0; j<neighborIds.length; j++)
					{
						RegionData neighbor = fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				}
				catch(Exception e) {
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		}
	}
	
	//regions from wich a player is able to pick his preferred starting regions
	public void setPickableStartingRegions(String[] mapInput)
	{
	    pickableStartingRegions = new ArrayList<Region>();
	    
		for(int i=2; i<mapInput.length; i++)
		{
			int regionId;
			try {
				regionId = Integer.parseInt(mapInput[i]);
				Region pickableRegion = Region.forId(regionId);
				pickableStartingRegions.add(pickableRegion);
			}
			catch(Exception e) {
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
	}
	
	//visible regions are given to the bot with player and armies info
	public void updateMap(String[] mapInput)
	{
		visibleMap = fullMap.clone();
		for(int i=1; i<mapInput.length; i++)
		{
			try {
				RegionData region = visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				int owner = Integer.parseInt(mapInput[i+1]);
				int armies = Integer.parseInt(mapInput[i+2]);
				
				region.setOwner(owner);
				region.setArmies(armies);
				i += 2;
			}
			catch(Exception e) {
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
	}
	
	public int getStartingArmies(){
		return startingArmies;
	}
	
	public int getRoundNumber(){
		return roundNumber;
	}
	
	public int getMyPlayerNumber() {
	    return playerNumber;
	}
	
	/**
	 * Map that is updated via observations.
	 * @return
	 */
	public GameMap getMap(){
		return visibleMap != null ? visibleMap : fullMap;
	}
	
	public GameMap getFullMap(){
		return fullMap;
	}
	
	public ArrayList<Region> getPickableStartingRegions(){
		return pickableStartingRegions;
	}

	public ConquestGame toConquestGame() {
	    return new ConquestGame(
	        new GameConfig(), getMap(), null, roundNumber, playerNumber, phase,
	        pickableStartingRegions);
	}
	
}
