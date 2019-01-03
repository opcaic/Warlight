package conquest.game;

import java.util.*;

import conquest.game.move.*;
import conquest.game.world.*;
import conquest.view.GUI;

public class ConquestGame implements Cloneable {
    public GameConfig config;
    GameMap map;
    String[] playerNames;
    int round;
    int turn;
    Phase phase;
    public ArrayList<Region> pickableRegions;
    public Random random;
    GUI gui;
    
    public static final int nrOfStartingRegions = 3;
    
    // Create a game that is already in progress.
    public ConquestGame(GameConfig config, GameMap map, String[] playerNames,
                        int round, int turn, Phase phase, ArrayList<Region> pickableRegions) {
        this.config = config;
        this.map = map;
        this.playerNames = playerNames != null ? playerNames : new String[] { "Player 1", "Player 2" };
        this.round = round;
        this.turn = turn;
        this.phase = phase;
        this.pickableRegions = pickableRegions;
        
        if (config.seed < 0) {
            config.seed = new Random().nextInt();
        }
        while (config.seed < 0)
            config.seed += Integer.MAX_VALUE;
        this.random = new Random(config.seed);
    }
    
    // Create a new game with the given configuration.
    public ConquestGame(GameConfig config, String[] playerNames) {
        this(config, makeInitMap(), playerNames, 0, 1, Phase.STARTING_REGIONS, null);
        initStartingRegions();
    }
    
    // Create a new game with default parameters.
    public ConquestGame() {
        this(new GameConfig(), null);
    }
    
    public void setGUI(GUI gui) {
        this.gui = gui;
    }
    
    @Override
    public ConquestGame clone() {
        // Unfortunately java.util.Random is not cloneable.  So a cloned game will have its
        // own random number generator, and actions applied to it may have different results
        // than in the original game.
        
        return new ConquestGame(config, map.clone(), playerNames, round, turn, phase,
        		                new ArrayList<Region>(pickableRegions));
    }
    
    public GameMap getMap() { return map; }

    public int getRoundNumber() {
        return round;
    }
    
    public int getTurn() {
        return turn;
    }
    
    public Phase getPhase() {
    	return phase;
    }
    
    public String playerName(int i) {
        return playerNames[i - 1];
    }
    
    public int winningPlayer()
    {
        for (int i = 1 ; i <= 2 ; ++i)
            if (map.numberRegionsOwned(i) == 0)
            return 3 - i;
        
        return 0;
    }
    
    public boolean isDone() {
        return round > 0 && (winningPlayer() > 0 || round > config.maxGameRounds);
    }
    
    public List<Region> getPickableRegions() {
    	return pickableRegions;
    }
    
    //calculate how many armies a player is able to place on the map each round
    public int armiesPerTurn(int player)
    {
        int armies = config.startingArmies;
        if (player == 1 && round <= 1)
            armies /= 2;
        
        for(ContinentData cd : map.getContinents())
            if (cd.owner() == player)
                armies += cd.getArmiesReward();
        
        return armies;
    }
    
    static GameMap makeInitMap()
    {
        GameMap map = new GameMap();
        
        Map<Continent, ContinentData> continents = new TreeMap<Continent, ContinentData>(new Comparator<Continent>() {
            @Override
            public int compare(Continent o1, Continent o2) {
                return o1.id - o2.id;
            }           
        });
        
        for (Continent continent : Continent.values()) {
            ContinentData continentData = new ContinentData(continent, continent.id, continent.reward);
            continents.put(continent, continentData);
        }
        
        Map<Region, RegionData> regions = new TreeMap<Region, RegionData>(new Comparator<Region>() {
            @Override
            public int compare(Region o1, Region o2) {
                return o1.id - o2.id;
            }
        });
        
        for (Region region : Region.values()) {
            RegionData regionData = new RegionData(region, region.id, continents.get(region.continent));
            regions.put(region, regionData);
        }
        
        for (Region regionName : Region.values()) {
            RegionData region = regions.get(regionName);
            for (Region neighbour : regionName.getForwardNeighbours()) {
                region.addNeighbor(regions.get(neighbour));
            }
        }
        
        for (RegionData region : regions.values()) {
            map.add(region);
        }
        
        for (ContinentData continent : continents.values()) {
            map.add(continent);
        }

        // Make every region neutral with 2 armies to start with
        for(RegionData region : map.regions)
        {
            region.setOwner(0);
            region.setArmies(2);
        }

        return map;
    }
    
    void initStartingRegions() {
        pickableRegions = new ArrayList<Region>();
        int regionsAdded = 0;
        
        //pick semi random regions to start with
        for(Continent continent : Continent.values())
        {
            int nrOfRegions = continent.getRegions().size();
            while(regionsAdded < 2)
            {
                //get one random subregion from continent
                int randomRegionId = random.nextInt(nrOfRegions);
                
                Region randomRegion = continent.getRegions().get(randomRegionId);
                if(!pickableRegions.contains(randomRegion))
                {
                    pickableRegions.add(randomRegion);
                    regionsAdded++;
                }
            }
            regionsAdded = 0;
        }
    }
    
