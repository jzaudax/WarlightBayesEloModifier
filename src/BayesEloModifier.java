import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
	
public class BayesEloModifier {

	public static final String INPUTFILE = "inputfile:";
	public static final String OUTPUTFILE = "outputfile:";
	public static final String MODE = "mode:";
	public static final String PASTETYPE = "pastetype:";
	public static final String PLAYER = "player:";
	public static final String REMOVEOPPONENT = "removeopponent:";
	public static final String REMOVEGAME = "removegame:";
	public static final String TOGGLEMYWIN = "togglemywin:";
	public static final String TOGGLEWIN = "togglewin:";
	public static final String ADDGAME = "addgame:";
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		BayesEloModifier modifier = new BayesEloModifier();
	
		String settingsfile = "settings.txt";
		if(args.length > 0) {
			settingsfile = args[0];
		}
		
		// Extract the settings
		String inputfile = "";
		String outputfile = inputfile + "_out";
		boolean fastmode = true;
		boolean output = true;
		String pastetype = BayesEloModifier.ADDGAME;
		String player = null;
		ArrayList<String> opponentsToRemove = new ArrayList<String>();
		ArrayList<String> gamesToRemove = new ArrayList<String>();
		ArrayList<String> toggleMyWins = new ArrayList<String>();
		ArrayList<String> toggleWins = new ArrayList<String>();
		ArrayList<String> gamesToAdd = new ArrayList<String>();
		try {
			java.io.FileReader inFile = new java.io.FileReader(settingsfile);
			java.io.BufferedReader settingsFile = new java.io.BufferedReader(inFile);
			
			String getLine;
			while ((getLine = settingsFile.readLine()) != null) 
			{
				if(getLine.startsWith(INPUTFILE)) {
					inputfile = getLine.substring(INPUTFILE.length()).trim();
				} else if(getLine.startsWith(OUTPUTFILE)) {
					outputfile = getLine.substring(OUTPUTFILE.length()).trim();
				} else if(getLine.startsWith(MODE)) {
					String mode = getLine.substring(MODE.length()).trim();
					if(mode.equalsIgnoreCase("pretty")) {
						fastmode = false;
					}
					if(mode.equalsIgnoreCase("silent")) {
						output = false;
					}
				} else if(getLine.startsWith(PASTETYPE)) {
					pastetype = getLine.substring(PASTETYPE.length()).trim();
					if(!pastetype.endsWith(":")) {
						pastetype += ":";
					}
				} else if(getLine.startsWith(PLAYER)) {
					player = modifier.truncate(getLine.substring(PLAYER.length()).trim());
				} else if(getLine.startsWith(REMOVEOPPONENT)) {
					opponentsToRemove.add(modifier.truncate(getLine.substring(REMOVEOPPONENT.length()).trim()));
				} else if(getLine.startsWith(REMOVEGAME)) {
					String players = getLine.substring(REMOVEGAME.length()).trim();
					String player1 = modifier.truncate(players.substring(0, players.indexOf("::")));
					String player2 = modifier.truncate(players.substring(players.indexOf("::") + 2));
					gamesToRemove.add(player1 + "::" + player2);
				} else if(getLine.startsWith(TOGGLEMYWIN)) {
					toggleMyWins.add(modifier.truncate(getLine.substring(TOGGLEMYWIN.length()).trim()));
				} else if(getLine.startsWith(TOGGLEWIN)) {
					String players = getLine.substring(TOGGLEWIN.length()).trim();
					String player1 = modifier.truncate(players.substring(0, players.indexOf("::")));
					String player2 = modifier.truncate(players.substring(players.indexOf("::") + 2));
					toggleWins.add(player1 + "::" + player2);
				} else if(getLine.startsWith(ADDGAME)) {
					String players = getLine.substring(ADDGAME.length()).trim();
					int index1 = players.indexOf("::");
					int index2 = players.indexOf("::", index1+2);
					String player1 = modifier.truncate(players.substring(0, index1));
					String player2 = modifier.truncate(players.substring(index1 + 2, index2));
					String win = modifier.truncate(players.substring(index2 + 2));
					gamesToAdd.add(player1 + "::" + player2 + "::" + win);
				} else if(getLine.contains(" defeated ")) {
					String[] stringArray = getLine.split("\t");
					String game = stringArray[0].trim();
					// Ignore expired games
					if(game.contains("(expires in") || game.contains("(expiration")) {
						game = game.substring(0, game.indexOf("(expir"));
					}
					if(!game.contains("(expired)")) {
						String[] players = game.split(" defeated ");
						String player1 = modifier.truncate(players[0]);
						String player2 = modifier.truncate(players[1]);
						if(pastetype.equalsIgnoreCase(REMOVEGAME)) {
							gamesToRemove.add(player1 + "::" + player2);
						} else if(pastetype.equalsIgnoreCase(ADDGAME)) {
							gamesToAdd.add(player1 + "::" + player2 + "::win");
						} else if(pastetype.equalsIgnoreCase(TOGGLEWIN)) {
							toggleWins.add(player1 + "::" + player2 + "::win");
						}
					}
				}
			}
			settingsFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		modifier.run(inputfile, outputfile, player, opponentsToRemove, toggleMyWins, gamesToRemove, toggleWins, gamesToAdd);
		System.out.println();
		//System.out.println("Program ran successfully.");
		//System.out.println("Copy the contents of " + outputfile + " into the bayeselo.exe program.");
		//System.out.println("Once bayeselo.exe has finished running, results can be found in the file \"output" + outputfile + "\"");
		System.out.println("Note that all players will be ranked, including those who are not participating in the ladder and those who have not completed enough games to be ranked.");
		
		modifier.submitToBayesElo(outputfile, fastmode, output);
		System.out.println("Note that all players will be ranked, including those who are not participating in the ladder and those who have not completed enough games to be ranked.");
		System.out.println("Results can be found in the file \"results_" + outputfile + "\"");
	}
	
