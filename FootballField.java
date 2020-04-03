import java.awt.Point;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class FootballField {
	//has two teams
	//does the actual analysis of the plays of each team over a specified margin of time (2-3 min)
	//get the part of each player's route from the play class itself and assign to each player
	final double CONSTANT_OF_SEPARATION = 1.41;
	
	private String winningTeam; //whoever is winning at the moment
	
	private DefensePlay expectedPlay; //what is expected from the defense
	private OffensePlay lastPlay; //the last play that the offense did (will be associated with certain defensive responses)
	private OffensePlay recoPlay; //the recommended play for offense
	private OffensePlay currPlay; //the curr play for offense
	
	private List<String> userDefensePlay;
	private List<Player> playerList; //all players
	private List<Player> offensePlayers; //all offensive players
	private List<Player> defensePlayers; //all defensive players
	
	private Map<Player, Route> oPlayerToRoute; //all offensive players and the route they just ran
	private Map<Player, Route> dPlayerToRoute; //all defensive players and the route they just ran 
	
	private Map<OffensePlay, Map<DefensePlay, Boolean>> playMap; //all offensive plays associated with what the defense did in response to them

	private Scanner fileReader; //used for the file
	BufferedWriter bw; //printwriter for the text document
	
	private int currentDown; //the current down the play was run on
	private int tillFirstDown; //yards till a first down
	
	public FootballField(String f_name) {
		try { //reads from the file
			File f = new File(f_name);
			fileReader = new Scanner(f);
			fileReader.useDelimiter("EOP");
			bw = new BufferedWriter(new FileWriter(f_name, true));
			playerList = new ArrayList<Player>(); 
			currentDown=1;
			tillFirstDown=10;
			offensePlayers = new ArrayList<>();
			defensePlayers = new ArrayList<>();
			userDefensePlay = new ArrayList<>();
			int numPlayers  = Integer.parseInt(fileReader.nextLine());
			for(int i=0; i<numPlayers; i++){
				String position = fileReader.nextLine();
				String name = fileReader.nextLine();
				String teamName = fileReader.nextLine();
				Player player;
				if(position.equals("Wide Receiver")) {
					player = new WideReceiver(name, teamName);
					offensePlayers.add(player);
				}
				else {
					player = new DefensiveBack(name, teamName);
					defensePlayers.add(player);
				}
				//need something to do for defensive players too
				playerList.add(player); //add player to player list and offense/defense respectively
			}
			winningTeam = "No plays run yet"; //no winner by default 
			oPlayerToRoute = new HashMap<>(); 
			dPlayerToRoute = new HashMap<>();
			playMap = new HashMap<>();
		}
		catch(Exception e) { //catches exception if file doesnt work
			e.printStackTrace(); 
		}
	}
	
	public OffensePlay previousPlay() {
		if(lastPlay==null) {
			System.out.println("No plays commenced");
			throw new IllegalStateException();
		}
		return lastPlay; //if no plays have been run, then throws exception. otherwise returns last play
	}
	
	public DefensePlay expectedPlay() {
		if(expectedPlay==null) {
			System.out.println("No plays commenced");
			throw new IllegalStateException();
		}
		return expectedPlay; //if no plays have been run, then throws exception. otherwise returns expected play
	}
	
	public List<Player> getPlayers(){
		return playerList;
	}
	
	public void commencePlay() {
		//keep track of all information in each sequence
		//draw a football field
		drawField();
		updatePlay();
		recommendedPlay();
		//draw out each route on it w models/position numbers
		for(Player p : expectedPlay.getPlay().keySet()) {
			List<Vector> r = expectedPlay.getPlay().get(p).getRoute();
			for(int i=1; i<r.size(); i++) {
				r.get(i).setBeginningPoint(r.get(i-1).getEndPoint());
		    	if(r.get(i).getEndPoint().x < 0) {
		    		r.get(i).setEndPointX(0);
		    	}
		    	if(r.get(i).getEndPoint().y < 0) {
		    		r.get(i).setEndPointY(0);
		    	}
			}
		}
		drawOriginal();
		System.out.println("Press 1 to continue");
		while(!StdDraw.isKeyPressed(49)) {
		}
		StdDraw.clear();
		drawField();
		drawDefensiveBacks(userDefensePlay.size()!=0);
		System.out.println("Press 2 to continue");
		while(!StdDraw.isKeyPressed(50)) {
		}
		StdDraw.clear();
		drawField();
		drawNew();
		System.out.println("Press 3 to draw all three at once");
		while(!StdDraw.isKeyPressed(51)) {
		}
		StdDraw.clear();
		drawField();
		drawOriginal();
		drawDefensiveBacks(userDefensePlay.size()!=0);
		drawNew();
		System.out.println("If any original routes do not appear that means the defensive"
				+ " back either followed them well or the same route is recommended again");
		//set the new expected defense play
		//prompt if they want to swap players for the current team
		Scanner tempScan = new Scanner(System.in);
		System.out.println("Do you want to switch players? "
				+ "If Yes, which players? (Put the new player first)");
		String response = tempScan.nextLine();	//if yes then ask which players and call swap on them
		//prompt if they want to use the recommended play as the next play
		String[] players = response.split(" and ");
		if(players[0].toLowerCase().charAt(0)!='n') {
			System.out.println("What team are they on?");
			String team = tempScan.nextLine();
			for(Player p : playerList) {
				if(p.getName().toLowerCase().equals(players[1].trim().toLowerCase())) {
					p.setName(players[0].trim());
					p.setTeam(team.trim());
				}
			}
		}
		System.out.println("Do you want to use the recommended play as the next play and run it?");
		String response2 = tempScan.nextLine();
		//update the new expected play based on the map if the recommended play exists in there
		//if it doesn't exist call expected
		if(response2.toLowerCase().charAt(0)=='y') {
			for(Player p : defensePlayers) {
				System.out.println("What is " + p.getName() +"'s new route?");
				String route = tempScan.nextLine();
				userDefensePlay.add(route);
			}
			for(Player p : offensePlayers) {
				p.setCurrRoute(currPlay.getPlay().get(p));
			}
			lastPlay = currPlay;
			currPlay = recoPlay;
			try {
				writeToFile();
			}
			catch(Exception e){
				System.out.println(e);
			}
			commencePlay();
		}
		else {
			try {
				bw.close();
			}
			catch(Exception e) {
				System.out.println(e);
			}
		}
	}
	
	public void updatePlay() {
		//find a layout for the file that is necessary to do everything	
		//Player Name: Route they ran in points made up of " " separated by ,
		//Vectors are split into a point for x origin y origin and x end y end
		//after all parts of the play are done: EOP
		String line;
		while(fileReader.hasNext() && !(line = fileReader.nextLine()).equals("EOP")) {
			String[] splitLine = line.split(":"); //splits name, vectors
			String[] splitVectors = splitLine[1].split(","); //splits the list of vectors into each other
			for(int i=0; i<splitVectors.length; i++) {
				splitVectors[i] = splitVectors[i].trim();
			}
			List<Vector> vectors = new ArrayList<Vector>();
			for(String x : splitVectors) {
				String[] splitPoint = x.split(" "); //splits every vector in the list into its point components
				Point p = new Point(Integer.parseInt(splitPoint[0]), Integer.parseInt(splitPoint[1])); //makes point
				Vector v = new Vector(p, Integer.parseInt(splitPoint[2]), Integer.parseInt(splitPoint[3])); //makes vector
				vectors.add(v); //creates a vector and adds it to the list
			}
			Route newRoute = new Route(vectors); //vectors, name
			//NEED TO SPEED THIS UP. CANT GO THROUGH EVERY SINGLE PLAYER
			for(Player p : offensePlayers) {
				if(splitLine[0].equals(p.getName())) { //for every player, adds the route they ran to the map
					oPlayerToRoute.put(p, newRoute);
					p.setCurrRoute(newRoute); //update the player to the route they ran
				}
			}
			for(Player p : defensePlayers) { //repeats for defensive players
				if(splitLine[0].equals(p.getName())) {
					dPlayerToRoute.put(p, newRoute);
					p.setCurrRoute(newRoute); //update the player to the route they ran
				}
			}
		}
		OffensePlay newOffensePlay = new OffensePlay(oPlayerToRoute);
		DefensePlay newDefensePlay = new DefensePlay(dPlayerToRoute); //creates new offense and defense plays from the maps
		lastPlay = currPlay;
		currPlay = newOffensePlay; //update the new plays
		int totYards=0; 
		for(Player player : currPlay.getPlay().keySet()) {
			if(player.getPosition()=="Wide Receiver") {
				totYards+=player.yardsLastRoute();
			}
		}
		tillFirstDown-=totYards;
		currentDown+=1;
		if(tillFirstDown<=0) {
			currentDown=1;
		}
		else if(tillFirstDown>0 && currentDown>4) {
			System.out.println("Turnover on Downs!");
			throw new IllegalStateException();
		}
		//check for first downs and such. if positive yards was there, then they win. can be modified based on down
		if(totYards>0 && currentDown<4) {
			winningTeam=offensePlayers.get(0).getTeam();
		}
		else {
			winningTeam=defensePlayers.get(0).getTeam();
			if(currentDown==1) {
				winningTeam=offensePlayers.get(0).getTeam();
			}
		}
		//adds the play to the map
		boolean brake = true;
		if(playMap.isEmpty()) {
			brake = false;
		}
		Map<DefensePlay, Boolean> newMap = new HashMap<>();
		if(winningTeam==offensePlayers.get(0).getTeam()) {
			newMap.put(newDefensePlay, true);
		}
		else {
			newMap.put(newDefensePlay, false);
		}
		if(brake && playMap.containsKey(newOffensePlay)) { //adds the new play to the set of defenses plays for that offensive play
			playMap.put(newOffensePlay, newMap); 
		}
		else if(brake){ //if it isnt already in the map of offense plays to defense plays
			playMap.put(newOffensePlay, newMap);
		}
		else {
			playMap.put(newOffensePlay, newMap);
		}
	}
	
	public void recommendedPlay() {
		//playSet contains all of the plays that are wins
		Map<OffensePlay, Integer> winMap = new HashMap<>(); //the offensive plays and how often they have won
		Map<DefensePlay, Integer> lossMap = new HashMap<>(); //the defensive plays and how often they have won
		int maxWins=0; //number of wins for the current play
		int maxLoss=0; //number of losses for the current play
		OffensePlay bigPlay=null; //the best play for offense and defense respectively
		DefensePlay bigDPlay=null; 
		for(OffensePlay play : playMap.keySet()) { //goes through all offense plays
			Map<DefensePlay, Boolean> dPlayMap = playMap.get(play); //links to true if the offense won
			for(DefensePlay dPlay : dPlayMap.keySet()) { //goes through all defense plays linked to offense play
				if(dPlayMap.get(dPlay)) { //true if the offense won the play
					if(!winMap.keySet().contains(play)) { 
						winMap.put(play, 1);
					}
					else {
						int i = winMap.get(play);
						winMap.put(play, i++); //adds the offense play and how many wins there are to win map
					}
				}
				else { //false if the defense won the play
					if(!lossMap.keySet().contains(play)) {
						lossMap.put(dPlay, 1);
					}
					else {
						int i = lossMap.get(dPlay);
						lossMap.put(dPlay, i++); //adds the defense play and how many losses there are to loss map
					}
				}
				if(lossMap.containsKey(dPlay) && lossMap.get(dPlay)>maxLoss) { //finds the defense play that succeeded the most times
					bigDPlay=dPlay; //this is the new best defensive play
					maxLoss=lossMap.get(dPlay); //how often this play succeeded
				}
			}
			if(winMap.containsKey(play) && winMap.get(play)>maxWins) { //finds the offense play that succeeded the most times overall
				bigPlay=play; //the new best offensive play
				maxWins=winMap.get(play); //how often this play succeeded
			}
		}
		expectedPlay=bigDPlay;
		//whichever has the most wins against is weak
		if(maxWins>0) { //if there were any winning plays
			recoPlay=bigPlay;
		}
		else {
			//need to generate a new play that will win
			//generate a map of routes somehow and use it to generate a new play of some kind
			//need to use the special vector methods for this
			Map<Player, List<Vector>> dPlayerToVector = bigDPlay.getRoutes(); //maps defensive players and their plays
			Map<Player, Point> defendersEndPoints = new HashMap<>();
			for(Player player : dPlayerToVector.keySet()) {
				defendersEndPoints.put(player, player.currRoute().getEnd()); //adds the end point of the last route each defender ran
			}
			Map<Player, Player> offenseToClosestDefense = new HashMap<>(); //each offensive player and the defense player they are closest to
			for(Player oPlayer : offensePlayers) {
				Player leastPlayer = null;
				double leastDist = Double.MAX_VALUE;
				for(Player dPlayer : defendersEndPoints.keySet()) {
					if(pointDist(defendersEndPoints.get(dPlayer), oPlayer.currRoute().getEnd())<leastDist) {
						leastPlayer = dPlayer; //keeps track of player closest to the wide receiver
						leastDist = pointDist(defendersEndPoints.get(dPlayer), oPlayer.currRoute().getEnd());
					}
				}
				offenseToClosestDefense.put(oPlayer, leastPlayer); //map of the offense players to their defense players
			}
			//get each defense players end point and compare to offense endpoint to change it
			Map<Player, Route> newPlayMap = new HashMap<>();
			for(Player oPlayer : offensePlayers) {
				List<Vector> oRoute = new ArrayList<>();
				if(oPlayer.currRoute()!=null) {
					oRoute = oPlayer.currRoute().getRoute();
				}
				List<Vector> dRoute = new ArrayList<>();
				if(offenseToClosestDefense.get(oPlayer)!= null && offenseToClosestDefense.get(oPlayer).currRoute() != null) {
					dRoute = offenseToClosestDefense.get(oPlayer).currRoute().getRoute();
				}
				List<Vector> improvedRoute = new ArrayList<>();
				int max = Math.max(oRoute.size(), dRoute.size());
				for(int i=0; i<max; ++i){
				    if(pointDist(oRoute.get(i).getEndPoint(), dRoute.get(i).getEndPoint())>CONSTANT_OF_SEPARATION) {
				    	improvedRoute.add(oRoute.get(i));
				    }
				    else {
				    	//need to find a route that adjusts the spacing
				    	String sides = whichSide(oRoute.get(i).getEndPoint(), dRoute.get(i).getEndPoint());
				    	//it can be empty, R, L, RU, RD, LU, LD, U, D
				    	Vector newVector = null;
				    	Vector additionalVector = null;
				    	Point newP = null;
				    	switch(sides) {
				    		case "R": 
				    			//cut right
				    			newVector = oRoute.get(i).getHardRight();
				    		case "L": 
				    			//cut left
				    			newVector = oRoute.get(i).getHardLeft();
				    		case "U": 
				    			//continue to run forward or stop or stop-and-go or cut;
				    			//forward
				    			newVector = oRoute.get(i);
				    			//stop
				    			newVector = oRoute.get(i).getNegative();
				    			newVector = newVector.scalarMultiple((int)Math.ceil((1/newVector.getDistance()))); 
				    			//and go
				    			newP = new Point(newVector.getEndPoint()); //end point of the new vector
				    			additionalVector = new Vector(newP, 
				    					(int) Math.round(newVector.getEndPoint().getX()),
				    					(int) Math.round(newP.y+newVector.getDistance())); //straight up
				    		case "D": 
				    			//stop or stop-and-go or cut;
				    			//stop
				    			newVector = oRoute.get(i).getNegative();
				    			newVector = newVector.scalarMultiple((int)Math.ceil((1/newVector.getDistance()))); 
				    			//and go
				    			newP = new Point(newVector.getEndPoint());
				    			additionalVector = new Vector(newP, 
				    					(int) Math.round(newVector.getEndPoint().getX()),
				    					(int)Math.round(newP.y+newVector.getDistance()));
				    			//cut right
				    			newVector = newVector.getHardRight();
				    			//cut left
				    			newVector = newVector.getHardLeft();
				    		case "LU": 
				    			//go further to the up left and up or straight up;
				    			//left
				    			newVector = oRoute.get(i).getHardLeft().getFourtyFive();
				    			//up
				    			newP = new Point(newVector.getEndPoint());
				    			additionalVector = new Vector(newP, 
				    					(int) Math.round(newVector.getEndPoint().getX()),
				    					(int)Math.round(newP.y+newVector.getDistance()));
				    			//straight up
				    			newVector = oRoute.get(i).getStraightUp();
				    		case "LD": 
				    			//stop or stop-and-go or cut hard left or cut down left;
				    			//stop
				    			newVector = oRoute.get(i).getNegative();
				    			newVector = newVector.scalarMultiple((int)Math.ceil((1/newVector.getDistance())));
				    			//and go
				    			newP = new Point(newVector.getEndPoint());
				    			additionalVector = new Vector(newP, 
				    					(int) Math.round(newVector.getEndPoint().getX()),
				    					(int)Math.round(newP.y+newVector.getDistance()));
				    			//cut left
				    			newVector = oRoute.get(i).getHardLeft(); 
				    			//down left
				    			newVector = newVector.getHardLeft().getFourtyFive();
				    		case "RU": 
				    			//go further to the up right and up or straight up;
				    			//right
				    			newVector = oRoute.get(i).getHardRight().getThreeFifteen();
				    			//up
				    			newP = new Point(newVector.getEndPoint());
				    			additionalVector = new Vector(newP, 
				    					(int) Math.round(newVector.getEndPoint().getX()),
				    					(int)Math.round(newP.y+newVector.getDistance()));
				    			//straight up
				    			newVector = oRoute.get(i).getStraightUp();
				    		case "RD": ;
				    			//stop or stop-and-go or cut hard right or cut down left;
				    			//stop
				    			newVector = oRoute.get(i).getNegative();
				    			newVector = newVector.scalarMultiple((int)Math.ceil((1/newVector.getDistance())));
				    			//and go
				    			newP = new Point(newVector.getEndPoint());
				    			additionalVector = new Vector(newP, 
				    					(int) Math.round(newVector.getEndPoint().getX()),
				    					(int)Math.round(newP.y+newVector.getDistance()));
				    			//cut right
				    			newVector = oRoute.get(i).getHardRight(); 
				    			//down right
				    			newVector = newVector.getHardRight().getThreeFifteen();
				    	}
				    	improvedRoute.add(newVector);
				    	if(additionalVector!=null) {
				    		improvedRoute.add(additionalVector);
				    	}
				    }
				}
				for(int i=1; i<improvedRoute.size(); i++) {
					improvedRoute.get(i).setBeginningPoint(improvedRoute.get(i-1).getEndPoint());
			    	if(improvedRoute.get(i).getEndPoint().x < 0) {
			    		improvedRoute.get(i).setEndPointX(0);
			    	}
			    	if(improvedRoute.get(i).getEndPoint().y < 0) {
			    		improvedRoute.get(i).setEndPointY(0);
			    	}
				}
				newPlayMap.put(oPlayer, new Route(improvedRoute));
			}
			//if they are within x distance of them then make it a stop or stop-and-go much earlier when they are farther from them
			recoPlay = new OffensePlay(newPlayMap);
		}
	}
	
	private double pointDist(Point p1, Point p2) { //finds the shortest distance between two points
		double x1 = p1.x;
		double x2 = p2.x;
		double y1 = p1.y;
		double y2 = p2.y;
		double xDist = Math.pow(x1-x2, 2);
		double yDist = Math.pow(y1-y2, 2);
		return Math.sqrt(xDist+yDist);
	}
	
	//returns the side the player is on compared to defender (L v R, U v D) if they're far enough
	private String whichSide(Point p1, Point p2) { 
		String sides = "";
		if(p1.x-p2.x < 0) {
			sides += 'R';
		}
		else {
			sides += 'L';
		}
		if(p1.x-p2.x<CONSTANT_OF_SEPARATION) {
			sides = "";
		}
		if(p1.y-p2.y < 0) {
			sides += 'U';
		}
		else {
			sides += 'D';
		}
		if(p1.y-p2.y < CONSTANT_OF_SEPARATION) {
			sides = sides.substring(0, sides.length());
		}
		return sides;
	}
	
	private void drawField() {
		StdDraw.setCanvasSize(330, 900);
		StdDraw.setPenColor(0, 102, 51);
		StdDraw.filledRectangle(83, 250, 83, 250);
		StdDraw.setPenColor(101, 67, 33);
		StdDraw.filledEllipse(0.5, 0.5, 0.5, 0.1);
		StdDraw.setPenColor(StdDraw.WHITE);
		for(int i=0; i<=10; i++) {
			StdDraw.line(0, i/10.0, 1, i/10.0);
		}
		for(int j=3; j<=7; j++) {
			StdDraw.line(.4, j/10, .6, j/10);
		}
	}
	
	private void drawOriginal() {
		for(Player p : currPlay.getPlay().keySet()) {
			for (Vector v : currPlay.getPlay().get(p).getRoute()){
				StdDraw.setPenRadius(.005);
				StdDraw.setPenColor(StdDraw.RED);
				double[] line = v.drawVector(.05, .02);
				StdDraw.line(line[0], line[1], line[2], line[3]);
				StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
				StdDraw.setPenRadius(.01);
				StdDraw.point(line[2], line[3]);
			}
		}
	}
	
	private void drawDefensiveBacks(boolean provided) {
		if(provided) {
			expectedPlay = makeDefensePlay(userDefensePlay);
		}
		for(Player p : expectedPlay.getPlay().keySet()) {
			for (Vector v : expectedPlay.getPlay().get(p).getRoute()){
				StdDraw.setPenRadius(.005);
				StdDraw.setPenColor(StdDraw.BLACK);
				double[] line = v.drawVector(.05, .02);
				StdDraw.line(line[0], line[1], line[2], line[3]);
				StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
				StdDraw.setPenRadius(.01);
				StdDraw.point(line[2], line[3]);
			}
		}
	}
	
	private void drawNew() {
		for(Player p : recoPlay.getPlay().keySet()) {
			for (Vector v : recoPlay.getPlay().get(p).getRoute()){
				StdDraw.setPenRadius(.005);
				StdDraw.setPenColor(StdDraw.BLUE);
				double[] line = v.drawVector(.05, .02);
				StdDraw.line(line[0], line[1], line[2], line[3]);
				StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
				StdDraw.setPenRadius(.01);
				StdDraw.point(line[2], line[3]);
			}
		}
	}
	
	private DefensePlay makeDefensePlay(List<String> makePlay) {
		for(String play : makePlay) {
			String[] splitLine = play.split(":"); //splits name, vectors
			String[] splitVectors = splitLine[1].split(","); //splits the list of vectors into each other
			for(int i=0; i<splitVectors.length; i++) {
				splitVectors[i] = splitVectors[i].trim();
			}
			List<Vector> vectors = new ArrayList<Vector>();
			for(String x : splitVectors) {
				String[] splitPoint = x.split(" "); //splits every vector in the list into its point components
				Point p = new Point(Integer.parseInt(splitPoint[0]), Integer.parseInt(splitPoint[1])); //makes point
				Vector v = new Vector(p, Integer.parseInt(splitPoint[2]), Integer.parseInt(splitPoint[3])); //makes vector
				vectors.add(v); //creates a vector and adds it to the list
			}
			Route newRoute = new Route(vectors); //vectors, name
			//NEED TO SPEED THIS UP. CANT GO THROUGH EVERY SINGLE PLAYER
			for(Player p : defensePlayers) { //repeats for defensive players
				if(splitLine[0].equals(p.getName())) {
					dPlayerToRoute.put(p, newRoute);
					p.setCurrRoute(newRoute); //update the player to the route they ran
				}
			}
		}
		return new DefensePlay(dPlayerToRoute);
	}
	
	private void writeToFile() {
		try {
			bw.newLine();
			for(Player p : playerList) {
				String s = p.getName() + ":";
				for(Vector v : p.currRoute().getRoute()) {
					s+= " " + v.getBeginningPoint().x + " " + v.getBeginningPoint().y 
							+ " " + v.getXDistance() + " " + v.getYDistance() + ",";
				}
				s = s.substring(0, s.length()-1);
				bw.write(s);
				bw.newLine();
			}
			bw.append("EOP");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
