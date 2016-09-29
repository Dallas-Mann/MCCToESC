
public class MCCToESC{
	public static void main(String[] args){
		try{
			int numberOfShells = Integer.parseInt(args[0]);
			double diameterInnermostShell = Double.parseDouble(args[1]);
			double distanceToGroundPlane = Double.parseDouble(args[2]);
			String outputFileName = args[3];
			
			Nanotube nanotube = new Nanotube(numberOfShells, diameterInnermostShell, distanceToGroundPlane);
			nanotube.printMCC(outputFileName);
			nanotube.printESC(outputFileName);
		}
		catch(Exception e){
			usage();
		}
	}
	
	public static void usage(){
		System.out.println("Correct Usage: MCCToESC.java <number of shells> <diameter of innermost shell> "
				+ "<distance to ground plane> <filename.txt>");
	}
}
