import java.io.File;
import java.io.PrintWriter;

public class MCCToESC{
	public static void main(String[] args){
		try{
			int numberOfShells = Integer.parseInt(args[0]);
			double diameterInnermostShell = Double.parseDouble(args[1]);
			double distanceToGroundPlane = Double.parseDouble(args[2]);
			String outputFileName = args[3];
			
			Nanotube nanotube = new Nanotube(numberOfShells, diameterInnermostShell, distanceToGroundPlane);
			PrintWriter writer = new PrintWriter(new File(outputFileName));
			nanotube.printMCC(writer);
			nanotube.printESC(writer);
			writer.close();
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
