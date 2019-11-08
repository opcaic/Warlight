package conquest.tournament;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Scanner;

import jdk.nashorn.internal.parser.JSONParser;
import org.json.*;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

import conquest.engine.Config;

public class ConquestFightConsole {
	
	private static final char ARG_SEED_SHORT = 's';
	
	private static final String ARG_SEED_LONG = "seed";
	
	private static final char ARG_GAME_CONFIG_SHORT = 'o';
	
	private static final String ARG_GAME_CONFIG_LONG = "game-config";
	
	private static final char ARG_GAMES_COUNT_SHORT = 'g';
	
	private static final String ARG_GAMES_COUNT_LONG = "games-count";
	
	private static final char ARG_REVERSE_GAMES_SHORT = 'r';
	
	private static final String ARG_REVERSE_GAMES_LONG = "reverse-games";
	
	private static final char ARG_BOT1_NAME_SHORT = 'a';
	
	private static final String ARG_BOT1_NAME_LONG = "bot1-name";
	
	private static final char ARG_BOT1_INIT_SHORT = 'b';
	
	private static final String ARG_BOT1_INIT_LONG = "bot1-init";
	
	private static final char ARG_BOT2_NAME_SHORT = 'c';
	
	private static final String ARG_BOT2_NAME_LONG = "bot2-name";
	
	private static final char ARG_BOT2_INIT_SHORT = 'd';
	
	private static final String ARG_BOT2_INIT_LONG = "bot2-init";
	
	private static final char ARG_BOT_ID_BATCH_SHORT = 'e';
	
	private static final String ARG_BOT_ID_BATCH_LONG = "bot-id-batch";
	
	private static final char ARG_BOTS_BATCH_PROPERTIES_SHORT = 'f';
	
	private static final String ARG_BOTS_BATCH_PROPERTIES_LONG = "bots-property-file-batch";
	
	private static final char ARG_RESULT_DIR_SHORT = 'u';
	
	private static final String ARG_RESULT_DIR_LONG = "result-dir";
	
	private static final char ARG_REPLAY_DIR_SHORT = 'y';
	
	private static final String ARG_REPLAY_DIR_LONG = "replay-dir";
	
	private static final char ARG_TABLE_FILE_SHORT = 't';
	
	private static final String ARG_TABLE_FILE_LONG = "table-file";
	
	private static JSAP jsap;

	private static int seed = 0;

	private static String roundConfig;
	
	private static int gamesCount;
	
	private static boolean reverseGames;
	
	private static String bot1Name;
	
	private static String bot1Init;

	private static String bot1JarPath;
	
	private static String bot2Name;
	
	private static String bot2Init;

	private static String bot2JarPath;
	
	private static String botIdBatch;
	
	private static String botsBatchPropertyFileName;
	
	private static File botsBatchPropertyFile;
		
	private static String resultDir;
	
	private static File resultDirFile;
	
	private static String replayDir;
	
	private static File replayDirFile;

	private static File resultJsonFile;

	private static String tableFileName;
	
	private static File tableFile;
	
	private static boolean batchFight;

	private static boolean headerOutput = false;

	private static JSAPResult config;

	private static void fail(String errorMessage) {
		fail(errorMessage, null);
	}

	private static void fail(String errorMessage, Throwable e) {
		header();
		System.out.println("ERROR: " + errorMessage);
		System.out.println();
		if (e != null) {
			e.printStackTrace();
			System.out.println("");
		}		
        System.out.println("Usage: java -jar conquest-tournament.jar ");
        System.out.println("                " + jsap.getUsage());
        System.out.println();
        System.out.println(jsap.getHelp());
        System.out.println();
        throw new RuntimeException("FAILURE: " + errorMessage);
	}

	private static void header() {
		if (headerOutput) return;
		System.out.println();
		System.out.println("==============");
		System.out.println("Conquest Fight");
		System.out.println("==============");
		System.out.println();
		headerOutput = true;
	}
		
	private static void readConfigFromJson(String jsonFileName) throws Exception
	{
		System.out.println(jsonFileName);
		String content = new Scanner(new File(jsonFileName)).useDelimiter("\\Z").next();
		JSONObject json = new JSONObject(content);
		System.out.println(json.toString());

		seed = json.getInt(ARG_SEED_LONG);

		roundConfig = CreateGameConfig(json);

		gamesCount = json.getInt(ARG_GAMES_COUNT_LONG);

		reverseGames = false;
	}

	private static String CreateGameConfig(JSONObject json)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("GAME;x;x;");

		// timeout for bot command
		builder.append(json.getInt("botCommandTimeoutMillis"));
		builder.append(";");

		// visualize, visualize continuous, frame time, logging to console, seed (will be autochanged), fully observable game
		builder.append("false;false;200;false;-1;true;");

		// starting armies
		builder.append(json.getInt("startingArmies"));
		builder.append(";");

		// max game rounds
		builder.append(json.getInt("maxGameRounds"));
		builder.append(";");

		// fight mode - either ORIGINAL_A60_D70 or CONTINUAL_1_1_A60_D70, see FightMode enum
		builder.append(json.getString("fightMode"));

