
public class MCCToESC{
	public static void main(String[] args){
		try{
			Nanotube nanotube = new Nanotube(Integer.parseInt(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
			nanotube.printMCC();
			nanotube.printESC();
		}
		catch(Exception e){
			usage();
		}
	}
	
	public static void usage(){
		System.out.println("Correct Usage: MCCToESC.java <number of shells> <diameter of innermost shell> <distance to ground plane>");
	}
}
