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
	
	private int numberOfShells;
	private double diameterInnermostShell;
	private double distanceToGroundPlane;
	
	private double diameterOutermostShell;
	private double meanFreePath;
	private double groundCapacitance;
	
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
			shells.add(new Shell(diameterCurrentShell, meanFreePath, currentShell, numberOfShells, distanceBetweenShells, diameterOutermostShell));
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
		if(temp.currentShell == temp.numberOfShells){
			return 1.0/((1.0/temp.quantumCapacitance) + (1.0/temp.electrostaticCapacitance));
		}
		else
			return 1.0/((1.0/temp.quantumCapacitance) + (1.0/temp.electrostaticCapacitance)) + sumQuantumCapacitance(index + 1);
	}
	
	public void printESC(PrintWriter writer) throws FileNotFoundException{
		double scatteringResistanceTotal = 0;
		double contactQuantumResistanceTotal = 0;
		double kineticInductanceTotal = 0;
		double tempResistance = 0;
		double totalInnerShellConductance = 0;
		for(Shell s : shells){
			scatteringResistanceTotal += s.scatteringResistance;
			contactQuantumResistanceTotal += s.contactQuantumResistance;
			kineticInductanceTotal += s.kineticInductance;
			
			if(s.innerShellConductance != 0){
				tempResistance = 1.0/(s.innerShellConductance);
				totalInnerShellConductance += tempResistance;
			}
		}
		double innerShellConductanceToResistance = totalInnerShellConductance;
		totalInnerShellConductance = 1.0/totalInnerShellConductance;
		
		double quantumCapacitanceTotal = sumQuantumCapacitance();
		double electrostaticCapacitance = shells.get(numberOfShells - 1).electrostaticCapacitance;
		
		writer.println("ESC Model");
		writer.println("Scattering Resistance: " + scatteringResistanceTotal);
		writer.println("Contact Quantum Resistance: " + contactQuantumResistanceTotal);
		writer.println("Kinetic Inductance: " + kineticInductanceTotal);
		writer.println("Quantum Capacitance: " + quantumCapacitanceTotal);
		writer.println("Electrostatic Capacitance: " + electrostaticCapacitance);
		writer.println("Total Inner Shell Conductance: " + totalInnerShellConductance);
		writer.println();
		
		PrintWriter escNetlist = new PrintWriter(new File("escNetlist.txt"));
		int numNodes = 6;
		int nodeOffset = 0;
		int nodeOne = 0;
		int nodeTwo = 1;
		
		int numRes = 0;
		int numCap = 0;
		int numInd = 0;
		for(int i = 0; i < 200; i++){
			nodeOne = i;
			nodeTwo = i+1;
			nodeOffset = i * numNodes;
			
			escNetlist.println("R" + numRes + " " + (nodeOffset+1) + " " + (nodeOffset+2) + " " + contactQuantumResistanceTotal/2.0);
			
			escNetlist.println("R" + (numRes+1) + " " + (nodeOffset+2) + " " + (nodeOffset+3) + " " + scatteringResistanceTotal/2.0);
			
			escNetlist.println("L" + numInd + " " + (nodeOffset+3) + " " + (nodeOffset+4) + " " + kineticInductanceTotal);
			
			escNetlist.println("R" + (numRes+2) + " " + (nodeOffset+4) + " " + 0 + " " + innerShellConductanceToResistance);
			
			escNetlist.println("C" + numCap + " " + (nodeOffset+4) + " " + (nodeOffset+5) + " " + contactQuantumResistanceTotal);
			
			escNetlist.println("C" + (numCap+1) + " " + (nodeOffset+5) + " " + 0 + " " + electrostaticCapacitance);
			
			escNetlist.println("R" + (numRes+3) + " " + (nodeOffset+4) + " " + (nodeOffset+6) + " " + contactQuantumResistanceTotal/2.0);
			
			escNetlist.println("R" + (numRes+4) + " " + (nodeOffset+6) + " " + (nodeOffset+7) + " " + scatteringResistanceTotal/2.0);
			
			numRes += 5;
			numCap += 2;
			numInd += 1;
		}
		escNetlist.println(".end");
		escNetlist.close();
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
}
