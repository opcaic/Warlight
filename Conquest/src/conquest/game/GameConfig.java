package conquest.game;

import conquest.engine.Engine.FightMode;

public class GameConfig {
    public FightMode fight = FightMode.ORIGINAL_A60_D70;
    
    public GameConfig(FightMode fight) { this.fight = fight; }
}
