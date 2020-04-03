import java.awt.Point;
import java.util.*;
//DONE
public class Route {
	private List<Vector> route;
	private Point ending;
	private String opposition;
	public Route(List<Vector> route) {
		this.route=route;
		if(route.size()>0) {
			Vector v = route.get(route.size()-1);
			ending = v.getEndPoint();
		}
	}

	public List<Vector> getRoute(){
		return route;
	}
	
	public int getYards() {
		int i=0;
		for(Vector x : route) {
			i+=x.yards();
		}
		return i;
	}
	
	public String getOpponent() {
		return opposition;
	}
	
	public Point getEnd() {
		return ending;
	}
	
	public List<String> routeString() {
		List<String> vectors = new ArrayList<>();
		for(Vector v : route) {
			vectors.add(v.getBeginningPoint().x+" "+v.getBeginningPoint().y+" "+v.getXDistance()+" "+v.getYDistance());
		}
		return vectors;
	}
}