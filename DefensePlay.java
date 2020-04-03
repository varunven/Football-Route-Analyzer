import java.util.*;
import java.util.List;
import java.util.Map;
public class DefensePlay{
	Map<Player, Route> defensePlayersRoutes;
	
	public DefensePlay(Map<Player, Route> map){
		defensePlayersRoutes = map;
	}
	public Map<Player, Route> getPlay() {
		return defensePlayersRoutes;
	}
	public Map<Player, List<Vector>> getRoutes(){
		Map<Player, List<Vector>> playMap = new HashMap<>();
		for(Player player : defensePlayersRoutes.keySet()) {
			playMap.put(player, defensePlayersRoutes.get(player).getRoute());
		}
		return playMap;
	}
}