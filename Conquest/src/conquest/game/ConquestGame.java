package conquest.game;

import java.util.*;

import conquest.game.move.*;
import conquest.game.world.*;
import conquest.view.GUI;

public class ConquestGame implements Cloneable {
    public GameConfig config;
    GameMap map;
    PlayerInfo player1, player2;
    int round;
    public ArrayList<RegionData> pickableRegions;
    Random random;
    GUI gui;
    
    static final int nrOfStartingRegions = 3;
    
    // Create a game that is already in progress.
    public ConquestGame(GameConfig config, GameMap map, PlayerInfo player1, PlayerInfo player2,
                        int round, ArrayList<RegionData> pickableRegions, GUI gui) {
        this.config = config;
        this.map = map;
        this.player1 = player1 != null ? player1 : new PlayerInfo("1", "Player 1");
        this.player2 = player2 != null ? player2 : new PlayerInfo("2", "Player 2");
        this.round = round;
        this.pickableRegions = pickableRegions;
        
        if (config.seed < 0) {
            config.seed = new Random().nextInt();
        }
        while (config.seed < 0)
            config.seed += Integer.MAX_VALUE;
        this.random = new Random(config.seed);
                
        this.gui = gui;

        recalculateStartingArmies();
    }
    
    // Create a new game with the given configuration.
    public ConquestGame(GameConfig config, PlayerInfo player1, PlayerInfo player2, GUI gui) {
        this(config, makeInitMap(), player1, player2, 1, null, gui);
        initStartingRegions();
    }
    
    // Create a new game with default parameters.
    public ConquestGame() {
        this(new GameConfig(), null, null, null);
    }
    
    @Override
    public ConquestGame clone() {
        // Unfortunately java.util.Random is not cloneable.  So a cloned game will have its
        // own random number generator, and actions applied to it may have different results
        // than in the original game.
        
        return new ConquestGame(config, map.clone(), player1.clone(), player2.clone(),
                round, pickableRegions, gui);
    }
    
    public GameMap getMap() { return map; }

    public int getRoundNumber() {
        return round;
    }
    
    public PlayerInfo getPlayer(String playerId)
    {
        if(player1.getId().equals(playerId))
            return player1;
        if(player2.getId().equals(playerId))
            return player2;
        return null;
    }
    
    public PlayerInfo otherPlayer(String playerId) {
        if(player1.getId().equals(playerId))
            return player2;
        if(player2.getId().equals(playerId))
            return player1;
        return null;
    }
    
    public PlayerInfo winningPlayer()
    {
        if(map.ownedRegionsByPlayer(player1).isEmpty())
            return player2;
        if(map.ownedRegionsByPlayer(player2).isEmpty())
            return player1;
        return null;
    }
    
    public boolean isDone() {
        return winningPlayer() != null || round > config.maxGameRounds;
    }
    
    //calculate how many armies each player is able to place on the map for the next round
    public void recalculateStartingArmies()
    {
        player1.setArmiesPerTurn(config.startingArmies);
        player2.setArmiesPerTurn(config.startingArmies);
        
        for(ContinentData superRegion : map.getContinents())
        {
            PlayerInfo player = getPlayer(superRegion.ownedByPlayer());
            if(player != null)
                player.setArmiesPerTurn(player.getArmiesPerTurn() + superRegion.getArmiesReward());
        }
    }
    
    static GameMap makeInitMap()
    {
        GameMap map = new GameMap();
        
        // INIT SUPER REGIONS

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
        
        // INIT REGIONS
        
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
        
        // INIT NEIGHBOURS
        
        for (Region regionName : Region.values()) {
            RegionData region = regions.get(regionName);
            for (Region neighbour : regionName.getForwardNeighbours()) {
                region.addNeighbor(regions.get(neighbour));
            }
        }
        
        // ADD REGIONS TO THE MAP
        
        for (RegionData region : regions.values()) {
            map.add(region);
        }
        
        // ADD SUPER REGIONS TO THE MAP

        for (ContinentData superRegion : continents.values()) {
            map.add(superRegion);
        }

        //Make every region neutral with 2 armies to start with
        for(RegionData region : map.regions)
        {
            region.setPlayerName("neutral");
            region.setArmies(2);
        }

        return map;
    }
    