		return builder.toString();
	}

	private static void sanityChecks() {
		System.out.println("Sanity checks...");
		
		System.out.println("-- seed: " + seed);
		System.out.println("-- game config: " + roundConfig);
		System.out.println("-- #games: " + gamesCount);
		System.out.println("-- play reversed games: " + reverseGames);

		if (resultDir != null)
		{
			resultDirFile = new File(resultDir);
			System.out.println("-- result dir: " + resultDir + " --> " + resultDirFile.getAbsolutePath());


			if (!resultDirFile.exists()) {
				System.out.println("---- result dir does not exist, creating!");
				resultDirFile.mkdirs();
			}
			if (!resultDirFile.exists()) {
				fail("Result dir does not exists. Parsed as: " + resultDir + " --> " + resultDirFile.getAbsolutePath());
			}
			if (!resultDirFile.isDirectory()) {
				fail("Result dir is not a directory. Parsed as: " + resultDir + " --> " + resultDirFile.getAbsolutePath());
			}
			System.out.println("---- result directory exists, ok");
		}

		if (replayDir != null) {

			replayDirFile = new File(replayDir);
			System.out.println("-- replay dir: " + replayDir + " --> " + replayDirFile.getAbsolutePath());

			if (!replayDirFile.exists()) {
				System.out.println("---- replay dir does not exist, creating!");
				replayDirFile.mkdirs();
			}
			if (!replayDirFile.exists()) {
				fail("Replay dir does not exists. Parsed as: " + replayDir + " --> " + replayDirFile.getAbsolutePath());
			}
			if (!replayDirFile.isDirectory()) {
				fail("Replay dir is not a directory. Parsed as: " + replayDir + " --> " + replayDirFile.getAbsolutePath());
			}
			System.out.println("---- replay directory exists, ok");
		}

		if (tableFileName != null) {

			tableFile = new File(tableFileName);
			System.out.println("-- table file: " + tableFileName + " --> " + tableFile.getAbsolutePath());

			if (tableFile.exists() && !tableFile.isFile()) {
				fail("Table file exists and is not a file. Parsed as: " + tableFileName + " --> " + tableFile.getAbsolutePath());
			}

		}

		if (bot1Name != null && bot2Name != null && bot1Init != null && bot2Init != null) {
			batchFight = false;
			System.out.println("-- Bot 1 & 2 ids / inits specified, will execute 1v1 fights");
			System.out.println("---- bot1: " + bot1Name + " / " + bot1Init);
			System.out.println("---- bot2: " + bot2Name + " / " + bot2Init);
		} else 
		if (botIdBatch != null && botsBatchPropertyFileName != null) {
			batchFight = true;				
			System.out.println("-- Bot batch ID + Bots batch property file name specified, will execute batch fights");
			
			System.out.println("---- Bot ID for batch fights: " + botIdBatch);
			
			botsBatchPropertyFile = new File(botsBatchPropertyFileName);
			System.out.println("---- Bots property file for batch fights: " + botsBatchPropertyFileName + " --> " + botsBatchPropertyFile.getAbsolutePath());
			
			if (!botsBatchPropertyFile.exists()) {
				fail("------ File does not exist: " + botsBatchPropertyFileName + " --> " + botsBatchPropertyFile.getAbsolutePath());
			}
			if (!botsBatchPropertyFile.isFile()) {
				fail("------ File is not a file: " + botsBatchPropertyFileName + " --> " + botsBatchPropertyFile.getAbsolutePath());
			}
			System.out.println("------ Bots property file exists, ok");
			
		} else {
			fail("Invalid specification, you either have to specify Bot 1 Id+Init and Bot 2 Id+Init for 1v1 fights, or Bot Id for batch fights together with property files with botId=botInit pairs.");
		}

	    System.out.println("Sanity checks OK!");
	}
	
	private static void fight() throws Exception {
		
		if (batchFight) {
			batchFight();
		} else {
			fight1v1();
		}
	}
	
	private static void fight1v1() throws Exception {
	
		System.out.println("EXECUTING 1v1 FIGHT!");
		
		ConquestFightConfig config = new ConquestFightConfig();
		
		config.config = Config.fromString(roundConfig);
		config.seed = seed;
		config.games = gamesCount;
		config.config.bot1JarPath = bot1JarPath;
		config.config.bot2JarPath = bot2JarPath;
		config.config.bot1Init = bot1Init;
		config.config.bot2Init = bot2Init;
		
		ConquestFight fight = new ConquestFight(config, tableFile, resultDirFile, replayDirFile, resultJsonFile);
		fight.fight(bot1Name, bot1Init, bot2Name, bot2Init);
		
		if (reverseGames) {
			fight.fight(bot2Name, bot2Init, bot1Name, bot1Init);
		}
	}
	
	private static void batchFight() throws Exception {
		System.out.println("EXECUTING BATCH FIGHTS!");
		
		ConquestFightConfig config = new ConquestFightConfig();
		
		config.config = Config.fromString(roundConfig);
		config.seed = seed;
		config.games = gamesCount;
		
		ConquestFightBatch batch = new ConquestFightBatch(botsBatchPropertyFile, config);
		
		batch.fight(botIdBatch, reverseGames, tableFile, resultDirFile, replayDirFile, resultJsonFile);
	}
		
	// ==============
	// TEST ARGUMENTS
	// ==============
	
	public static String[] getTestArgs_1v1() {
		return new String[] {
				  "-s", "20"     // seed
				, "-o", "GAME;x;x;5000;false;false;200;false;-1;true;5;100;CONTINUAL_1_1_A60_D70"   // game-config
				, "-g", "10"      // games-count
				, "-r", "false"   // reverse-games
				, "-a", "MyBot"                              // bot1-id
				, "-b", "internal:conquest.bot.playground.MyBot" // bot1-init
				, "-c", "AggressiveBot"                                 // bot2-id
				, "-d", "internal:conquest.bot.custom.AggressiveBot"           // bot2-init
				, "-u", "./results/fights"              // result-dir
				, "-y", "./results/replays"           // replay-dir
				, "-t", "./results/all-results.csv"   // single results file
		};
		
		// engine config:
		//		result.gameId = parts[0];                                        // should be always: GAME
		//		result.player1Name = parts[1];                                   // will be auto-changed
		//		result.player2Name = parts[2];                                   // will be auto-changed
		//		result.botCommandTimeoutMillis = Integer.parseInt(parts[3]);
		//		result.visualize = Boolean.parseBoolean(parts[4]);
		//		result.visualizeContinual = (parts[5].toLowerCase().equals("null") ? null : Boolean.parseBoolean(parts[5]));
		//		result.visualizeContinualFrameTimeMillis = (parts[6].toLowerCase().equals("null") ? null : Integer.parseInt(parts[6]));
		//		result.logToConsole = Boolean.parseBoolean(parts[7]);

		// followed by game config:
		//		result.seed = Integer.parseInt(parts[0]);                        // will be auto-changed according to master seed above
		//		result.fullyObservableGame = Boolean.parseBoolean(parts[1]);
		//		result.startingArmies = Integer.parseInt(parts[2]);
		//		result.maxGameRounds = Integer.parseInt(parts[3]);
		//		result.fight = FightMode.valueOf(parts[4]);                      // see FightMode for strings
	}
	
	public static String[] getTestArgs_Batch() {
		return new String[] {
				  "-s", "20"     // seed
				, "-o", "GAME;x;x;5000;false;null;null;false;-1;true;5;100;CONTINUAL_1_1_A60_D70"   // game-config
				, "-g", "3"      // games-count
				, "-r", "true"   // reverse-games
				, "-e", "AggressiveBot"               // bot-id that will perform fights against all other bots within batch property file
				, "-f", "batch-fight.properties" 	  // batch property file
				, "-u", "./results/fights"            // result-dir
				, "-y", "./results/replays"           // replay-dir
				, "-t", "./results/all-results.csv"   // single results file
		};
	}

	private static void ExitOk()
	{
		System.out.println("OK");
		System.exit(0);
	}

	private static void ExitError(String message)
	{
		System.out.println(message);
		System.exit(200);
	}

	private static void InitBotsFromJar(File botJarFile, File bot2JarFile)
	{
		String botName = botJarFile.getName().replace(".jar", "");
		String botInit = "external:conquest.bot.custom." + botName.replace(".jar", "");

		String botName2 = bot2JarFile.getName().replace(".jar", "");
		String botInit2 = "external:conquest.bot.custom." + botName2.replace(".jar", "");

		bot1Name = botName;
		bot1Init = botInit;

		bot2Name = botName2;
		bot2Init = botInit2;
	}

	private static File FirstOrDefaultJarFileFromDirectory(File directory)
	{
		for (File f : directory.listFiles())
		{
			if (f.getName().endsWith(".jar"))
				return f;
		}
		return null;
	}

	public static void main(String[] args) {
		// -----------
		// FOR TESTING
		// -----------
		//args = getTestArgs_1v1();		
		//args = getTestArgs_Batch();
		
		// --------------
		// IMPLEMENTATION
		// --------------

		if (args.length < 3)
		{
			ExitError("At least three args expected.");
		}

		try
		{
			header();

			readConfigFromJson(args[0] + System.getProperty("file.separator") + "config.json");

			File source1Directory = new File(args[1]);
			File jarBot1File = FirstOrDefaultJarFileFromDirectory(source1Directory);
			bot1JarPath = args[1];
			File source2Directory = new File(args[2]);
			File jarBot2File = FirstOrDefaultJarFileFromDirectory(source2Directory);
			bot2JarPath = args[2];

			InitBotsFromJar(jarBot1File, jarBot2File);
			if (args.length > 3)
			{
				resultJsonFile = new File(args[3] + System.getProperty("file.separator") + "match-results.json");
				replayDir = args[3];
				resultDir = args[3];
			}

			sanityChecks();

			fight();

			System.out.println("---// FINISHED //---");

			ExitOk();
		}
		catch (Exception e)
		{
			ExitError(e.toString());
		}
	}

}