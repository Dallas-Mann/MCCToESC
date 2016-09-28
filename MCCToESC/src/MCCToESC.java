
public class MCCToESC{
	public static void main(String[] args){
		Nanotube nanotube = new Nanotube(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
		nanotube.printMCC();
		nanotube.printESC();
	}
}
