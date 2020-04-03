import java.util.*;
import java.io.*;
public interface Player {
	//implemented in the following classes:
	//WR, RB, FB, QB, TE, OT, OG, C 
	//DE, DT, CB, S (can be strong or fast), MLB, OLB
	public String getName();
	public String getPosition();
	public String getTeam();
	public int yardsLastRoute();
	public Route lastRoute();
	public Route currRoute();
	public void setCurrRoute(Route a);
	public void setTeam(String team);
	public void setName(String name);
}