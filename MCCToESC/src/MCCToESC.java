import java.io.File;
import java.io.PrintWriter;

public class MCCToESC{
	public static void main(String[] args){
		try{
			int numberOfShells = Integer.parseInt(args[0]);
			double diameterInnermostShell = convert(args[1]);
			double distanceToGroundPlane = convert(args[2]);
			double lengthOfNanotube = convert(args[3]);
			String outputFileName = args[4];
			
			
			Nanotube nanotube = new Nanotube(numberOfShells, diameterInnermostShell, distanceToGroundPlane, lengthOfNanotube);
			PrintWriter writer = new PrintWriter(new File(outputFileName));
			nanotube.printESC(writer);
			nanotube.printMCC(writer);
			writer.close();
		}
		catch(Exception e){
			System.out.println(e);
			usage();
		}
	}
	
	public static void usage(){
		System.out.println("Correct Usage: MCCToESC.java <number of shells> <diameter of innermost shell> "
				+ "<distance to ground plane> <filename.txt>");
	}
	
	private static double convert(String token){
		double baseNum;
		int indexOfModifier = token.length();
		for(int i = 0; i < token.length(); i++){
			char temp = token.charAt(i);
			if(temp != 'E' && Character.isAlphabetic(temp)){
				indexOfModifier = i;
				break;
			}
		}
		String modifier = token.substring(indexOfModifier);
		String value = token.substring(0, indexOfModifier);
		baseNum = Double.parseDouble(value);
		//there should be a trailing modifier after the number
		/*
		F	E-15	femto
		P	E-12	pico
		N	E-9		nano
		U	E-6		micro
		M	E-3		milli
		K	E+3		kilo
		MEG E+6 	mega
		G 	E+9 	giga
		T 	E+12 	tera
		 */
		switch(modifier){
			case "f":
				return baseNum *= Math.pow(10, -15);
			case "p":
				return baseNum *= Math.pow(10, -12);
			case "n":
				return baseNum *= Math.pow(10, -9);
			case "u":
				return baseNum *= Math.pow(10, -6);
			case "m":
				return baseNum *= Math.pow(10, -3);
			case "k":
				return baseNum *= Math.pow(10, 3);
			case "meg":
				return baseNum *= Math.pow(10, 6);
			case "g":
				return baseNum *= Math.pow(10, 9);
			case "t":
				return baseNum *= Math.pow(10, 12);
			default:
				try{
					if(token.chars().allMatch(Character::isDigit)){
						baseNum = Double.parseDouble(token);
					}
				}
				catch(Exception e){
				}
				return baseNum;	
		}
	}
}
