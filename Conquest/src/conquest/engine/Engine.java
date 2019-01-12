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

package conquest.engine;

import java.util.ArrayList;
import java.util.List;

import conquest.engine.robot.HumanRobot;
import conquest.engine.robot.RobotParser;
import conquest.game.*;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;
import conquest.view.GUI;

public class Engine {
    ConquestGame game;
	
	private Robot[] robots;
	private long timeoutMillis;
	private RobotParser parser;
	private GUI gui;
	
	public Engine(ConquestGame game, Robot[] robots, GUI gui, long timeoutMillis)
	{
	    this.game = game;
	    
		this.gui = gui;
		
		this.robots = robots;
		this.timeoutMillis = timeoutMillis;		
		
		parser = new RobotParser();
	}
	
	Robot robot(int i) {
	    return robots[i - 1];
	}
	
    private List<PlaceArmiesMove> placeArmiesMoves(String input, int player) {
        ArrayList<PlaceArmiesMove> moves = new ArrayList<PlaceArmiesMove>();
        
        for (Move move : parser.parseMoves(input, player))
            if (move instanceof PlaceArmiesMove)
                moves.add((PlaceArmiesMove) move);
            else
                System.err.println("INVALID MOVE: " + move);
        
        return moves;
    }

    private List<AttackTransferMove> attackTransferMoves(String input, int player) {
        ArrayList<AttackTransferMove> moves = new ArrayList<AttackTransferMove>();
        
        for (Move move : parser.parseMoves(input, player))
            if (move instanceof AttackTransferMove)
                moves.add((AttackTransferMove) move);
            else
                System.err.println("INVALID MOVE: " + move);
        
        return moves;
    }

	public void playRound()
	{
		if (gui != null) {
			gui.newRound(game.getRoundNumber());
			gui.updateRegions(game.getMap().regions);
		}
		
		for (int i = 1 ; i <= 2 ; ++i) {
			ArrayList<Move> opponentMoves = new ArrayList<Move>();
			
    		List<PlaceArmiesMove> placeMoves =
    		    placeArmiesMoves(robot(i).getPlaceArmiesMoves(timeoutMillis), i);
    		
    		game.placeArmies(placeMoves, opponentMoves);
    
    		sendUpdateMapInfo(i);
    		
    		if (gui != null && !(robot(i) instanceof HumanRobot)) {
    	        List<PlaceArmiesMove> legalMoves = new ArrayList<PlaceArmiesMove>();
    
    	        for (PlaceArmiesMove move : placeMoves)
    	            if (move.getIllegalMove().equals(""))
    	                legalMoves.add(move);
                
    			gui.placeArmies(i, game.getMap().regions, legalMoves);
    		}
    		
    		List<AttackTransferMove> moves =
    		    attackTransferMoves(robot(i).getAttackTransferMoves(timeoutMillis), i);
    		
    		game.attackTransfer(moves, opponentMoves);
    		
    		sendAllInfo();
    		
	        sendOpponentMovesInfo(3 - i, opponentMoves);    		
    		
    		if (game.isDone())
    		    break;
		}
		
		if (gui != null) {
			gui.updateMap();
		}
		nextRound();	
	}
	
	public void distributeStartingRegions()
	{
	    ArrayList<Region> pickableRegions = game.pickableRegions;
	    
		if (gui != null) {
			gui.pickableRegions();
		}
		
		for (int i = 1 ; i <= ConquestGame.nrOfStartingRegions ; ++i)
    	    for (int p = 1 ; p <= 2 ; ++p) {
    	    	sendUpdateMapInfo(p);
        		Region region = parser.parseStartingRegion(
        		    robot(p).getStartingRegion(timeoutMillis, pickableRegions));
        		
        		//if the bot did not correctly return a starting region, get some random ones
        		if (!game.pickableRegions.contains(region)) {
        		    System.err.println("invalid starting region; choosing one at random");
        			region = getRandomStartingRegion();
        		}
        
        		game.chooseRegion(region);
        		if (gui != null)
        		    gui.updateMap();
    	    }
        
        if (gui != null) {
            gui.regionsChosen(game.getMap().regions);
        }
	}
	
	private Region getRandomStartingRegion()
	{
		return game.pickableRegions.get(game.random.nextInt(game.pickableRegions.size()));
	}
	
	public void sendAllInfo()
	{
	    for (int i = 1 ; i <= 2 ; ++i)
	        sendUpdateMapInfo(i);
	}
	
	//inform the player about how much armies he can place at the start next round
	private void sendStartingArmiesInfo(int player)
	{
		String updateStartingArmiesString = "settings starting_armies " + game.armiesPerTurn(player);
		robot(player).writeInfo(updateStartingArmiesString);
	}
	
	public void nextRound() {
	    for (int i = 1 ; i <= 2 ; ++i) {
            robot(i).writeInfo("next_round");
            sendStartingArmiesInfo(i);
	    }
	}
		
	//inform the player about how his visible map looks now
	private void sendUpdateMapInfo(int player)
	{
		ArrayList<RegionData> visibleRegions;
		if (game.config.fullyObservableGame) {
			visibleRegions = game.getMap().regions;
		} else {
			visibleRegions = game.getMap().visibleRegionsForPlayer(player);
		}
		String updateMapString = "update_map";
		for(RegionData region : visibleRegions)
		{
			int id = region.getId();
			int owner = region.getOwner();
			int armies = region.getArmies();
			
			updateMapString = updateMapString.concat(" " + id + " " + owner + " " + armies);
		}
		robot(player).writeInfo(updateMapString);
	}

	private void sendOpponentMovesInfo(int player, ArrayList<Move> moves)
	{
		String opponentMovesString = "opponent_moves ";

		for(Move move : moves)
		    if (move.getIllegalMove().equals(""))
			{
				if (move instanceof PlaceArmiesMove) {
					PlaceArmiesMove plm = (PlaceArmiesMove) move;
					opponentMovesString = opponentMovesString.concat(plm.getString() + " ");
				}
				else {
					AttackTransferMove atm = (AttackTransferMove) move;
					opponentMovesString = opponentMovesString.concat(atm.getString() + " ");					
				}
			}
		
		opponentMovesString = opponentMovesString.substring(0, opponentMovesString.length()-1);

		robot(player).writeInfo(opponentMovesString);
	}
}