    public void chooseRegion(Region region) {
    	if (phase != Phase.STARTING_REGIONS)
    		throw new Error("cannot choose regions after game has begun");
    	
        if (!pickableRegions.contains(region))
            throw new Error("starting region is not pickable");
        
        map.getRegionData(region).setOwner(turn);
        pickableRegions.remove(region);
        turn = 3 - turn;
        
        if (map.numberRegionsOwned(turn) == nrOfStartingRegions) {
        	round = 1;
        	phase = Phase.PLACE_ARMIES;
        }
    }
    
    public void placeArmies(List<PlaceArmiesMove> moves, List<Move> opponentMoves)
    {
    	if (phase != Phase.PLACE_ARMIES)
    		throw new Error("wrong time to place armies");

        int left = armiesPerTurn(turn); 
                
        for(PlaceArmiesMove move : moves)
        {
            RegionData region = map.getRegionData(move.getRegion());
            int armies = move.getArmies();
            
            if (!region.ownedByPlayer(turn))
                move.setIllegalMove(region.getId() + " not owned");
            else if (armies < 1)
                move.setIllegalMove("cannot place less than 1 army");
            else if (left <= 0)
                move.setIllegalMove("no armies left to place");
            else {
                if(armies > left) //player wants to place more armies than he has left
                    move.setArmies(left); //place all armies he has left
                
                left -= armies;
                region.setArmies(region.getArmies() + armies);

                if (opponentMoves != null && region.isVisible(3 - turn))
                    opponentMoves.add(move);
            }
        }
        
        phase = Phase.ATTACK_TRANSFER;
    }
    
    public static enum FightSide {
        ATTACKER,
        DEFENDER
    }
    
    public static class FightResult {
        public FightSide winner;
        
        public int attackersDestroyed;
        public int defendersDestroyed;
        
        public FightResult() {
            winner = null;
            attackersDestroyed = 0;         
            defendersDestroyed = 0;
        }
        
        public FightResult(FightSide winner, int attackersDestroyed, int defendersDestroyed) {
            this.winner = winner;
            this.attackersDestroyed = attackersDestroyed;
            this.defendersDestroyed = defendersDestroyed;
        }
        
        protected void postProcessFightResult(int attackingArmies, int defendingArmies) {      
            if(attackersDestroyed >= attackingArmies)
            {
                if (defendersDestroyed >= defendingArmies)
                    defendersDestroyed = defendingArmies - 1;
                
                attackersDestroyed = attackingArmies;
            }   
            
            if (defendersDestroyed >= defendingArmies) { //attack success
                winner = FightSide.ATTACKER;
            } else {
                winner = FightSide.DEFENDER;
            }
        }
    }

    static FightResult doOriginalAttack(Random random, int attackingArmies, int defendingArmies,
                                        double defenderDestroyedChance, double attackerDestroyedChance) {
        FightResult result = new FightResult();
        
        for(int t=1; t<=attackingArmies; t++) //calculate how much defending armies are destroyed
        {
            double rand = random.nextDouble();
            if(rand < defenderDestroyedChance) //60% chance to destroy one defending army
                result.defendersDestroyed++;
        }
        for(int t=1; t<=defendingArmies; t++) //calculate how much attacking armies are destroyed
        {
            double rand = random.nextDouble();
            if(rand < attackerDestroyedChance) //70% chance to destroy one attacking army
                result.attackersDestroyed++;
        }
        result.postProcessFightResult(attackingArmies, defendingArmies);
        return result;
    }
    
    static FightResult doContinualAttack(Random random,
            int attackingArmies, int defendingArmies,
            double defenderDestroyedChance, double attackerDestroyedChance) {
        
        FightResult result = new FightResult();
        
        while (result.attackersDestroyed < attackingArmies && result.defendersDestroyed < defendingArmies) {
            // ATTACKERS STRIKE
            double rand = random.nextDouble();
            if (rand < defenderDestroyedChance) ++result.defendersDestroyed;
            
            // DEFENDERS STRIKE
            rand = random.nextDouble();
            if (rand < attackerDestroyedChance) ++result.attackersDestroyed;
        }
        
        result.postProcessFightResult(attackingArmies, defendingArmies);
        return result;
    }

    static FightResult doAttack_ORIGINAL_A60_D70(
            Random random, int attackingArmies, int defendingArmies) {
        
        return doOriginalAttack(random, attackingArmies, defendingArmies, 0.6, 0.7);
    }
    
