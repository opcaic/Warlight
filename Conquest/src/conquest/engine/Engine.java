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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import conquest.engine.robot.RobotParser;
import conquest.game.*;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;
import conquest.view.GUI;

public class Engine {
	
	public static enum FightMode {
		
		/**
		 * Original Warlight fight without luck:
		 * -- each attacking army has 60% chance to kill one defending army
		 * -- each defending army has 70% chance to kill one attacking army
		 * 
		 * You may use: {@link Engine#doAttack_ORIGINAL_A60_D70(Random, int, int)} method for off-engine simulation.
		 */
		ORIGINAL_A60_D70,

		/**
		 * RISK-like attack
		 * -- fight happens in round until one of side is fully wiped out
		 * -- each round there is a 60% chance that 1 defending army is killed and 70% chance that 1 attacking army is killed (independent variables)
		 * 
		 * You may use: {@link Engine#doAttack_CONTINUAL_1_1_A60_D70(Random, int, int)} method for off-engine simulation.
		 */
		CONTINUAL_1_1_A60_D70
		
	}
	
	public static class EngineConfig implements Cloneable {
		
		/**
		 * Non-negative seed => use concrete seed.
		 * Negative seed => pick random seed.
		 */
		public int seed = -1;
		
		public boolean fullyObservableGame = true;
		
		public long botCommandTimeoutMillis = 2000;
		
		public int startingArmies = 5;
		public int maxGameRounds = 100;
		
		public FightMode fight = FightMode.ORIGINAL_A60_D70;
		
		public GameConfig getGameConfig() {
		    return new GameConfig(fight);
		}
		
		public String asString() {
			return seed + ";" + fullyObservableGame + ";" + botCommandTimeoutMillis + ";" + startingArmies + ";" + maxGameRounds + ";" + fight;
		}
		
		public static EngineConfig fromString(String line) {
			EngineConfig result = new EngineConfig();
			
			String[] parts = line.split(";");
			
			result.seed = Integer.parseInt(parts[0]);
			result.fullyObservableGame = Boolean.parseBoolean(parts[1]);
			result.botCommandTimeoutMillis = Long.parseLong(parts[2]);
			result.startingArmies = Integer.parseInt(parts[3]);
			result.maxGameRounds = Integer.parseInt(parts[4]);
			result.fight = FightMode.valueOf(parts[5]);

			return result;
		}

		public String getCSVHeader() {
			return "seed;fullyObservable;timeoutMillis;startingArmies;maxGameRounds;fightMode";			
		}
		
		public String getCSV() {
			return seed + ";" + fullyObservableGame + ";" + botCommandTimeoutMillis + ";" + startingArmies + ";" + maxGameRounds + ";" + fight;
		}
		
	}
	
	private PlayerInfo player1;
	private PlayerInfo player2;
	private Robot robot1;
	private Robot robot2;
	private GameMap map;
	private RobotParser parser;
	private LinkedList<Move> opponentMoves;
	private GUI gui;
	
	private long timeoutMillis;
	private Random random;
	private int seed;
	private boolean fullyObservableGame;
	
	ConquestGame game;

	public Engine(GameMap initMap, PlayerInfo player1, PlayerInfo player2, Robot robot1, Robot robot2, GUI gui, EngineConfig config)
	{
		if (config.seed < 0) {
			config.seed = new Random().nextInt();
		}
		while (config.seed < 0) config.seed += Integer.MAX_VALUE;
		this.seed = config.seed;
		this.random = new Random(this.seed);
		
		this.fullyObservableGame = config.fullyObservableGame;
		
		this.gui = gui;
		this.map = initMap;
		game = new ConquestGame(config.getGameConfig(), map, player1, player2, random, gui);
		this.player1 = player1;
		this.player2 = player2;
		this.robot1 = robot1;
		this.robot2 = robot2;
		this.timeoutMillis = config.botCommandTimeoutMillis;		
		
		parser = new RobotParser(map);
	}
	
    private List<PlaceArmiesMove> placeArmiesMoves(String input, PlayerInfo player) {
        ArrayList<PlaceArmiesMove> moves = new ArrayList<PlaceArmiesMove>();
        
        for (Move move : parser.parseMoves(input, player))
            if (move instanceof PlaceArmiesMove)
                moves.add((PlaceArmiesMove) move);
            else
                System.err.println("INVALID MOVE: " + move);
        
        return moves;
    }

    private List<AttackTransferMove> attackTransferMoves(String input, PlayerInfo player) {
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
			gui.newRound(game.getRoundNr());
			gui.updateRegions(map.regions);
		}
		
		List<PlaceArmiesMove> placeMoves1 =
		    placeArmiesMoves(robot1.getPlaceArmiesMoves(timeoutMillis), player1);
		
