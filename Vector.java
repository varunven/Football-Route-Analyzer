import java.util.*;
import java.awt.*;
import java.math.*;
public class Vector {
	private int x;
	private int y;
	private Point p;
	
	public Vector(Point p, int x, int y) {
		this.x=x;
		this.y=y;
		this.p=p;
	}
	
	public double getDistance() {
		return Math.pow(Math.pow(x, 2)+Math.pow(x, 2), .5);
	}
	
	public Point getBeginningPoint() {
		return p;
	}
	
	public void setBeginningPoint(Point p) {
		this.p = p;
	}
	
	public Point getEndPoint() {
		Point end = new Point(p.x+x, p.y+y);
		return end;
	}
	
	public void setEndPointX(int x) {
		this.x = p.x+x;
	}
	
	public void setEndPointY(int y) {
		this.y = p.y+y;
	}
	
	public int getXDistance() {
		return x;
	}
	
	public int getYDistance() {
		return y;
	}
	
	public double getAngle() {
		return Math.atan((y-p.y)/(x-p.x));
	}
	
	public int distance(Point p) {
		return Math.max(getNormalRight(p).yards(), getNormalLeft(p).yards());
	}
	
	public Vector add(Vector v) {
		x=x+v.x;
		y=y+v.y;
		return new Vector(p, x, y);
	}
	
	public int yards() {
		return (int)Math.round((Math.sqrt(x*x +y*y)));
	}
	
	public Vector scalarMultiple(int a) {
		return new Vector(p, x*a, y*a);
	}
	
	public Vector getNormalRight(Point p){
		int newX=y;
		int newY=x;
		Vector v = new Vector(p, newX, newY);
		return v;
	}
	
	public Vector getNormalLeft(Point p) {
		int newX=y;
		int newY=x;
		Vector v = new Vector(p, -newX, -newY);
		return v;
	}
	
	public Vector getNegative() {
		return new Vector(p, -x, -y);
	}
	
	public Vector getFourtyFive() {
		int newX=(int)(y*Math.cos(Math.PI/4));
		int newY=(int)(x*Math.sin(Math.PI/4));
		Vector v = new Vector(p, newX, newY);
		return v;
	}
	
	public Vector getThreeFifteen() {
		int newX=(int)(y*Math.cos(7*Math.PI/4));
		int newY=(int)(x*Math.sin(7*Math.PI/4));
		Vector v = new Vector(p, newX, newY);
		return v;
	}
	
	public Vector getHardRight() {
		Vector v = new Vector(p, p.x, p.y+this.getYDistance());
		return v;
	}
	
	public Vector getHardLeft() {
		Vector v = new Vector(p, p.x, p.y-this.getYDistance());
		return v;
	}
	
	public Vector getStraightUp() {
		Vector v = new Vector(p, p.x+this.getXDistance(), p.y);
		return v;
	}
	
	public double[] drawVector(double xsf, double ysf) {
		double[] pArr = {this.getBeginningPoint().x*xsf, this.getBeginningPoint().y*ysf,
				this.getEndPoint().x*xsf, this.getEndPoint().y*ysf};
		return pArr;
	}
}