    static FightResult doAttack_CONTINUAL_1_1_A60_D70(
            Random random, int attackingArmies, int defendingArmies) {
        
        return doContinualAttack(random, attackingArmies, defendingArmies, 0.6, 0.7);
    }
    
    //see wiki.warlight.net/index.php/Combat_Basics
    private void doAttack(AttackTransferMove move)
    {
        RegionData fromRegion = map.getRegionData(move.getFromRegion());
        RegionData toRegion = map.getRegionData(move.getToRegion());
        int attackingArmies;
        int defendingArmies = toRegion.getArmies();
        
        if (fromRegion.getArmies() <= 1) {
            move.setIllegalMove(fromRegion.getId() + " attack " + "only has 1 army");
            return;
        }
        
        if(fromRegion.getArmies()-1 >= move.getArmies()) //are there enough armies on fromRegion?
            attackingArmies = move.getArmies();
        else
            attackingArmies = fromRegion.getArmies()-1;
        
        FightResult result = null;
        
        switch (config.fight) {
        case ORIGINAL_A60_D70:
            result = doAttack_ORIGINAL_A60_D70(random, attackingArmies, defendingArmies);
            break;
        case CONTINUAL_1_1_A60_D70:
            result = doAttack_CONTINUAL_1_1_A60_D70(random, attackingArmies, defendingArmies);
            break;
        }
        
        switch (result.winner) {
        case ATTACKER: //attack success
            fromRegion.setArmies(fromRegion.getArmies() - attackingArmies);
            toRegion.setOwner(turn);
            toRegion.setArmies(attackingArmies - result.attackersDestroyed);
            break; 
        case DEFENDER: //attack fail
            fromRegion.setArmies(fromRegion.getArmies() - result.attackersDestroyed);
            toRegion.setArmies(toRegion.getArmies() - result.defendersDestroyed);
            break;
        default:
            throw new RuntimeException("Unhandled FightResult.winner: " + result.winner);
        }
        
        if (gui != null) {
            gui.attackResult(fromRegion, toRegion, result.attackersDestroyed, result.defendersDestroyed);
        }
    }

    void validateAttackTransfers(List<AttackTransferMove> moves)
    {
        int[] totalFrom = new int[Region.LAST_ID + 1];
        
        for (int i = 0 ; i < moves.size() ; ++i) {
            AttackTransferMove move = moves.get(i);
            RegionData fromRegion = map.getRegionData(move.getFromRegion());
            RegionData toRegion = map.getRegionData(move.getToRegion());

            if (!fromRegion.ownedByPlayer(turn))
                move.setIllegalMove(fromRegion.getId() + " attack/transfer not owned");
            else if (!fromRegion.isNeighbor(toRegion))
                move.setIllegalMove(toRegion.getId() + " attack/transfer not a neighbor");
            else if (move.getArmies() < 1)
                move.setIllegalMove("attack/transfer cannot use less than 1 army");
            else if (totalFrom[fromRegion.getId()] + move.getArmies() >= fromRegion.getArmies())
                move.setIllegalMove(fromRegion.getId() +
                        " attack/transfer has used all available armies");
            else {
                for (int j = 0 ; j < i ; ++j) {
                    AttackTransferMove n = moves.get(j);
                    if (n.getFromRegion() == move.getFromRegion() && n.getToRegion() == move.getToRegion()) {
                        move.setIllegalMove(fromRegion.getId() +
                                " attack/transfer has already attacked/transfered to this region");
                        break;
                    }
                }
                totalFrom[fromRegion.getId()] += move.getArmies();
            }
        }
    }
    
    public void attackTransfer(List<AttackTransferMove> moves, List<Move> opponentMoves) {
    	if (phase != Phase.ATTACK_TRANSFER)
    		throw new Error("wrong time to attack/transfer");

        validateAttackTransfers(moves);
        
        for (AttackTransferMove move : moves) {
            if(!move.getIllegalMove().equals("")) //the move is illegal
                continue;
            
            RegionData fromRegion = map.getRegionData(move.getFromRegion());
            RegionData toRegion = map.getRegionData(move.getToRegion());
            
            move.setArmies(Math.min(move.getArmies(), fromRegion.getArmies() - 1));

            int other = 3 - turn;
            if (opponentMoves != null && (fromRegion.isVisible(other) || toRegion.isVisible(other)) )
                opponentMoves.add(move);
           
            if(toRegion.ownedByPlayer(turn)) //transfer
            {
                if (gui != null) {
                    gui.transfer(move);
                }
                fromRegion.setArmies(fromRegion.getArmies() - move.getArmies());
                toRegion.setArmies(toRegion.getArmies() + move.getArmies());
            }
            else //attack
            {
                if (gui != null) {
                    gui.attack(move);
                }
                doAttack(move);
            }
        }
        
        turn = 3 - turn;
        phase = Phase.PLACE_ARMIES;
        if (turn == 1)
            round++;
    }
}
