package conquest.view;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.Team;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.view.TriButton.ClickListener;

public class GUI extends JFrame implements MouseListener, KeyListener
{
	private static final long serialVersionUID = 2116436198852146401L;
	private static final String RESOURCE_IMAGE_FILE = "resources/images/conquest-map.png";
	private static final int WIDTH = 1239;
	private static final int HEIGHT = 664;
	
	public static int[][] positionsAIvsAI = new int[][]{
		{95, 150},  //1.  Alaska
		{209, 160}, //2.  Northwest Territory
		{441, 96},  //3.  Greenland
		{190, 205}, //4.  Alberta
		{257, 209}, //5.  Ontario
		{355, 203}, //6.  Quebec
		{224, 263}, //7.  Western United States
		{295,277},  //8.  Eastern United States
		{255,333},  //9.  Central America
		{350,373},  //10. Venezuela
		{344,445},  //11. Peru
		{415,434},  //12. Brazil
		{374,511},  //13. Argentina
		{514,158},  //14. Iceland
		{545,200},  //15. Great Britain
		{627,160},  //16. Scandinavia
		{699,205},  //17. Ukraine
		{556,266},  //18. Western Europe
		{618, 218}, //19. Northern Europe
		{650, 255}, //20. Southern Europe
		{576,339},  //21. North Africa
		{647,316},  //22. Egypt
		{698,379},  //23. East Africa
		{654,408},  //24. Congo
		{657,478},  //25. South Africa
		{726,465},  //26. Madagascar
		{800,178},  //27. Ural
		{890,146},  //28. Siberia
		{972,150},  //29. Yakutsk
		{1080,150}, //30. Kamchatka
		{942,205},  //31. Irkutsk
		{798,242},  //32. Kazakhstan
		{895,279},  //33. China
		{965,242},  //34. Mongolia
		{1030,279}, //35. Japan
		{716,295},  //36. Middle East
		{835,316},  //37. India
		{908,348},  //38. Siam
		{930,412},  //39. Indonesia
		{1035,422}, //40. New Guinea
		{983,484},  //41. Western Australia
		{1055,500}, //42. Eastern Australia
	};
	
	//Position of each region's info (team color & number of armies)
	public static final int[][] positionsHuman = new int[][]{
				{95, 150},  //1.  Alaska
				{209, 143}, //2.  Northwest Territory
				{441, 96},  //3.  Greenland
				{190, 202}, //4.  Alberta
				{257, 203}, //5.  Ontario
				{355, 203}, //6.  Quebec
				{214, 263}, //7.  Western United States
				{295,273},  //8.  Eastern United States
				{255,333},  //9.  Central America
				{350,373},  //10. Venezuela
				{344,445},  //11. Peru
				{425,434},  //12. Brazil
				{374,511},  //13. Argentina
				{520,135},  //14. Iceland
				{538,195},  //15. Great Britain
				{627,130},  //16. Scandinavia
				{719,195},  //17. Ukraine
				{560,255},  //18. Western Europe
				{628,196},  //19. Northern Europe
				{650,255},  //20. Southern Europe
				{576,339},  //21. North Africa
				{654,316},  //22. Egypt
				{740,375},  //23. East Africa
				{654,408},  //24. Congo
				{657,478},  //25. South Africa
				{736,465},  //26. Madagascar
				{800,178},  //27. Ural
				{865,116},  //28. Siberia
				{972,110},  //29. Yakutsk
				{1080,150}, //30. Kamchatka
				{942,175},  //31. Irkutsk
				{798,242},  //32. Kazakhstan
				{895,279},  //33. China
				{975,250},  //34. Mongolia
				{1057,279}, //35. Japan
				{730,295},  //36. Middle East
				{815,316},  //37. India
				{908,348},  //38. Siam
				{930,412},  //39. Indonesia
				{1035,422}, //40. New Guinea
				{971,484},  //41. Western Australia
				{1055,500}, //42. Eastern Australia
			};
	
	public static int[][] positions = positionsAIvsAI;
	