        List<PlaceArmiesMove> placeMoves2 =
            placeArmiesMoves(robot2.getPlaceArmiesMoves(timeoutMillis), player2);
		
		game.placeArmies(placeMoves1, placeMoves2, opponentMoves);

		sendUpdateMapInfo(player1, robot1);
		sendUpdateMapInfo(player2, robot2);
		
		if (gui != null) {
	        List<PlaceArmiesMove> legalMoves = new ArrayList<PlaceArmiesMove>();

	        for (PlaceArmiesMove move : placeMoves1)
	            if (move.getIllegalMove().equals(""))
	                legalMoves.add(move);
            
	        for (PlaceArmiesMove move : placeMoves2)
                if (move.getIllegalMove().equals(""))
                    legalMoves.add(move);
	        
			gui.placeArmies(map.regions, legalMoves);
		}
		
		List<AttackTransferMove> moves1 = attackTransferMoves(robot1.getAttackTransferMoves(timeoutMillis), player1);
        List<AttackTransferMove> moves2 = attackTransferMoves(robot2.getAttackTransferMoves(timeoutMillis), player2);
		
		game.attackTransfer(moves1, moves2, opponentMoves);
		
		if (gui != null) {
			gui.updateAfterRound(map);
		}
		
		game.recalculateStartingArmies();
		sendAllInfo();	
		game.newRound();
	}
	
	public void distributeStartingRegions()
	{
	    ArrayList<RegionData> pickableRegions = game.pickableRegions;
	    
		if (gui != null) {
			gui.pickableRegions(pickableRegions);
		}
		
		//get the preferred starting regions from the players
		List<RegionData> p1Regions = parser.parsePreferredStartingRegions(
		        robot1.getPreferredStartingArmies(timeoutMillis, pickableRegions), pickableRegions, player1);
		List<RegionData> p2Regions = parser.parsePreferredStartingRegions(
		        robot2.getPreferredStartingArmies(timeoutMillis, pickableRegions), pickableRegions, player2);
		
		//if the bot did not correctly return his starting regions, get some random ones
		if(game.validateStartingRegions(p1Regions) != null) {
			p1Regions = getRandomStartingRegions(pickableRegions);
		}
		if(game.validateStartingRegions(p2Regions) != null) {
		    p2Regions = getRandomStartingRegions(pickableRegions);
		}

		game.distributeRegions(p1Regions, p2Regions);
		
		if (gui != null) {
			gui.regionsChosen(map.regions);
		}
	}
	
	private List<RegionData> getRandomStartingRegions(ArrayList<RegionData> pickableRegions)
	{
		List<RegionData> startingRegions = new ArrayList<RegionData>(pickableRegions);
		Collections.shuffle(startingRegions);
		return startingRegions.subList(0,6);
	}
	
	public void sendAllInfo()
	{
		sendStartingArmiesInfo(player1, robot1);
		sendStartingArmiesInfo(player2, robot2);
		sendUpdateMapInfo(player1, robot1);
		sendUpdateMapInfo(player2, robot2);
		sendOpponentMovesInfo(player1, robot1);
		sendOpponentMovesInfo(player2, robot2);
		opponentMoves.clear();
	}
		
	//inform the player about how much armies he can place at the start next round
	private void sendStartingArmiesInfo(PlayerInfo player, Robot bot)
	{
		String updateStartingArmiesString = "settings starting_armies";
		
		updateStartingArmiesString = updateStartingArmiesString.concat(" " + player.getArmiesLeft());
		
		//System.out.println("sending to " + player.getName() + ": " + updateStartingArmiesString);
		bot.writeInfo(updateStartingArmiesString);
	}
	
	//inform the player about how his visible map looks now
	private void sendUpdateMapInfo(PlayerInfo player, Robot bot)
	{
		LinkedList<RegionData> visibleRegions;
		if (fullyObservableGame) {
			visibleRegions = map.regions;
		} else {
			visibleRegions = map.visibleRegionsForPlayer(player);
		}
		String updateMapString = "update_map";
		for(RegionData region : visibleRegions)
		{
			int id = region.getId();
			String playerName = region.getPlayerName();
			int armies = region.getArmies();
			
			updateMapString = updateMapString.concat(" " + id + " " + playerName + " " + armies);
		}
		bot.writeInfo(updateMapString);
	}

	private void sendOpponentMovesInfo(PlayerInfo player, Robot bot)
	{
		String opponentMovesString = "opponent_moves ";

		for(Move move : opponentMoves)
		    if (!move.getPlayerName().equals(player.getId()) &&  // move was by other player
			    move.getIllegalMove().equals(""))
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

		bot.writeInfo(opponentMovesString);
	}
}
