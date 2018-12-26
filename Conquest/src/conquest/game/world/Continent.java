package conquest.game.world;

import java.util.*;

public enum Continent {
	
	North_America("North America", 1, 5),
	South_America("South America", 2, 2),
	Europe("Europe", 3, 5),
	Africa("Africa", 4, 3),	
	Asia("Asia", 5, 7),
	Australia("Australia", 6, 2);
	
	public static final int LAST_ID = 6;
	
	/**
	 * Must be 1-based!
	 */
	public final int id;
	public final int reward;
	public final int continentFlag;
	public final String mapName;
	
	private List<Region> regions = null;
	
	private Continent(String mapName, int id, int reward) {
		this.mapName = mapName;
		this.id = id;
		this.reward = reward;	
		this.continentFlag = 1 << (id-1);
	}
	
	public List<Region> getRegions() {
		if (regions == null) {
			synchronized(this) {
				if (regions == null) {
					List<Region> regions = new ArrayList<Region>();
					for (Region regionName : Region.values()) {
						if (regionName.continent == this) {
							regions.add(regionName);
						}
					}
					this.regions = regions;
				}
			}
		}
		return regions;
	}
	
	private static Map<Integer, Continent> id2Continent = null;
	
	public static Continent forId(int id) {
		if (id2Continent == null) {
			id2Continent = new HashMap<Integer, Continent>();
			for (Continent continent : Continent.values()) {
				id2Continent.put(continent.id, continent);
			}
		}
		return id2Continent.get(id);
	}
	
	private static Map<Integer, Continent> flagToContinent = null;
	
	public static Continent fromFlag(int continentFlag) {
		if (flagToContinent == null) {
			flagToContinent = new HashMap<Integer, Continent>();
			for (Continent continent : Continent.values()) {
				flagToContinent.put(continent.continentFlag, continent);
			}
		}
		return flagToContinent.get(continentFlag);
	}

}