	private GUINotif notification;
	
	private JLabel roundNumTxt;
	private JLabel actionTxt;
	
	private RegionInfo[] regions;
	private boolean clicked = false;
	private boolean rightClick = false;
	private boolean nextRound = false;
	private boolean continual = false;
	private int continualTime = 1000;
	
	private String playerName1, botName1;
	private String playerName2, botName2;
	
	private RegionInfo p1;
	private RegionInfo p2;
	
	private Arrow mainArrow;
	
	private JLayeredPane mainLayer;
	
	public boolean showIds = false;
	
	public GUI(String playerEngineName1, String playerEngineName2, String playerName1, String playerName2)
	{
		System.out.println("GUI: Click to advance to next round.");
		System.out.println("GUI: Hold right mouse button to QUICKLY advance through many rounds.");
		
		this.playerName1 = playerEngineName1;
		this.playerName2 = playerEngineName2;
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Warlight");
		this.addMouseListener(this);
		this.addKeyListener(this);
		
		this.setLayout(null);
		
        mainLayer = new JLayeredPane();
        mainLayer.setBounds(0, 0, WIDTH, HEIGHT);
        mainLayer.setSize(WIDTH, HEIGHT);
        mainLayer.setPreferredSize(mainLayer.getSize());
        mainLayer.setLocation(0, -19);
        this.add(mainLayer);

        //Map image
		JLabel labelForImage = new JLabel();
		labelForImage.setBounds(0, 0, WIDTH, HEIGHT);
		URL iconURL = this.getClass().getResource(RESOURCE_IMAGE_FILE);
		ImageIcon icon = new ImageIcon(iconURL);
		labelForImage.setIcon(icon);
		mainLayer.add(labelForImage, JLayeredPane.DEFAULT_LAYER);

		int boxWidth = 300;
		
		//Current round number
		roundNumTxt = new JLabel("Round: --", JLabel.CENTER);
		roundNumTxt.setBounds(WIDTH / 2 - boxWidth / 2, 20, boxWidth, 15);
		roundNumTxt.setBackground(Color.gray);
		roundNumTxt.setOpaque(true);
		roundNumTxt.setForeground(Color.WHITE);
		mainLayer.add(roundNumTxt, JLayeredPane.DRAG_LAYER);
		
		actionTxt = new JLabel("ACTION", JLabel.CENTER);
		actionTxt.setBounds(WIDTH / 2 - boxWidth / 2, 35, boxWidth, 15);
		actionTxt.setBackground(Color.gray);
		actionTxt.setOpaque(true);
		actionTxt.setForeground(Color.WHITE);
			actionTxt.setPreferredSize(actionTxt.getSize());
		mainLayer.add(actionTxt, JLayeredPane.DRAG_LAYER);
				
		this.regions = new RegionInfo[42];
		
		for (int idx = 0; idx < 42; idx++) {
			this.regions[idx] = new RegionInfo(this);
			this.regions[idx].setLocation(positions[idx][0] - 50, positions[idx][1]);
			this.regions[idx].setRegion(Region.forId(idx+1));			
			mainLayer.add(this.regions[idx], JLayeredPane.PALETTE_LAYER);
		}
		
		//Legend
		p1 = new RegionInfo(this);
		p1.setLocation(45,50);
		p1.setTeam(Team.PLAYER_1);
		p1.setNameLabel(playerName1);
		p1.setText(playerEngineName1);
		mainLayer.add(p1, JLayeredPane.PALETTE_LAYER);
		
		p2 = new RegionInfo(this);
		p2.setLocation(45,85);
		p2.setTeam(Team.PLAYER_2);
		p2.setNameLabel(playerName2);
		p2.setText(playerEngineName2);
		mainLayer.add(p2, JLayeredPane.PALETTE_LAYER);
		
		notification = new GUINotif(mainLayer, 1015, 45, 200, 50);		
		
		mainArrow = new Arrow(0, 0, WIDTH, HEIGHT);
		mainLayer.add(mainArrow, JLayeredPane.PALETTE_LAYER);
		
		//Finish
        this.pack();
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
                
	}
	
