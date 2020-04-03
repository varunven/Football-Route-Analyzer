import java.util.*;
import java.awt.*;
import java.io.*;
public class FootballTester {
	public static void main(String[] args) {
		String f_name = "Football Test.txt";
		FootballField field = new FootballField(f_name);
		//OffensePlay x = field.previousPlay(); tests previous play when no plays commenced
		//DefensePlay y = field.expectedPlay(); tests expected play when no plays commenced
		field.commencePlay();
	}
}