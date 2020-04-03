import java.io.*; 
import java.util.*;

public class WideReceiver implements Player{
	private String name;
	private String teamName;
	private Route lastRoute;
	private Route currRoute;
	
	public WideReceiver(String name, String teamName){
		this.name=name;
		this.teamName=teamName;
		lastRoute=null;
	}
	
	public String getName(){ 
		return name;
	}
	
	public String getPosition(){ 
		return "Wide Receiver";
	}
	
	public String getTeam(){ 
		return teamName;
	}
	
	public String getSide() {
		return "Offense";
	}
	
	public int yardsLastRoute(){ 
		if(lastRoute!=null) {
			return lastRoute.getYards();
		}
		return 0;
	}
	
	public Route lastRoute(){ 
		return lastRoute;
	}
	
	public Route currRoute() {
		return currRoute;
	}
	
	public List recoRoute(Map<DefensePlay, Boolean> defendersRoutes){
		List<DefensePlay> playList = new ArrayList<>();
		for(DefensePlay play: defendersRoutes.keySet()) {
			if(defendersRoutes.get(play)) {
				playList.add(play);
			}
		}
		//playList is all the defensive routes that were won against
		return playList;
	} 
	
	public void setCurrRoute(Route a) {
		lastRoute = currRoute;
		currRoute = a;
	}
	
	public void setTeam(String team) {
		teamName = team;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}