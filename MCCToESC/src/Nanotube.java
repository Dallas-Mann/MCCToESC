import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Nanotube{
	// constants
	public static final double VANDER_WAALS_GAP = Math.pow(10, -9) * 0.34;
	public static final double distanceBetweenShells = VANDER_WAALS_GAP;
	
	private List<Shell> shells;
	private double diameterInnermostShell;	
	protected double diameterOutermostShell;
	protected double meanFreePath;
	protected int numberOfShells;
	protected int numberOfSections = 200;
	
	private double distanceToGroundPlane;
	
	public Nanotube(int numberOfShells, double diameterInnermostShell, double distanceToGroundPlane){
		this.numberOfShells = numberOfShells;
		this.diameterInnermostShell = diameterInnermostShell;
		this.distanceToGroundPlane = distanceToGroundPlane;
		shells = new ArrayList<Shell>();
		
		calculateOutermostShellDiameter();
		calculateMeanFreePath();	
		constructNanotube();
	}
	
	private void calculateOutermostShellDiameter(){
		diameterOutermostShell = diameterInnermostShell + (numberOfShells * distanceBetweenShells);
	}
	
	private void calculateMeanFreePath(){
		meanFreePath = diameterOutermostShell * 1000;
	}
	
	private void constructNanotube(){
		
		double diameterCurrentShell = diameterInnermostShell;
		
		for(int currentShell = 1; currentShell <= numberOfShells; currentShell++){
			shells.add(new Shell(diameterCurrentShell, currentShell, this));
			diameterCurrentShell += distanceBetweenShells;
		}
	}
	
	// wrapper method so you don't have to pass in 0 as a parameter
	public double sumQuantumCapacitance(){
		return sumQuantumCapacitance(0);
	}
	
	// used to convert bridge of capacitances to equivalent capacitance recursively
	// this is used when converting from the MCC to the ESC model
	public double sumQuantumCapacitance(int index){
		Shell temp = shells.get(index);
		if(temp.currentShell == numberOfShells){
			return 1.0/((1.0/temp.quantumCapacitance) + (1.0/temp.electrostaticCapacitance));
		}
		else
			return 1.0/((1.0/temp.quantumCapacitance) + (1.0/temp.electrostaticCapacitance)) + sumQuantumCapacitance(index + 1);
	}
	
	public void printMCC(PrintWriter writer){
		writer.println("MCC Model");
		
		for(Shell s : shells){
			writer.println("Shell number: " + s.currentShell);
			writer.println("Contact Quantum Resistance (Ohms per micrometer): " + s.contactQuantumResistance);
			writer.println("Scattering Resistance: (Ohms per micrometer)" + s.scatteringResistance);
			writer.println("Kinetic Inductance: " + s.kineticInductance);
			writer.println("Quantum Capacitance: " + s.quantumCapacitance);
			writer.println("Electrostatic Capacitance: " + s.electrostaticCapacitance);
			writer.println("Number of Conducting Channels: " + s.numConductingChannels);
			writer.println("Inner Shell Conductance: " + s.innerShellConductance);
			writer.println("Shell Diameter: " + s.shellDiameter);
			writer.println();
		}
	}
	
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
		imperfectContactResistance = 1.0/imperfectContactResistance;
		contactQuantumResistance = 1.0/contactQuantumResistance;
		
		//outoput these values per micrometer
		//need to divide by number of sections to get section parameters for the netlist
		scatteringResistance = 1.0/scatteringResistance;
		kineticInductance = 1.0/kineticInductance;
		double quantumCapacitance = sumQuantumCapacitance();
		double electrostaticCapacitance = shells.get(numberOfShells - 1).electrostaticCapacitance;
		
		writer.println("ESC Model");
		writer.println("Scattering Resistance: " + scatteringResistance);
		writer.println("Contact Quantum Resistance: " + contactQuantumResistance);
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
			
			
			escNetlist.println("R" + numRes + " " + nodeOne + " " + nodeTwo + " " + (scatteringResistance/numberOfSections));
			
			escNetlist.println("L" + numInd + " " + nodeTwo + " " + (nodeTwo + 1) + " " + (kineticInductance/numberOfSections));
						
			escNetlist.println("C" + numCap + " " + (nodeTwo + 1) + " " + (nodeTwo + 2) + " " + (quantumCapacitance/numberOfSections));
			
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
