
public class main {
	public static void Main(String[] args){
		Nanotube nanotube = new Nanotube(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
		nanotube.printMCC();
		nanotube.printESC();
	}
}
