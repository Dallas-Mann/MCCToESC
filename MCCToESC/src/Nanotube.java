import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Nanotube{
	//constants
	public static final double VANDER_WAALS_GAP = Math.pow(10, -9) * 0.34;
	public static final double distanceBetweenShells = VANDER_WAALS_GAP;
	//passed in values
	protected int numberOfShells;
	private double diameterInnermostShell;
	protected double distanceToGroundPlane;
	private double lengthOfNanotube;
	//instance variables
	private List<Shell> shells;
	protected double diameterOutermostShell;
	protected double meanFreePath;
	//TODO actually calculate the number of sections required
	protected int numberOfSections = 200;
	
	public Nanotube(int numberOfShells, double diameterInnermostShell, double distanceToGroundPlane, double lengthOfNanotube){
		this.numberOfShells = numberOfShells;
		this.diameterInnermostShell = diameterInnermostShell;
		this.distanceToGroundPlane = distanceToGroundPlane;
		this.lengthOfNanotube = lengthOfNanotube;
		shells = new ArrayList<Shell>();
		diameterOutermostShell = calculateOutermostShellDiameter();
		meanFreePath = calculateMeanFreePath();	
		constructNanotube();
	}
	
	private double calculateOutermostShellDiameter(){
		return diameterInnermostShell + ((numberOfShells - 1) * 2 * distanceBetweenShells);
	}
	
	private double calculateMeanFreePath(){
		return diameterOutermostShell * 1000;
	}
	//TODO calculate number of sections for a given nanotube length
	private int calculateNumberOfSections(double length){
		//typically 200 per micrometer
		return (int) Math.round(length * 200 / Math.pow(10, -9));
	}
	
	private void constructNanotube(){
		double diameterCurrentShell = diameterInnermostShell;
		
		for(int currentShell = 1; currentShell <= numberOfShells; currentShell++){
			shells.add(new Shell(diameterCurrentShell, currentShell, this));
			diameterCurrentShell += 2 * distanceBetweenShells;
		}
	}
	
	//wrapper method so you don't have to pass in 0 as a parameter
	private double sumQuantumCapacitance(){
		//starts at innermost shell and works its way out
		return sumQuantumCapacitance(0);
	}
	
	//used to convert bridge of capacitances to equivalent capacitance recursively
	//this is used when converting from the MCC to the ESC model
	private double sumQuantumCapacitance(int index){
		Shell temp = shells.get(index);
		if(temp.currentShell == numberOfShells){
			return temp.quantumCapacitance;
		}
		else{
			return 1.0/((1.0/temp.quantumCapacitance)+(1.0/(temp.electrostaticCapacitance + sumQuantumCapacitance(index + 1))));
		}
		/*
		if(temp.currentShell == numberOfShells){
			return 1.0/((1.0/temp.quantumCapacitance) + (1.0/temp.electrostaticCapacitance));
		}
		else
			return 1.0/((1.0/temp.quantumCapacitance) + (1.0/temp.electrostaticCapacitance)) + sumQuantumCapacitance(index + 1);
		 */
	}
	
	public void printMCC(PrintWriter writer){
		writer.println("MCC Model");
		
		for(Shell s : shells){
			writer.println("Shell number: " + s.currentShell);
			writer.println("Shell Diameter: " + s.shellDiameter);
			writer.println("Number of Conducting Channels: " + s.numConductingChannels);
			writer.println("Contact Quantum Resistance (Ohms per meter): " + s.contactQuantumResistance);
			writer.println("Scattering Resistance (Ohms per meter): " + s.scatteringResistance);
			writer.println("Kinetic Inductance (Henry per meter): " + s.kineticInductance);
			writer.println("Quantum Capacitance (Farads per meter): " + s.quantumCapacitance);
			writer.println("Electrostatic Capacitance: " + s.electrostaticCapacitance);
			writer.println("Inner Shell Conductance: " + s.innerShellConductance);
			writer.println();
		}
	}
	
	//convert SI units to section length parameters (multiply by length / num sections)
	public void printESC(PrintWriter writer) throws FileNotFoundException{
		double imperfectContactResistance = 0;
		double scatteringResistance = 0;
		double contactQuantumResistance = 0;
		double kineticInductance = 0;
		
		//sum resistances and inductance in parallel, because the shells are in parallel
		for(Shell s : shells){
			imperfectContactResistance += 1.0/s.imperfectContactResistance;
			scatteringResistance += 1.0/s.scatteringResistance;
			contactQuantumResistance += 1.0/s.contactQuantumResistance;
			kineticInductance += 1.0/s.kineticInductance;
		}
		//final inversion, to sum in parallel
		//example: 1/R = 1/r1 + 1/r2
		imperfectContactResistance = (1.0/imperfectContactResistance);
		contactQuantumResistance = (1.0/contactQuantumResistance);
		scatteringResistance = (1.0/scatteringResistance);
		kineticInductance = (1.0/kineticInductance);
		double quantumCapacitance = sumQuantumCapacitance();
		double electrostaticCapacitance = shells.get(numberOfShells - 1).electrostaticCapacitance;
		
		writer.println("ESC Model");
		writer.println("Imperfect Contact Resistance: " + imperfectContactResistance);
		writer.println("Contact Quantum Resistance: " + contactQuantumResistance);
		writer.println("");
		writer.println("SI Unit Parameters:");
		writer.println("Scattering Resistance: " + scatteringResistance);
		writer.println("Kinetic Inductance: " + kineticInductance);
		writer.println("Quantum Capacitance: " + quantumCapacitance);
		writer.println("Electrostatic Capacitance: " + electrostaticCapacitance);
		
		scatteringResistance = scatteringResistance*(lengthOfNanotube/numberOfSections);
		kineticInductance = kineticInductance*(lengthOfNanotube/numberOfSections);
		quantumCapacitance = quantumCapacitance*(lengthOfNanotube/numberOfSections);
		electrostaticCapacitance = electrostaticCapacitance*(lengthOfNanotube/numberOfSections);
		writer.println("");
		writer.println("Per Section Parameters:");
		writer.println("Scattering Resistance: " + scatteringResistance);
		writer.println("Kinetic Inductance: " + kineticInductance);
		writer.println("Quantum Capacitance: " + quantumCapacitance);
		writer.println("Electrostatic Capacitance: " + electrostaticCapacitance);
		writer.println();
		
		PrintWriter escNetlist = new PrintWriter(new File("escNetlist.txt"));
		int startNode = 3;
		int nodeOne = 0;
		int nodeTwo  = 0;
		//start at resistor number 5, 4 resistances already placed in netlist
		int numRes = 4;
		int numInd = 0;
		int numCap = 0;

		
		escNetlist.println("R0" + " " + 1 + " " + 2 + " " + contactQuantumResistance/2.0);
		escNetlist.println("R1" + " " + 2 + " " + 3 + " " + imperfectContactResistance/2.0);
		
		for(int i = 0; i < 200; i++){
			if(i == 0){
				nodeOne = 3;
				nodeTwo = 4;
			}
			else{
				nodeOne = (3 * i) + 2;
				nodeTwo = (3 * i) + 4;
			}
			
			
			escNetlist.println("R" + numRes + " " + nodeOne + " " + nodeTwo + " " + scatteringResistance);
			
			escNetlist.println("L" + numInd + " " + nodeTwo + " " + (nodeTwo + 1) + " " + kineticInductance);
						
			escNetlist.println("C" + numCap + " " + (nodeTwo + 1) + " " + (nodeTwo + 2) + " " + quantumCapacitance);
			
			escNetlist.println("C" + (numCap+1) + " " + (nodeTwo + 2) + " " + 0 + " " + electrostaticCapacitance);
			
			numCap += 2;
			numInd += 1;
			numRes += 1;
		}
		
		escNetlist.println("R2" + " " + (nodeTwo + 1) + " " + (nodeTwo + 3) + " " + contactQuantumResistance/2.0);
		escNetlist.println("R3" + " " + (nodeTwo + 3) + " " + (nodeTwo + 4) + " " + imperfectContactResistance/2.0);
		
		escNetlist.println(".end");
		escNetlist.close();
	}
}
