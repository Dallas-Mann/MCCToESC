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
	
	public void printESC(String outputFileName){
		double scatteringResistanceTotal = 0;
		double contactQuantumResistanceTotal = 0;
		double kineticInductanceTotal = 0;
		for(Shell s : shells){
			scatteringResistanceTotal += s.scatteringResistance;
			contactQuantumResistanceTotal += s.contactQuantumResistance;
			kineticInductanceTotal += s.kineticInductance;
		}
		
		double quantumCapacitanceTotal = sumQuantumCapacitance(0);
		double electrostaticCapacitance = shells.get(numberOfShells - 1).electrostaticCapacitance;
		
		System.out.println("ESC Model");
		System.out.println("Scattering Resistance: " + scatteringResistanceTotal);
		System.out.println("Contact Quantum Resistance: " + contactQuantumResistanceTotal);
		System.out.println("Kinetic Inductance: " + kineticInductanceTotal);
		System.out.println("Quantum Capacitance: " + quantumCapacitanceTotal);
		System.out.println("Electrostatic Capacitance: " + electrostaticCapacitance);
		System.out.println();
		
	}
	
	public void printMCC(String outputFileName){
		System.out.println("MCC Model");
		for(Shell s : shells){
			System.out.println("Shell number: " + s.currentShell);
			System.out.println("Contact Quantum Resistance: " + s.contactQuantumResistance);
			System.out.println("Scattering Resistance: " + s.scatteringResistance);
			System.out.println("Kinetic Inductance: " + s.kineticInductance);
			System.out.println("Quantum Capacitance: " + s.quantumCapacitance);
			System.out.println("Electrostatic Capacitance: " + s.electrostaticCapacitance);
			System.out.println("Number of Conducting Channels: " + s.numConductingChannels);
			System.out.println("Inner Shell Conductance: " + s.innerShellConductance);
			System.out.println("Shell Diameter: " + s.shellDiameter);
			System.out.println();
		}
	}
}
