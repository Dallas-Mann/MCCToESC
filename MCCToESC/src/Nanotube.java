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
	
	public void printESC(PrintWriter writer){
		double scatteringResistanceTotal = 0;
		double contactQuantumResistanceTotal = 0;
		double kineticInductanceTotal = 0;
		for(Shell s : shells){
			scatteringResistanceTotal += s.scatteringResistance;
			contactQuantumResistanceTotal += s.contactQuantumResistance;
			kineticInductanceTotal += s.kineticInductance;
		}
		
		double quantumCapacitanceTotal = sumQuantumCapacitance();
		double electrostaticCapacitance = shells.get(numberOfShells - 1).electrostaticCapacitance;
		
		writer.println("ESC Model");
		writer.println("Scattering Resistance: " + scatteringResistanceTotal);
		writer.println("Contact Quantum Resistance: " + contactQuantumResistanceTotal);
		writer.println("Kinetic Inductance: " + kineticInductanceTotal);
		writer.println("Quantum Capacitance: " + quantumCapacitanceTotal);
		writer.println("Electrostatic Capacitance: " + electrostaticCapacitance);
		writer.println();
		
	}
	
	public void printMCC(PrintWriter writer){
		writer.println("MCC Model");
		for(Shell s : shells){
			writer.println("Shell number: " + s.currentShell);
			writer.println("Contact Quantum Resistance: " + s.contactQuantumResistance);
			writer.println("Scattering Resistance: " + s.scatteringResistance);
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
