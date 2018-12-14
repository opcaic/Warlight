package conquest.game;

import java.util.*;

import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.MoveQueue;
import conquest.game.move.PlaceArmiesMove;
import conquest.view.GUI;

public class ConquestGame {
    GameConfig config;
    GameMap map;
    PlayerInfo player1, player2;
    int roundNr;
    public ArrayList<RegionData> pickableRegions;
    Random random;
    private MoveQueue moveQueue;
    public List<PlaceArmiesMove> lastPlaceArmies;
    private GUI gui;
    
    static final int nrOfStartingRegions = 3;
    
    public ConquestGame(GameConfig config, GameMap map, PlayerInfo player1, PlayerInfo player2, Random random, GUI gui) {
        this.config = config;
        this.map = map;
        this.player1 = player1;
        this.player2 = player2;
        this.random = random;
        roundNr = 1;
        this.gui = gui;

        initStartingRegions();
    }

    public int getRoundNr() {
        return roundNr;
    }
    
    public void newRound() {
        roundNr++;
    }
    
    public PlayerInfo getPlayer(String playerName)
    {
        if(player1.getId().equals(playerName))
            return player1;
        else if(player2.getId().equals(playerName))
            return player2;
        else
            return null;
    }
    
    public PlayerInfo winningPlayer()
    {
        if(map.ownedRegionsByPlayer(player1).isEmpty())
            return player2;
        else if(map.ownedRegionsByPlayer(player2).isEmpty())
            return player1;
        else
            return null;
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
                double rand = random.nextDouble();
                int randomRegionId = (int) (rand*nrOfRegions);
                RegionData randomRegion = superRegion.getSubRegions().get(randomRegionId); //get one random subregion from superRegion
                if(!pickableRegions.contains(randomRegion))
                {
                    pickableRegions.add(randomRegion);
                    regionsAdded++;
                }
            }
            regionsAdded = 0;
        }
    }
    
    public void distributeRegions(ArrayList<RegionData> p1Regions, ArrayList<RegionData> p2Regions) {
        ArrayList<RegionData> givenP1Regions = new ArrayList<RegionData>();
        ArrayList<RegionData> givenP2Regions = new ArrayList<RegionData>();

        int i1, i2, n;
        i1 = 0; i2 = 0;
        n = 0;

        while(n < nrOfStartingRegions) {
            RegionData p1Region = p1Regions.get(i1);
            RegionData p2Region = p2Regions.get(i2);
            
            if(givenP2Regions.contains(p1Region)) {//preferred region for player1 is not given to player2 already
                i1++;
            } else if(givenP1Regions.contains(p2Region)) { //preferred region for player2 is not given to player1 already
                i2++;
            } else if(p1Region != p2Region) {
                p1Region.setPlayerName(player1.getId());
                p2Region.setPlayerName(player2.getId());
                givenP1Regions.add(p1Region);
                givenP2Regions.add(p2Region);
                n++; i1++; i2++;
            } else { //random player gets the region if same preference
                double rand = random.nextDouble();
                if(rand < 0.5) {
                    i1++;
                } else {
                    i2++;
                }
            }
        }
        
        recalculateStartingArmies();
    }
    
    //calculate how many armies each player is able to place on the map for the next round
    public void recalculateStartingArmies()
    {
        player1.setArmiesLeft(player1.getArmiesPerTurn());
        player2.setArmiesLeft(player2.getArmiesPerTurn());
        
        for(ContinentData superRegion : map.getContinents())
        {
            PlayerInfo player = getPlayer(superRegion.ownedByPlayer());
            if(player != null)
                player.setArmiesLeft(player.getArmiesLeft() + superRegion.getArmiesReward());
        }
    }
    
    public void queuePlaceArmies(PlaceArmiesMove plm)
    {
        RegionData region = plm.getRegion();
        PlayerInfo player = getPlayer(plm.getPlayerName());
        int armies = plm.getArmies();
        
        //check legality
        if(region.ownedByPlayer(player.getId()))
        {
            if(armies < 1)
            {
                plm.setIllegalMove(" place-armies " + "cannot place less than 1 army");
            }
            else
            {
                if(armies > player.getArmiesLeft()) //player wants to place more armies than he has left
                    plm.setArmies(player.getArmiesLeft()); //place all armies he has left
                if(player.getArmiesLeft() <= 0)
                    plm.setIllegalMove(" place-armies " + "no armies left to place");
                
                player.setArmiesLeft(player.getArmiesLeft() - plm.getArmies());
            }
        }
        else
            plm.setIllegalMove(plm.getRegion().getId() + " place-armies " + " not owned");

        moveQueue.addMove(plm);
    }
    
    public void executePlaceArmies(LinkedList<Move> opponentMovesPlayer1,
                                   LinkedList<Move> opponentMovesPlayer2)
    {       
        List<PlaceArmiesMove> legalMoves = new ArrayList<PlaceArmiesMove>();
        
        for(PlaceArmiesMove move : moveQueue.placeArmiesMoves)
        {
            if(move.getIllegalMove().equals("")) {
                move.getRegion().setArmies(move.getRegion().getArmies() + move.getArmies());
                legalMoves.add(move);
            }
            
            if(map.visibleRegionsForPlayer(player1).contains(move.getRegion()))
            {
                if(move.getPlayerName().equals(player2.getId()))
                    opponentMovesPlayer1.add(move); //for the opponent_moves output
            }
            if(map.visibleRegionsForPlayer(player2).contains(move.getRegion()))
            {
                if(move.getPlayerName().equals(player1.getId()))
                    opponentMovesPlayer2.add(move); //for the opponent_moves output
            }
        }
        
        if (gui != null) {
            lastPlaceArmies = legalMoves;
        }
    }
    
    public void queueAttackTransfer(AttackTransferMove atm)
    {
        RegionData fromRegion = atm.getFromRegion();
        RegionData toRegion = atm.getToRegion();
        PlayerInfo player = getPlayer(atm.getPlayerName());
        int armies = atm.getArmies();
        
        //check legality
        if(fromRegion.ownedByPlayer(player.getId()))
        {
            if(fromRegion.isNeighbor(toRegion))
            {
                if(armies < 1)
                    atm.setIllegalMove(" attack/transfer " + "cannot use less than 1 army");
            }
            else
                atm.setIllegalMove(atm.getToRegion().getId() + " attack/transfer " + "not a neighbor");
        }
        else
            atm.setIllegalMove(atm.getFromRegion().getId() + " attack/transfer " + "not owned");
        moveQueue.addMove(atm);
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

    public static FightResult doOriginalAttack(Random random, int attackingArmies, int defendingArmies, double defenderDestroyedChance, double attackerDestroyedChance) {
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
    
    public static FightResult doContinualAttack(Random random,
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

    public static FightResult doAttack_ORIGINAL_A60_D70(Random random, int attackingArmies, int defendingArmies) {
        return doOriginalAttack(random, attackingArmies, defendingArmies, 0.6, 0.7);
    }
    
    public static FightResult doAttack_CONTINUAL_1_1_A60_D70(Random random, int attackingArmies, int defendingArmies) {
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
        case ORIGINAL_A60_D70:      result = doAttack_ORIGINAL_A60_D70(     random, attackingArmies, defendingArmies); break;
        case CONTINUAL_1_1_A60_D70: result = doAttack_CONTINUAL_1_1_A60_D70(random, attackingArmies, defendingArmies); break;
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

    public void executeAttackTransfer(LinkedList<Move> opponentMovesPlayer1,
                                            LinkedList<Move> opponentMovesPlayer2)
    {    
        LinkedList<RegionData> visibleRegionsPlayer1Map = map.visibleRegionsForPlayer(player1);
        LinkedList<RegionData> visibleRegionsPlayer2Map = map.visibleRegionsForPlayer(player2);
        LinkedList<RegionData> visibleRegionsPlayer1OldMap = visibleRegionsPlayer1Map;
        LinkedList<RegionData> visibleRegionsPlayer2OldMap = visibleRegionsPlayer2Map;
        ArrayList<ArrayList<Integer>> usedRegions = new ArrayList<ArrayList<Integer>>();
        for(int i = 0; i<=42; i++) {
            usedRegions.add(new ArrayList<Integer>());
        }
        GameMap oldMap = map.getMapCopy();

        int moveNr = 1;
        Boolean previousMoveWasIllegal = false;
        String previousMovePlayer = "";
        while(moveQueue.hasNextAttackTransferMove())
        {   
            AttackTransferMove move = moveQueue.getNextAttackTransferMove(
                    moveNr, previousMovePlayer, previousMoveWasIllegal);
            
            if(move.getIllegalMove().equals("")) //the move is legal
            {
                RegionData fromRegion = move.getFromRegion();
                RegionData oldFromRegion = oldMap.getRegion(move.getFromRegion().getId());
                RegionData toRegion = move.getToRegion();
                PlayerInfo player = getPlayer(move.getPlayerName());
                
                //check if the fromRegion still belongs to this player
                if(fromRegion.ownedByPlayer(player.getId())) 
                {
                    //between two regions there can only be attacked/transfered once
                    if(!usedRegions.get(fromRegion.getId()).contains(toRegion.getId())) 
                    {
                        if(oldFromRegion.getArmies() > 1) //there are still armies that can be used
                        {
                            //not enough armies on fromRegion at the start of the round?
                            if(oldFromRegion.getArmies() < fromRegion.getArmies() &&
                                    oldFromRegion.getArmies() - 1 < move.getArmies()) 
                                move.setArmies(oldFromRegion.getArmies() - 1); //move the maximal number.
                            else //not enough armies on fromRegion currently?
                                if(oldFromRegion.getArmies() >= fromRegion.getArmies() &&
                                       fromRegion.getArmies() - 1 < move.getArmies()) 
                                    move.setArmies(fromRegion.getArmies() - 1); //move the maximal number.

                            //update oldFromRegion so new armies cannot be used yet
                            oldFromRegion.setArmies(oldFromRegion.getArmies() - move.getArmies()); 

                            if(toRegion.ownedByPlayer(player.getId())) //transfer
                            {
                                if(fromRegion.getArmies() > 1)
                                {
                                    if (gui != null) {
                                        gui.transfer(move);
                                    }
                                    fromRegion.setArmies(fromRegion.getArmies() - move.getArmies());
                                    toRegion.setArmies(toRegion.getArmies() + move.getArmies());
                                    usedRegions.get(fromRegion.getId()).add(toRegion.getId());
                                }
                                else
                                    move.setIllegalMove(move.getFromRegion().getId() + " transfer only has 1 army");
                            }
                            else //attack
                            {
                                if (gui != null) {
                                    gui.attack(move);
                                }
                                doAttack(move);
                                usedRegions.get(fromRegion.getId()).add(toRegion.getId());
                            }
                        }
                        else
                            move.setIllegalMove(move.getFromRegion().getId() +
                                    " attack/transfer has used all available armies");
                    }
                    else
                        move.setIllegalMove(move.getFromRegion().getId() +
                                " attack/transfer has already attacked/transfered to this region");
                }
                else
                    move.setIllegalMove(move.getFromRegion().getId() + " attack/transfer was taken this round");
            }

            visibleRegionsPlayer1Map = map.visibleRegionsForPlayer(player1);
            visibleRegionsPlayer2Map = map.visibleRegionsForPlayer(player2);
            
            if(visibleRegionsPlayer1Map.contains(move.getFromRegion()) || visibleRegionsPlayer1Map.contains(move.getToRegion()) ||
                    visibleRegionsPlayer1OldMap.contains(move.getToRegion()))
            {
                if(move.getPlayerName().equals(player2.getId()))
                    opponentMovesPlayer1.add(move); //for the opponent_moves output
            }
            if(visibleRegionsPlayer2Map.contains(move.getFromRegion()) || visibleRegionsPlayer2Map.contains(move.getToRegion()) ||
                    visibleRegionsPlayer2OldMap.contains(move.getToRegion()))
            {
                if(move.getPlayerName().equals(player1.getId()))
                    opponentMovesPlayer2.add(move); //for the opponent_moves output
            }
            
            visibleRegionsPlayer1OldMap = visibleRegionsPlayer1Map;
            visibleRegionsPlayer2OldMap = visibleRegionsPlayer2Map;

            //set some stuff to know what next move to get
            if(move.getIllegalMove().equals("")) {
                previousMoveWasIllegal = false;
                moveNr++;
            }
            else {
                previousMoveWasIllegal = true;
            }
            previousMovePlayer = move.getPlayerName();
            
        }
        moveQueue.clear();
    }
}