	private void submitToBayesElo(String outputfile, boolean fastmode, boolean output) {
		try {
			//URL url = BayesEloModifier.class.getProtectionDomain().getCodeSource().getLocation();
			File jarFile = new File(BayesEloModifier.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			String jarDir = jarFile.getParentFile().getPath() + "\\";
			ProcessBuilder pb = null;
			try {
				pb = new ProcessBuilder(jarDir + "bayeselo.exe");
			} catch (Exception e) {
				pb = new ProcessBuilder("bayeselo.exe");
			}
			System.out.print("Running....");
			// Prints the output of the bayeselo program to this window
			if(output) {
				pb.redirectOutput(Redirect.INHERIT);
				pb.redirectError(Redirect.INHERIT);
				pb.redirectErrorStream(true);
			} else {
				File file = new File("bayesEloOutput.txt");
				pb.redirectOutput(Redirect.to(file));
				pb.redirectError(Redirect.to(file));
				pb.redirectErrorStream(true);
			}
			Process process = pb.start();
			//InputStream is = process.getInputStream();
			//InputStreamReader isr = new InputStreamReader(is);
			//BufferedReader br = new BufferedReader(isr);
			//String line;
	
			//System.out.printf("Output of running %s is:", Arrays.toString(args));
			
			//while ((line = br.readLine()) != null) {
			//  System.out.println(line + "\r\n");
			//}

			// This writes to the bayeselo executable
			OutputStream stdin = process.getOutputStream();
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

	        // This reads from the modified file that was created
	        java.io.FileReader inFile = new java.io.FileReader(outputfile);
			java.io.BufferedReader bufferFile = new java.io.BufferedReader(inFile);
			
			String getLine;
			while ((getLine = bufferFile.readLine()) != null) 
			{
				if(output) {
					System.out.println(getLine);
				}
		        writer.write(getLine);
		        writer.newLine();
		        writer.flush();
		        if(!fastmode) {
		        	Thread.sleep(1);
		        }
			}
	        writer.close();
			bufferFile.close();
			System.out.println("complete");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run(String infile, String outfile, String playername, 
			ArrayList<String> myremoves, ArrayList<String> mytoggles, 
			ArrayList<String> removegames, ArrayList<String> togglegames,
			ArrayList<String> addgames) {
		//ArrayList<String> removes = new ArrayList<String>();
		//for(Iterator<String> iter = opponentsToRemove.iterator(); iter.hasNext();) {
		//	String item = iter.next();
		//	String truncated = truncate(item);
		//	removes.add(truncated);
		//}
		//String playernametrunc = truncate(playername);
		String playerid = null;
		try {
			HashMap<String, String> namelookupmap = new HashMap<String, String>();
			HashMap<String, String> idlookupmap = new HashMap<String, String>();
			
			java.io.BufferedReader bufferFile = null;
			
			if(infile.startsWith("http")) {
				URL u = new URL(infile);
				InputStream in = u.openStream();
				bufferFile = new java.io.BufferedReader(new InputStreamReader(in));
			} else {
				java.io.FileReader inFile = new java.io.FileReader(infile);
				bufferFile = new java.io.BufferedReader(inFile);
			}
			
			FileWriter outFile = new FileWriter(outfile);
			BufferedWriter outputFile = new BufferedWriter(outFile);
			
			String getLine;
			while ((getLine = bufferFile.readLine()) != null) 
			{
				if(getLine.startsWith("addplayer")) {
					int numberlocation = getLine.indexOf(";") + 1;
					if(numberlocation > 12) {
						String value = getLine.substring(10, numberlocation - 2).trim();
						String key = getLine.substring(numberlocation).trim();
						if(value.equals(playername)) {
							playerid = key;
						}
						System.out.println("Adding player " + value + " to namelookupmap as " + key);
						namelookupmap.put(key, value);
						idlookupmap.put(value, key);
					}
				} else if(getLine.startsWith("addresult")) {
					if((myremoves.size() > 0 || mytoggles.size() > 0) && playerid == null) {
						System.err.println("\"player\" not found: " + playername);
						System.err.println("Player must be specified if using removeopponent or togglemywin.");
						break;
					}
					int location1 = getLine.indexOf(" ") + 1;
					int location2 = getLine.indexOf(" ", location1) + 1;
					int location3 = getLine.indexOf(" ", location2) + 1;
					String player1id = getLine.substring(location1, location2 - 1).trim();
					String player2id = getLine.substring(location2, location3 - 1).trim();
					if(player1id.equals(playerid)) {
						String other = namelookupmap.get(player2id);
						if(myremoves.contains(other)) {
							myremoves.remove(other);
							System.out.println("Skipped game between " + playername + " and " + other);
							continue;
						}
						if(mytoggles.contains(other)) {
							String winresult = getLine.substring(location3);
							if(winresult.equals("0")) {
								getLine = getLine.substring(0, location3) + "2";
							} else {
								getLine = getLine.substring(0, location3) + "0";
							}
							mytoggles.remove(other);
							System.out.println("Toggled game against " + other);
						}
						
					} else if(player2id.equals(playerid)) {
						String other = namelookupmap.get(player1id);
						if(myremoves.contains(other)) {
							myremoves.remove(other);
							System.out.println("Skipped game between " + playername + " and " + other);
							continue;
						}
						if(mytoggles.contains(other)) {
							String winresult = getLine.substring(location3);
							if(winresult.equals("0")) {
								getLine = getLine.substring(0, location3) + "2";
							} else {
								getLine = getLine.substring(0, location3) + "0";
							}
							mytoggles.remove(other);
							System.out.println("Toggled game against " + other);
						}
					}
					
					String player1name = namelookupmap.get(player1id);
					String player2name = namelookupmap.get(player2id);
					String str1 = player1name + "::" + player2name;
					String str2 = player2name + "::" + player1name;
					if(removegames.contains(str1)) {
						removegames.remove(str1);
						System.out.println("Skipped game between " + player1name + " and " + player2name);
						continue;
					} else if(removegames.contains(str2)) {
						removegames.remove(str2);
						System.out.println("Skipped game between " + player1name + " and " + player2name);
						continue;
					}
					if(togglegames.contains(str1)) {
						String winresult = getLine.substring(location3);
						if(winresult.equals("0")) {
							getLine = getLine.substring(0, location3) + "2";
						} else {
							getLine = getLine.substring(0, location3) + "0";
						}
						System.out.println("Toggled game between " + player1name + " and " + player2name);
						togglegames.remove(str1);
					} else if(togglegames.contains(str2)) {
						String winresult = getLine.substring(location3);
						if(winresult.equals("0")) {
							getLine = getLine.substring(0, location3) + "2";
						} else {
							getLine = getLine.substring(0, location3) + "0";
						}
						System.out.println("Toggled game between " + player1name + " and " + player2name);
						togglegames.remove(str2);
					}
				} else if(getLine.startsWith("elo")) {
					// Add the games that need to be included
					for(Iterator<String> iter = addgames.iterator(); iter.hasNext();) {
						String game = iter.next();
						int index1 = game.indexOf("::");
						int index2 = game.indexOf("::", index1+2);
						String player1 = game.substring(0, index1);
						String player2 = game.substring(index1 + 2, index2);
						String win = game.substring(index2 + 2);
						
						String playerid1 = idlookupmap.get(player1);
						if(playerid1 == null) {
							System.err.println("Player ID not found: " + player1);
							continue;
						}
						String playerid2 = idlookupmap.get(player2);
						if(playerid2 == null) {
							System.err.println("Player ID not found: " + player2);
							continue;
						}
						String winid = "2";
						if(win == null || win.length() == 0) {
							System.err.println("No winner specified: addgame:" + game);
							continue;
						}
						if(win.equalsIgnoreCase("win")
								|| win.equalsIgnoreCase("w")
								|| win.equalsIgnoreCase("1")
								|| win.equalsIgnoreCase("2")) {
							winid = "2";
						} else if(win.equalsIgnoreCase("lose")
								|| win.equalsIgnoreCase("loss")
								|| win.equalsIgnoreCase("l")
								|| win.equalsIgnoreCase("0")) {
							winid = "0";
						}
						outputFile.write("addresult " + playerid1 + " " + playerid2 + " " + winid);
						outputFile.newLine();
					}
				} else if(getLine.startsWith("ratings")) {
					outputFile.write(getLine + " >results_" + outfile);
					outputFile.newLine();
					continue;
				}
				outputFile.write(getLine);
				outputFile.newLine();
			}
			
			for(Iterator<String> iter = myremoves.iterator(); iter.hasNext(); ) {
				System.err.println("Remove Opponent: " + iter.next() + " not found.");
			}
			for(Iterator<String> iter = removegames.iterator(); iter.hasNext(); ) {
				System.err.println("Remove Game: " + iter.next() + " not found.");
			}
			for(Iterator<String> iter = mytoggles.iterator(); iter.hasNext(); ) {
				System.err.println("My Toggle: " + iter.next() + " not found.");
			}
			for(Iterator<String> iter = togglegames.iterator(); iter.hasNext(); ) {
				System.err.println("Toggle: " + iter.next() + " not found.");
			}
			bufferFile.close();
			outputFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String truncate(String item) {
		String returnstr = "";
		for(int i = 0; i < item.length(); i++) {
			String charat = item.substring(i, i+1);
			if(charat.matches("[a-z]|[A-Z]|[0-9]")) {
				returnstr += charat;
			}
		}
		return returnstr;
	}
}
