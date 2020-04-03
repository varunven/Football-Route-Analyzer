import java.io.*; 
import java.util.*;

public class DefensiveBack implements Player{
	private String name;
	private String teamName;
	private Route lastRoute;
	private Route currRoute;
	
	public DefensiveBack(String name, String teamName){
		this.name=name;
		this.teamName=teamName;
		lastRoute=null;
	}
	
	public String getName(){ 
		return name;
	}
	
	public String getPosition(){ 
		return "Defensive Back";
	}
	
	public String getTeam(){ 
		return teamName;
	}

	public String getSide() {
		return "Defense";
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