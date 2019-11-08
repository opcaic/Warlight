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


import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import conquest.engine.io.BotStreamReader;
import conquest.game.Phase;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;

public class BotParser extends Thread {
	
	final BotStreamReader input;	

	final PrintStream output;
	
	final Bot bot;	
	
	BotState currentState;

	FileBotLog log;
	
	public BotParser(Bot bot) {
		this(bot, System.in, System.out);
	}
	
	public FileBotLog setLogFile(File file) {
		log = new FileBotLog(file);
		log.start();
		log("Logging started...");
		return log;
	}
	
	public BotParser(Bot bot, InputStream input, PrintStream output)
	{
		super("BotParser[" + bot.getClass().getName() + "]");
		this.input = new BotStreamReader(input);
		this.output = output;
		
		this.bot = bot;
		this.currentState = new BotState();
	}

	public static Bot constructBot(String botFQCN, String pathToJar)
	{
		Class botClass = null;
		try {
			File jarDirectory = new File(pathToJar);
			for (File file : jarDirectory.listFiles())
			{
				URL[] urls = {file.toURI().toURL()};
				URLClassLoader cl = URLClassLoader.newInstance(urls, BotParser.class.getClassLoader());
				botClass = Class.forName(botFQCN, true, cl);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(botFQCN);
			throw new RuntimeException("Failed to locate bot class: " + botFQCN, e);
		}
		return constructBot(botClass);
	}

	public static Bot constructBot(String botFQCN) {
		Class botClass;
		try {
			botClass = Class.forName(botFQCN);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to locate bot class: " + botFQCN, e);
		}
		return constructBot(botClass);
	}
	
	public static Bot constructBot(Class botClass) {		
		Object botObj;
		try {
			botObj = botClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to construct Bot instance, tried to invoke parameterless constructor from class: " + botClass.getName()); 
		}
		if (!(Bot.class.isAssignableFrom(botObj.getClass()))) {
			throw new RuntimeException("Constructed bot does not implement " + Bot.class.getName() + " interface, bot class instantiated: " + botClass.getName());
		}
		Bot bot = (Bot) botObj;
		return bot;
	}
	
	public static BotParser runInternal(String botFQCN, InputStream input, PrintStream output) {
		Bot bot = constructBot(botFQCN);
		return runInternal(bot, input, output);
	}

	public static BotParser runInternal(String botFQCN, String jarPath, InputStream input, PrintStream output) {
		Bot bot = constructBot(botFQCN, jarPath);
		return runInternal(bot, input, output);
	}
	
	public static BotParser runInternal(Bot bot, InputStream input, PrintStream output) {
		BotParser parser = new BotParser(bot, input, output);
		parser.start();
		return parser;
	}
	
	private void log(String msg) {
		if (log != null) {
			log.log(msg);
		}
	}
	
	public Bot getBot() {
		return bot;
	}

	@Override
	public void run()
	{
		log("Bot thread started.");
		while (true) {
			String line;
			log("Reading input...");
			try {
				line = input.readLine();
			} catch (IOException e) {
				log("FAILED TO READ NEXT LINE: " + e.getMessage());
				if (log != null) {
					log.finish();
				}
				throw new RuntimeException("Failed to read next line.", e);
			}
			if (line == null) {
				log("End of INPUT stream reached...");
				log("Terminating the thread.");
				if (log != null) {
					log.finish();
				}
				return;
			}
			line = line.trim();
			if(line.length() == 0) { continue; }
			log("IN : " + line);
			String[] parts = line.split(" ");
			if(parts[0].equals("pick_starting_region")) {
				//pick a region you want to start with
				currentState.setPhase(Phase.STARTING_REGIONS);
				currentState.setPickableStartingRegions(parts);
				Region startingRegion = bot.getStartingRegion(currentState, Long.valueOf(parts[1]));
				String output = startingRegion.id + "";
				
				log("OUT: " + output);
				this.output.println(output);
			} else if(parts.length == 3 && parts[0].equals("go")) {
				//we need to do a move
				String output = "";
				if(parts[1].equals("place_armies")) 
				{
					currentState.setPhase(Phase.PLACE_ARMIES);
					ArrayList<PlaceArmiesMove> placeArmiesMoves = bot.getPlaceArmiesMoves(currentState, Long.valueOf(parts[2]));
					for(PlaceArmiesMove move : placeArmiesMoves)
						output = output.concat(move.getString() + ",");
				} 
				else if(parts[1].equals("attack/transfer")) 
				{
					currentState.setPhase(Phase.ATTACK_TRANSFER);
					ArrayList<AttackTransferMove> attackTransferMoves = bot.getAttackTransferMoves(currentState, Long.valueOf(parts[2]));
					for(AttackTransferMove move : attackTransferMoves)
						output = output.concat(move.getString() + ",");
				}
				if(output.length() == 0) output = "No moves";
				log("OUT: " + output);
				this.output.println(output);
			} else if(parts.length == 3 && parts[0].equals("settings")) {
				//update settings
				currentState.updateSettings(parts[1], parts[2]);
			} else if(parts[0].equals("setup_map")) {
				//initial full map is given
				currentState.setupMap(parts);
			} else if(parts[0].equals("update_map")) {
				//all visible regions are given
				currentState.updateMap(parts);
			} else if(parts[0].equals("opponent_moves")) {
				// TODO: finish implementation
			} else if (parts[0].equals("next_round"))
			    currentState.nextRound();
			else {
				log("Unable to parse line: " + line);
			}
		}
		// COULD NOT REACH HERE...
	}

}