    void initStartingRegions() {
        ArrayList<RegionData> pickableRegions = new ArrayList<RegionData>();
        int regionsAdded = 0;
        
        //pick semi random regions to start with
        for(ContinentData superRegion : map.getContinents())
        {
            int nrOfRegions = superRegion.getSubRegions().size();
            while(regionsAdded < 2)
            {
                //get one random subregion from superRegion
                int randomRegionId = random.nextInt(nrOfRegions);
                
                RegionData randomRegion = superRegion.getSubRegions().get(randomRegionId);
                if(!pickableRegions.contains(randomRegion))
                {
                    pickableRegions.add(randomRegion);
                    regionsAdded++;
                }
            }
            regionsAdded = 0;
        }
    }
    
    public String validateStartingRegions(List<RegionData> regions) {
        if (regions.size() < nrOfStartingRegions * 2)
            return "not return enough preferred starting regions";
        
        for (int i = 0 ; i < regions.size() ; ++i) {
            RegionData r = regions.get(i);
            if (!pickableRegions.contains(r))
                return "chosen region is not in the given pickable regions list";
            
            for (int j = i + 1 ; j < regions.size() ; ++j)
                if (r == regions.get(j))
                    return "same starting region appears more than once";
        }
        
        return null;
    }
    
    public void distributeRegions(List<RegionData> p1Regions, List<RegionData> p2Regions) {
        String s = validateStartingRegions(p1Regions);
        if (s != null) throw new Error(s);
        s = validateStartingRegions(p2Regions);
        if (s != null) throw new Error(s);
        
        ArrayList<RegionData> givenP1Regions = new ArrayList<RegionData>();
        ArrayList<RegionData> givenP2Regions = new ArrayList<RegionData>();

        int i1 = 0, i2 = 0, n = 0;

        while(n < nrOfStartingRegions) {
            RegionData p1Region = p1Regions.get(i1);
            RegionData p2Region = p2Regions.get(i2);
            
            if(givenP2Regions.contains(p1Region)) {//preferred region for player1 is given to player2 already
                i1++;
            } else if(givenP1Regions.contains(p2Region)) {
                i2++;
            } else if(p1Region != p2Region) {
                p1Region.setPlayerName(player1.getId());
                p2Region.setPlayerName(player2.getId());
                givenP1Regions.add(p1Region);
                givenP2Regions.add(p2Region);
                n++; i1++; i2++;
            } else { //random player gets the region if same preference
                if(random.nextBoolean()) {
                    i1++;
                } else {
                    i2++;
                }
            }
        }
        
        recalculateStartingArmies();
    }
    
    void placeArmies(PlayerInfo player, List<PlaceArmiesMove> moves, List<Move> opponentMoves)
    {   
        int left = player.getArmiesPerTurn(); 
                
        for(PlaceArmiesMove move : moves)
        {
            RegionData region = move.getRegion();
            int armies = move.getArmies();
            
            if (!move.getPlayerName().equals(player.getId()))
                move.setIllegalMove("move by wrong player");
            else if (!region.ownedByPlayer(player.getId()))
                move.setIllegalMove(move.getRegion().getId() + " not owned");
            else if (armies < 1)
                move.setIllegalMove("cannot place less than 1 army");
            else if (left <= 0)
                move.setIllegalMove("no armies left to place");
            else {
                if(armies > left) //player wants to place more armies than he has left
                    move.setArmies(left); //place all armies he has left
                
                left -= armies;
                region.setArmies(region.getArmies() + armies);

                if (region.isVisible(otherPlayer(player.getId())))
                    opponentMoves.add(move);
            }
        }
    }
    
