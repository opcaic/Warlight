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

import java.util.*;

import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;
import conquest.view.GUI;


public class BotStarter implements Bot 
{
	Random rand = new Random();
	/**
	 * A method used at the start of the game to decide which players start with which Regions.
	 * This example randomly picks a region from the pickable starting Regions given by the engine.
	 */
	@Override
	public Region getStartingRegion(BotState state, Long timeOut)
	{
		ArrayList<Region> pickable = state.getPickableStartingRegions();
		return pickable.get(rand.nextInt(pickable.size()));
	}
	
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		int me = state.getMyPlayerNumber();
		int armies = 2;
		int armiesLeft = state.getStartingArmies();
		ArrayList<RegionData> visibleRegions = state.getMap().getRegions();
		
		while(armiesLeft > 0)
		{
			int r = rand.nextInt(visibleRegions.size());
			RegionData rd = visibleRegions.get(r);
			
			if(rd.ownedByPlayer(me))
			{
				placeArmiesMoves.add(new PlaceArmiesMove(rd.getRegion(), Math.min(armiesLeft, armies)));
				armiesLeft -= armies;
			}
		}
		
		return placeArmiesMoves;
	}

	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		int me = state.getMyPlayerNumber();
		int armies = 5;
		
		for(RegionData fromRegion : state.getMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(me)) //do an attack
			{
				ArrayList<RegionData> possibleToRegions = new ArrayList<RegionData>();
				possibleToRegions.addAll(fromRegion.getNeighbors());
				
				while(!possibleToRegions.isEmpty())
				{
					double rand = Math.random();
					int r = (int) (rand*possibleToRegions.size());
					RegionData toRegion = possibleToRegions.get(r);
					
					if(!toRegion.ownedByPlayer(me) && fromRegion.getArmies() > 6) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(fromRegion.getRegion(), toRegion.getRegion(), armies));
						break;
					}
					else if(toRegion.ownedByPlayer(me) && fromRegion.getArmies() > 1) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(fromRegion.getRegion(), toRegion.getRegion(), armies));
						break;
					}
					else
						possibleToRegions.remove(toRegion);
				}
			}
		}
		
		return attackTransferMoves;
	}

	@Override
	public void setGUI(GUI gui) {
	}
	
	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		//parser.setLogFile(new File("./BotStarter.log"));
		parser.run();
	}

}
