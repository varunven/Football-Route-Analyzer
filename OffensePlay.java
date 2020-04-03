//DONE
import java.util.*;
public class OffensePlay{
	private Map<Player, Route> offensePlayersRoutes;
	
	public OffensePlay(Map<Player, Route> map){
		offensePlayersRoutes = map;
	}
	public Map<Player, Route> getPlay() {
		return offensePlayersRoutes;
	}
	public Map<Player, List<Vector>> getRoutes(){
		Map<Player, List<Vector>> playMap = new HashMap<>();
		for(Player player : offensePlayersRoutes.keySet()) {
			playMap.put(player, offensePlayersRoutes.get(player).getRoute());
		}
		return playMap;
	}
}