    public void placeArmies(List<PlaceArmiesMove> moves1, List<PlaceArmiesMove> moves2,
                            List<Move> opponentMoves) {
        placeArmies(player1, moves1, opponentMoves);
        placeArmies(player2, moves2, opponentMoves);
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
        RegionData fromRegion = move.getFromRegion();
        RegionData toRegion = move.getToRegion();
        int attackingArmies;
        int defendingArmies = toRegion.getArmies();
        
        if (fromRegion.getArmies() <= 1) {
            move.setIllegalMove(move.getFromRegion().getId() + " attack " + "only has 1 army");
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
            toRegion.setPlayerName(move.getPlayerName());
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

    void validateAttackTransfers(PlayerInfo player, List<AttackTransferMove> moves)
    {
        int[] totalFrom = new int[Region.LAST_ID + 1];
        
        for (int i = 0 ; i < moves.size() ; ++i) {
            AttackTransferMove move = moves.get(i);
            RegionData fromRegion = move.getFromRegion();
            RegionData toRegion = move.getToRegion();

            if (!move.getPlayerName().equals(player.getId()))
                move.setIllegalMove("move by wrong player");
            else if (!fromRegion.ownedByPlayer(player.getId()))
                move.setIllegalMove(fromRegion.getId() + " attack/transfer not owned");
            else if (!fromRegion.isNeighbor(toRegion))
                move.setIllegalMove(toRegion.getId() + " attack/transfer not a neighbor");
            else if (move.getArmies() < 1)
                move.setIllegalMove("attack/transfer cannot use less than 1 army");
            else if (totalFrom[fromRegion.getId()] + move.getArmies() >= fromRegion.getArmies())
                move.setIllegalMove(fromRegion.getId() +
                        " attack/transfer has used all available armies");
            else
                for (int j = 0 ; j < i ; ++j) {
                    AttackTransferMove n = moves.get(j);
                    if (n.getFromRegion() == fromRegion && n.getToRegion() == toRegion) {
                        move.setIllegalMove(fromRegion.getId() +
                                " attack/transfer has already attacked/transfered to this region");
                        break;
                    }
                }
        }
    }
    
    public void attackTransfer(List<AttackTransferMove> moves1, List<AttackTransferMove> moves2,
                               List<Move> opponentMoves) {
        validateAttackTransfers(player1, moves1);
        validateAttackTransfers(player2, moves2);
        List<List<AttackTransferMove>> moves = List.of(moves1, moves2);
        
        int count = 0;
        int turn = random.nextInt(2);   // current player to move (0 or 1)
        int[] index = new int[2];
        
        while(index[0] < moves.get(0).size() || index[1] < moves.get(1).size())
        {
            if (index[turn] >= moves.get(turn).size())  // out of moves
                turn = 1 - turn;  // switch to other player
            AttackTransferMove move = moves.get(turn).get(index[turn]++);  // next move for this player
            if(!move.getIllegalMove().equals("")) //the move is illegal
                continue;
            
            RegionData fromRegion = move.getFromRegion();
            RegionData toRegion = move.getToRegion();
            PlayerInfo player = turn == 0 ? player1 : player2;
            
            if (!fromRegion.ownedByPlayer(player.getId()))
                move.setIllegalMove(move.getFromRegion().getId() + " attack/transfer was taken this round");
            else if (fromRegion.getArmies() <= 1)
                move.setIllegalMove(move.getFromRegion().getId() + " attack/transfer only has 1 army");
            else {
                move.setArmies(Math.min(move.getArmies(), fromRegion.getArmies() - 1));

                PlayerInfo other = turn == 0 ? player2 : player1;
                if (move.getFromRegion().isVisible(other) || move.getToRegion().isVisible(other))
                    opponentMoves.add(move);
               
                if(toRegion.ownedByPlayer(player.getId())) //transfer
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
                
                ++count;
                if (index[1 - turn] < moves.get(1 - turn).size())  // other player still has moves
                    if (count % 2 == 0)
                        turn = random.nextInt(2);
                    else
                        turn = 1 - turn;
            }
        }
        
        recalculateStartingArmies();
        round++;
    }
}