	RegionInfo regionInfo(Region region) {
		return regions[region.id - 1];
	}
	
	public Team getTeam(String player) {
		if (player.equals(playerName1)) return Team.PLAYER_1;
		if (player.equals(playerName2)) return Team.PLAYER_2;
		return Team.NEUTRAL;
	}
	
	public void setContinual(boolean state) {
		continual = state;
	}
	
	public void setContinualFrameTime(int millis) {
		continualTime = millis;
	}
	
	// ==============
	// MOUSE LISTENER
	// ==============
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (moving != null) {
				moveFrom = null;
				highlight();
			} else clicked = true;
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			rightClick = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			rightClick = false;
		}
	}
	
	private void waitForClick() {
		long time = System.currentTimeMillis() + continualTime;
		clicked = false;
		
		while(!clicked && !rightClick && !nextRound) { //wait for click, or skip if right button down
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (continual && time < System.currentTimeMillis()) break; // skip if continual action and time out
		}
	}
	
	// ============
	// KEY LISTENER
	// ============
	
	@Override
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		c = Character.toLowerCase(c);
		switch(c) {
		case 'n':
			nextRound = true;
			showNotification("SKIP TO NEXT ROUND");
			break;
		case 'c':
			continual = !continual;
			showNotification( continual ? "Continual run enabled" : "Continual run disabled");
			break;
		case 'i':
			showIds = !showIds;
			for (RegionInfo i : regions)
				i.drawName();
			break;
		case ' ':
			clicked = true;
			break;
		case '+':
			continualTime += 100;
			continualTime = Math.min(continualTime, 3000);
			showNotification("Action visualized for: " + continualTime + " ms");
			break;
		case '-':
			continualTime -= 100;
			continualTime = Math.max(continualTime, 200);
			showNotification("Action visualized for: " + continualTime + " ms");
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	// =====
	// NOTIF
	// =====
	
	public void showNotification(String txt) {
		notification.show(txt, 1500);
	}
	
	// =============
	// ENGINE EVENTS
	// =============
	
	private void updateStats() {
		
		int plr1Regions = 0;
		int plr2Regions = 0;
		
		int plr1Armies = 0;
		int plr2Armies = 0;
		
		int plr1Income = 5;
		int plr2Income = 5;
		
		for (RegionInfo region : this.regions) {
			switch(region.getTeam()) {
			case PLAYER_1:
				++plr1Regions;
				plr1Armies += region.getArmies();
				break;
			case PLAYER_2:
				++plr2Regions;
				plr2Armies += region.getArmies();
				break;
			}
		}
		
		for (Continent continent : Continent.values()) {
			boolean plr1 = true;
			boolean plr2 = true;
			for (Region region : continent.getRegions()) {
				RegionInfo info = regions[region.id-1];
				if (info.getTeam() == Team.PLAYER_1) plr2 = false;
				if (info.getTeam() == Team.PLAYER_2) plr1 = false;
				if (info.getTeam() == Team.NEUTRAL) {
					plr1 = false;
					plr2 = false;
				}
				if (!plr1 && !plr2) break;
			}
			if (plr1) plr1Income += continent.reward;
			if (plr2) plr2Income += continent.reward;
		}
		
		p1.setText("[" + plr1Regions + " / " + plr1Armies + " / +" + plr1Income + "]");
		p2.setText("[" + plr2Regions + " / " + plr2Armies + " / +" + plr2Income + "]");
	}
	
	public void newRound(int roundNum) {
		roundNumTxt.setText("Round: " + Integer.toString(roundNum));
		actionTxt.setText("NEW ROUND");
		nextRound = false;

		//Wait for user to request next round
		waitForClick();		
	}
	
	public void updateAfterRound(GameMap map) { //called by Engine.playRound()
		this.requestFocusInWindow();
		
		//Update regions info
		for(RegionData region : map.regions) {
			int id = region.getId();
			this.regions[id-1].setArmies(region.getArmies());
			this.regions[id-1].setText(Integer.toString(region.getArmies()));			
			this.regions[id-1].setTeam(getTeam(region.getPlayerName()));
		}

		updateStats();
	}

	List<RegionData> pickableRegions = null;
	
	public void pickableRegions(List<RegionData> pickableRegions) {
		this.requestFocusInWindow();
		
		actionTxt.setText("PICKABLE REGIONS");
		
		for (RegionData regionData : pickableRegions) {
			int id = regionData.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight(RegionInfo.Green);
		}
		
		this.pickableRegions = pickableRegions;
		
		waitForClick();
		
		for (RegionData regionData : pickableRegions) {
			int id = regionData.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight(false);
		}
	}
	
	public void updateRegions(List<RegionData> regions) {
		this.requestFocusInWindow();
		
		for (RegionData data : regions) {
			int id = data.getId();
			RegionInfo region = this.regions[id-1];
			region.setTeam(getTeam(data.getPlayerName()));
			region.setArmies(data.getArmies());
			region.setText("" + region.getArmies());
		}
	}
	
	public void regionsChosen(List<RegionData> regions) {
		this.requestFocusInWindow();
		
		actionTxt.setText("CHOSEN REGIONS");
		
		updateRegions(regions);
		
		for (RegionData data : regions) {
			int id = data.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight(region.getTeam() != Team.NEUTRAL);
		}

		waitForClick();
		
		for (RegionData regionData : regions) {
			int id = regionData.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight(false);
		}
		
		updateStats();
	}
	
	public void placeArmies(LinkedList<RegionData> regions, List<PlaceArmiesMove> placeArmiesMoves) {
		this.requestFocusInWindow();
		
		actionTxt.setText("ARMIES PLACED");
		
		updateRegions(regions);
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];	
			region.setArmies(region.getArmies() - move.getArmies());
			region.armiesPlus += move.getArmies();
			region.setText(region.getArmies() + "+" + region.armiesPlus);
			region.setHighlight(true);
		}
		
		waitForClick();
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];
			region.setArmies(region.getArmies() + region.armiesPlus);
			region.armiesPlus = 0;
			region.setText("" + region.getArmies());
			region.setHighlight(false);
		}
		
		actionTxt.setText("---");
		
		updateStats();
	}	

	public void transfer(AttackTransferMove move) {
		this.requestFocusInWindow();
		
		String toName = move.getToRegion().getRegion().mapName;
		actionTxt.setText(botName(move.getPlayerName()) + " transfers to " + toName);
		Team player = getTeam(move.getPlayerName());
		
		RegionInfo fromRegion = this.regions[move.getFromRegion().getId() - 1];
		RegionInfo toRegion = this.regions[move.getToRegion().getId() - 1];
		int armies = move.getArmies();
		
		fromRegion.armiesPlus = -armies;
		fromRegion.setHighlight(true);
		
		toRegion.armiesPlus = armies;
		toRegion.setHighlight(true);
		
		int[] fromPos = positions[move.getFromRegion().getId() - 1];
		int[] toPos = positions[move.getToRegion().getId() - 1];
		mainArrow.setFromTo(fromPos[0], fromPos[1] + 20, toPos[0], toPos[1] + 20);
		mainArrow.setColor(TeamView.getColor(player));
		mainArrow.setNumber(armies);
		mainArrow.setVisible(true);
		
		waitForClick();
		
		fromRegion.setHighlight(false);
		fromRegion.setArmies(fromRegion.getArmies() + fromRegion.armiesPlus);
		fromRegion.setText(String.valueOf(fromRegion.getArmies()));
		fromRegion.armiesPlus = 0;
		
		toRegion.setHighlight(false);
		toRegion.setArmies(toRegion.getArmies() + toRegion.armiesPlus);
		toRegion.setText(String.valueOf(toRegion.getArmies()));
		toRegion.armiesPlus = 0;
		
		mainArrow.setVisible(false);
		
		actionTxt.setText("---");
	}

	String botName(String playerName) {
		if (playerName.equals(playerName1))
			return botName1;
		if (playerName.equals(playerName2))
			return botName2;
		throw new Error("unknown name");
	}
	
	void showArrow(Arrow arrow, int fromRegionId, int toRegionId, Team team, int armies) {
		int[] fromPos = positions[fromRegionId - 1];
		int[] toPos = positions[toRegionId - 1];
		arrow.setFromTo(fromPos[0], fromPos[1] + 20, toPos[0], toPos[1] + 20);
		arrow.setColor(TeamView.getColor(team));
		arrow.setNumber(armies);
		arrow.setVisible(true);
	}
	
	public void attack(AttackTransferMove move) {
		this.requestFocusInWindow();
		
		String toName = move.getToRegion().getRegion().mapName;
		actionTxt.setText(botName(move.getPlayerName()) + " attacks " + toName);
		
		Team attacker = getTeam(move.getPlayerName());
		RegionInfo fromRegion = this.regions[move.getFromRegion().getId() - 1];
		RegionInfo toRegion = this.regions[move.getToRegion().getId() - 1];
		int armies = move.getArmies();
		
		fromRegion.armiesPlus = -armies;
		fromRegion.setHighlight(true);
		
		toRegion.armiesPlus = armies;
		toRegion.setHighlight(true);
		
		showArrow(mainArrow, move.getFromRegion().getId(), move.getToRegion().getId(), attacker, armies);
		
		waitForClick();		
	}

	static Color withSaturation(Color c, float sat) {
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		return new Color(Color.HSBtoRGB(hsb[0], sat, hsb[2]));
	}
	
	public void attackResult(RegionData fromRegionData, RegionData toRegionData, int attackersDestroyed, int defendersDestroyed) {
		this.requestFocusInWindow();
		
		RegionInfo fromRegion = this.regions[fromRegionData.getId() - 1];
		RegionInfo toRegion = this.regions[toRegionData.getId() - 1];
		Team attacker = getTeam(fromRegionData.getPlayerName());
		
		boolean success;
		
		if (fromRegionData.getPlayerName().equals(toRegionData.getPlayerName())) {
			success = true;
			actionTxt.setText("SUCCESS [A:" + (attackersDestroyed > 0 ? "-" : "") + attackersDestroyed + " | D:" + (defendersDestroyed > 0 ? "-" : "") + defendersDestroyed + "]");
			fromRegion.setArmies(fromRegion.getArmies() + fromRegion.armiesPlus);
			toRegion.setTeam(getTeam(toRegionData.getPlayerName()));
			toRegion.setArmies((-fromRegion.armiesPlus) - attackersDestroyed);
		} else {
			success = false;
			actionTxt.setText("FAILURE [A:" + (attackersDestroyed > 0 ? "-" : "") + attackersDestroyed + " | D:" + (defendersDestroyed > 0 ? "-" : "") + defendersDestroyed + "]");
			fromRegion.setArmies(fromRegion.getArmies() - attackersDestroyed);
			toRegion.setArmies(toRegion.getArmies() - defendersDestroyed);
		}
					
		
		fromRegion.armiesPlus = 0;
		fromRegion.setText("" + fromRegion.getArmies());
		
		toRegion.armiesPlus = 0;
		toRegion.setText("" + toRegion.getArmies());
		
		fromRegion.setHighlight(true);
		toRegion.setHighlight(true);
		Color c = TeamView.getColor(attacker);
		mainArrow.setColor(withSaturation(c, success ? 0.5f : 0.2f));
		mainArrow.setNumber(0);
		
		waitForClick();		
		
		fromRegion.setHighlight(false);
		toRegion.setHighlight(false);
		mainArrow.setVisible(false);
		
		actionTxt.setText("---");	
		
		updateStats();
	}
	
	// --------------
	// ==============
	// HUMAN CONTROLS
	// ==============
	// --------------
	
	// ======================
	// CHOOSE INITIAL REGIONS
	// ======================
	
	private CountDownLatch chooseRegionsAction;
	
	private List<Region> chosenRegions;
	
	private List<Region> availableRegions;
	
	private List<Button> regionButtons;
	
	private Button finishedButton;
	
	private String chooseRegionsPlayerName;
	
	Button doneButton() {
		Button b = new Button("DONE");
		b.setForeground(Color.WHITE);
		b.setBackground(Color.BLACK);
		b.setSize(60, 30);
		b.setLocation(WIDTH / 2 - 30, HEIGHT - 100);
		return b;
	}
	
	public List<Region> chooseRegionsHuman(String playerName, List<Region> availableRegions) {
		this.requestFocusInWindow();
		
		this.chooseRegionsPlayerName = playerName;
		chooseRegionsAction = new CountDownLatch(1);
		
		this.availableRegions = availableRegions;
		this.chosenRegions = new ArrayList<Region>();
		
		actionTxt.setText(botName(chooseRegionsPlayerName) + ": choose " + (6-chosenRegions.size()) + " regions");
		
		regionButtons = new ArrayList<Button>();
		
		for (Region region : availableRegions) {
			Button button = new Button("+");
			button.setForeground(Color.WHITE);
			button.setBackground(Color.BLACK);
			button.setSize(30, 20);
			int[] regionPos = positions[region.id-1];
			button.setLocation(regionPos[0] - 15, regionPos[1] + 40);
			regionButtons.add(button);
			
			mainLayer.add(button, JLayeredPane.MODAL_LAYER);
			
			final Region targetRegion = region;
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					regionChosen(targetRegion);
					GUI.this.requestFocusInWindow();
				}
			});
		}
		
		finishedButton = doneButton();
		finishedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseRegionsAction.countDown();
				GUI.this.requestFocusInWindow();
			}
		});
		finishedButton.setVisible(false);
		mainLayer.add(finishedButton, JLayeredPane.MODAL_LAYER);
		
		try {
			chooseRegionsAction.await();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while awaiting user action.");
		}
		
		for (Button button : regionButtons) {
			mainLayer.remove(button);			
		}
		mainLayer.remove(finishedButton);
		mainLayer.repaint();
		
		for (Region region : availableRegions) {
			if (chosenRegions.size() == 6) break;
			if (chosenRegions.contains(region)) continue;
			chosenRegions.add(region);
		}
		
		return chosenRegions;
	}
	
	private void regionChosen(Region region) {
		if (chosenRegions.contains(region)) {
			chosenRegions.remove(region);
			renumberButtons();
			finishedButton.setVisible(chosenRegions.size() == 6);
			return;
		}
		
		if (chosenRegions.size() == 6) return;

		chosenRegions.add(region);
		renumberButtons();
		
		finishedButton.setVisible(chosenRegions.size() == 6);
	}
	
	private void renumberButtons() {
		int n = 6 - chosenRegions.size();
		actionTxt.setText(botName(chooseRegionsPlayerName) + ": choose " +
	                     n + " region" + (n == 1 ? "" : "s"));
		for (int i = 0; i < regionButtons.size(); ++i) {
			Button button = regionButtons.get(i);
			if (chosenRegions.contains(availableRegions.get(i))) {
				int index = chosenRegions.indexOf(availableRegions.get(i));				
				button.setLabel(String.valueOf(index+1) + ".");
			} else {
				button.setLabel("+");
			}
		}
	}
	
	// ============
	// PLACE ARMIES
	// ============
	
	private CountDownLatch placeArmiesAction;
	
	private String placeArmiesPlayerName;
	
	private int armiesLeft;
	
	private List<Region> armyRegions;
	
	private List<TriButton> armyRegionButtons;
	
	private Button placeArmiesFinishedButton;
		
	public List<PlaceArmiesMove> placeArmiesHuman(String playerName, Team team, int startingArmies) {
		this.requestFocusInWindow();
		
		List<Region> availableRegions = new ArrayList<Region>();
		for (int i = 0; i < regions.length; ++i) {
			RegionInfo info = regions[i];
			if (info.getTeam() == team) {
				availableRegions.add(Region.values()[i]);
			}			
		}
		return placeArmiesHuman(playerName, startingArmies, availableRegions);
	}
	
	public List<PlaceArmiesMove> placeArmiesHuman(String playerName, int totalArmies, List<Region> availableRegions) {
		this.armyRegions = availableRegions;
		this.placeArmiesPlayerName = playerName;
		
		placeArmiesAction = new CountDownLatch(1);
		
		actionTxt.setText(botName(placeArmiesPlayerName) + ": place " + totalArmies +
				          (totalArmies == 1 ? "army" : " armies"));
		
		this.armiesLeft = totalArmies;		
		
		armyRegionButtons = new ArrayList<TriButton>();
		
		int ch1 = 1;
		int ch2 = 4;
		int ch3 = 10;
		
		for (Region region : armyRegions) {
			int[] regionPos = positions[region.id-1];
			
			TriButton button = new TriButton(mainLayer, regionPos[0], regionPos[1] + 30, ch1, ch2, ch3);
			
			armyRegionButtons.add(button);
						
			final Region targetRegion = region;
			
			button.clickListener = new ClickListener() {
				@Override
				public void clicked(int change) {
					placeArmyRegionClicked(targetRegion, change);
					GUI.this.requestFocusInWindow();
				}
			};
		}
		
		if (placeArmiesFinishedButton == null) {
			placeArmiesFinishedButton = doneButton();
			placeArmiesFinishedButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (armiesLeft == 0) {
						placeArmiesAction.countDown();
					}
					GUI.this.requestFocusInWindow();
				}
			});
		}
		mainLayer.add(placeArmiesFinishedButton, JLayeredPane.MODAL_LAYER);
		placeArmiesFinishedButton.setVisible(false);
		repaint();
		
		try {
			placeArmiesAction.await();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while awaiting user action.");
		}
		
		for (TriButton button : armyRegionButtons) {
			button.dispose();
		}
		mainLayer.remove(placeArmiesFinishedButton);
		
		List<PlaceArmiesMove> result = new ArrayList<PlaceArmiesMove>();
		
		for (Region region : availableRegions) {
			RegionInfo info = regions[region.id-1];
			if (info.armiesPlus > 0) {
				info.setArmies(info.getArmies() + info.armiesPlus);
				info.setText("" + info.getArmies());
				info.setHighlight(false);

				PlaceArmiesMove command = new PlaceArmiesMove(playerName, new RegionData(region, region.id, null), info.armiesPlus);
				info.armiesPlus = 0;
				
				result.add(command);
			}
		}
		
		return result;
	}
	
	private void placeArmyRegionClicked(Region region, int change) {		
		change = Math.min(armiesLeft, change);
		if (change == 0) return;
		
		RegionInfo info = regions[region.id-1];
		
		if (change < 0) {
			change = -Math.min(Math.abs(change), info.armiesPlus);
		}
		if (change == 0) return;
		
		info.armiesPlus += change;
		armiesLeft -= change;
		
		if (info.armiesPlus > 0) {
			info.setText(info.getArmies() + "+" + info.armiesPlus);
			info.setHighlight(true);
		} else {
			info.setText(String.valueOf(info.getArmies()));
			info.setHighlight(false);
		}
		
		actionTxt.setText(botName(placeArmiesPlayerName) + ": place " + armiesLeft +
				(armiesLeft == 1 ? " army" : " armies"));
		
		placeArmiesFinishedButton.setVisible(armiesLeft == 0);
	}

	// ===========
	// MOVE ARMIES
	// ===========
	
	class Move {
		Region from;
		Region to;
		int armies;
		Arrow arrow;
		
		Move(Region from, Region to, int armies, Arrow arrow) {
			this.from = from; this.to = to; this.armies = armies; this.arrow = arrow;
		}
	}
	
	private Team moving = null;
	private Map<Integer, Move> moves;  // maps encoded (fromId, toId) to Move
	private Region moveFrom;	
	
	private CountDownLatch moveArmiesAction;
	private Button moveArmiesFinishedButton;
		
	static int encode(int fromId, int toId) {
		return fromId * (Region.LAST_ID + 1) + toId;
	}
	
	int totalFrom(Region r) {
		int sum = 0;
		
		for (Move m : moves.values())
			if (m.from == r)
				sum += m.armies;
		
		return sum;
	}
	
	void move(Region from, Region to, int delta) {
		int e = encode(to.id, from.id);
		Move m = moves.get(e);
		if (m != null) { // move already exists in the opposite direction
			 move(to, from, - delta);
			 return;
		}
		
		if (totalFrom(from) + delta >= regionInfo(from).getArmies())
			return;		// no available armies
		
		e = encode(from.id, to.id);
		m = moves.get(e);
		if (m == null && delta > 0) {
			Arrow arrow = new Arrow(0, 0, WIDTH, HEIGHT);
			showArrow(arrow, from.id, to.id, moving, delta);
			mainLayer.add(arrow, JLayeredPane.PALETTE_LAYER);
			moves.put(e, new Move(from, to, delta, arrow));
		} else if (m != null) {
			m.armies += delta;
			if (m.armies > 0)
				m.arrow.setNumber(m.armies);
			else {
				mainLayer.remove(m.arrow);
				moves.remove(e);
				repaint();
			}
		}
		
	}
	
	void highlight() {
		if (moveFrom == null)
			for (RegionInfo ri : regions)
				ri.setHighlight(ri.getTeam() == moving);
		else {
			for (RegionInfo ri : regions)
				ri.setHighlight(ri.getRegion() == moveFrom ? RegionInfo.Green : null);
			
			for (Region n : moveFrom.getNeighbours())
				regionInfo(n).setHighlight(RegionInfo.Gray);
		}
	}
	
	boolean isNeighbor(Region r, Region s) {
		return r.getNeighbours().contains(s);
	}
	
	void regionClicked(RegionInfo ri, boolean left) {
		if (moving == null) {
			clicked = true;
			return;
		}
		
		Region r = ri.getRegion();
		
		if (moveFrom != null && isNeighbor(moveFrom, r)) {
			move(moveFrom, r, left ? 1 : -1);
			return;
		}
		if (!left)
			return;
		
		moveFrom = (ri.getTeam() == moving) ? r : null;
		highlight();
	}

	public List<AttackTransferMove> moveArmiesHuman(String playerName, Team team) {
		this.requestFocusInWindow();
		moving = team;
		moveFrom = null;
		
		actionTxt.setText(botName(playerName) + ": move armies");
			
		moveArmiesAction = new CountDownLatch(1);
		
		moves = new HashMap<Integer, Move>();
		
		if (moveArmiesFinishedButton == null) {
			moveArmiesFinishedButton = doneButton();
			moveArmiesFinishedButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					moveArmiesAction.countDown();
					GUI.this.requestFocusInWindow();
				}
			});
		}
		mainLayer.add(moveArmiesFinishedButton, JLayeredPane.MODAL_LAYER);
		highlight();
		
		try {
			moveArmiesAction.await();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while awaiting user action.");
		}
		
		mainLayer.remove(moveArmiesFinishedButton);
		
		for (RegionInfo info : regions)
			info.setHighlight(false);
		
		List<AttackTransferMove> moveArmies = new ArrayList<AttackTransferMove>();
		
		for (Move m : moves.values()) {
			moveArmies.add(new AttackTransferMove(
				playerName,
				new RegionData(m.from, m.from.id, null),
				new RegionData(m.to, m.to.id, null),
				m.armies));
			mainLayer.remove(m.arrow);
		}
		repaint();
		
		moving = null;
		
		return moveArmies;
	}
	
	public void setPlayerNames(String player1Name, String player2Name) {
		botName1 = player1Name;
		p1.setNameLabel(player1Name);
		
		botName2 = player2Name;
		p2.setNameLabel(player2Name);
	}
} 
