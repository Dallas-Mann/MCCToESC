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
	protected int numberOfSections;
	
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
	
	private void constructNanotube(){
		double diameterCurrentShell = diameterInnermostShell;
		
		for(int currentShell = 1; currentShell <= numberOfShells; currentShell++){
			shells.add(new Shell(diameterCurrentShell, currentShell, this));
			diameterCurrentShell += 2 * distanceBetweenShells;
		}
	}
	
	//wrapper method so you don't have to pass a start index as a parameter
	private double sumQuantumCapacitance(){
		//starts at outermost shell and works its way in
		return sumQuantumCapacitance(numberOfShells-1);
	}
	
	//used to convert bridge of capacitances to equivalent capacitance recursively
	//this is used when converting from the MCC to the ESC model
	private double sumQuantumCapacitance(int index){
		if(index == 0){
			return shells.get(0).quantumCapacitance;
		}
		else{
			Shell temp = shells.get(index);
			Shell temp2 = shells.get(index-1);
			return temp.quantumCapacitance + addReciprocal(temp2.electrostaticCapacitance, sumQuantumCapacitance(index-1));
		}
	}
	
	private int calculateNumberOfSections(double kineticInductance, double totalCapacitance){
		//TODO set to 200 right now to confirm results
		return 200;//(int) Math.round(20*Math.sqrt(kineticInductance*totalCapacitance)*lengthOfNanotube/Math.pow(10, -12));
	}
	
	private double addReciprocal(double val1, double val2){
		double val = 1.0/((1.0/val1)+(1.0/val2));
		return val;
	}
	
	public void printMCC(PrintWriter writer) throws FileNotFoundException{
		writer.println("***************MCC Model*********************");
		
		double imperfectContactResistance = 0;
		double contactQuantumResistance = 0;
		
		double[] scatteringResistance = new double[numberOfShells];
		double[] kineticInductance = new double[numberOfShells];
		double[] quantumCapacitance = new double[numberOfShells];
		double[] innerShellConductance = new double[numberOfShells];
		double[] electrostaticCapacitance = new double[numberOfShells];
		
		for(int curShell = 0; curShell < numberOfShells; curShell++){
			imperfectContactResistance += 1.0/shells.get(curShell).imperfectContactResistance;
			contactQuantumResistance += 1.0/shells.get(curShell).contactQuantumResistance;
			
			scatteringResistance[curShell] = shells.get(curShell).scatteringResistance*(lengthOfNanotube/numberOfSections);
			kineticInductance[curShell] = shells.get(curShell).kineticInductance*(lengthOfNanotube/numberOfSections);
			quantumCapacitance[curShell] = shells.get(curShell).quantumCapacitance*(lengthOfNanotube/numberOfSections);
			innerShellConductance[curShell] = 1.0/shells.get(curShell).innerShellConductance*(lengthOfNanotube/numberOfSections);
			electrostaticCapacitance[curShell] = shells.get(curShell).electrostaticCapacitance*(lengthOfNanotube/numberOfSections);
		}
		imperfectContactResistance = 1.0/imperfectContactResistance/2.0;
		contactQuantumResistance = 1.0/contactQuantumResistance/2.0;

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
		
		int[][] contactRes = new int[2][2];
		int[][] quantumResFront = new int[numberOfShells][2];
		int[][] quantumResBack = new int[numberOfShells][2];
		
		int[][][] scatteringRes = new int[numberOfShells][numberOfSections][2];
		int[][][] kineticInd = new int[numberOfShells][numberOfSections][2];
		int[][][] innerCond = new int[numberOfShells - 1][numberOfSections][2];
		
		int[][][] quantumCap = new int[numberOfShells][numberOfSections][2];
		int[][][] innerCap = new int[numberOfShells - 1][numberOfSections][2];
		
		int[][] electroCap = new int[numberOfSections][2];
		
		int startNode = 1;
		int commonNodeOne = startNode + 1;
		int commonNodeTwo = startNode + 2;
		int currentNode = startNode + 3;
		
		//imperfect contact resistance (parallel resistors from commonNodeOne to all shell beginnings)
		contactRes[0][0] = startNode;
		contactRes[0][1] = commonNodeOne;
		
		//precalculate component node values
		for(int curShell = 0; curShell < numberOfShells; curShell++){
			
			//these all share a common node, the same as the second node of the contact resistance
			quantumResFront[curShell][0] = commonNodeOne;
			quantumResFront[curShell][1] = currentNode;
			
			for(int curSec = 0; curSec < numberOfSections; curSec++){
				
				if(curSec == 0){
					scatteringRes[curShell][curSec][0] = currentNode++;
					scatteringRes[curShell][curSec][1] = currentNode;
				}
				else{
					scatteringRes[curShell][curSec][0] = currentNode++ - 1;
					scatteringRes[curShell][curSec][1] = currentNode;
				}
				
				kineticInd[curShell][curSec][0] = currentNode++;
				kineticInd[curShell][curSec][1] = currentNode;
				
				quantumCap[curShell][curSec][0] = currentNode++;
				quantumCap[curShell][curSec][1] = currentNode;
				
				if(curShell != 0){
					innerCond[curShell - 1][curSec][0] = scatteringRes[curShell - 1][curSec][1];
					innerCond[curShell - 1][curSec][1] = scatteringRes[curShell][curSec][1];
					
					innerCap[curShell - 1][curSec][0] = quantumCap[curShell - 1][curSec][1];
					innerCap[curShell - 1][curSec][1] = quantumCap[curShell][curSec][1];
				}
				
				if(curShell == numberOfShells - 1){
					electroCap[curSec][0] = kineticInd[curShell][curSec][1];
					electroCap[curSec][1] = 0;
				}
			}

			//use the last section of the shell kinetic inductance node value 2 for this resistances node value 1
			quantumResBack[curShell][0] = kineticInd[curShell][numberOfSections - 1][1];
			quantumResBack[curShell][1] = commonNodeTwo;
		}
		
		//imperfect contact resistance (parallel resistors from all shell beginnings to commonNodeTwo)
		contactRes[1][0] = commonNodeTwo;
		contactRes[1][1] = currentNode;
		
		//print the components to a netlist
		PrintWriter mccNetlist = new PrintWriter(new File("mccNetlist.txt"));
		
		int numRes = 0;
		int numInd = 0;
		int numCap = 0;
		
		int curShell = 0;
		//imperfect contact resistance
		mccNetlist.println("R" + (numRes++) + " " + contactRes[0][0] + " " + contactRes[0][1] + " " + imperfectContactResistance);
		for(Shell s : shells){
			//quantum contact resistance
			mccNetlist.println("R" + (numRes++) + " " + quantumResFront[curShell][0] + " " + quantumResFront[curShell][1] + " " + contactQuantumResistance);
			for(int curSec = 0; curSec < numberOfSections; curSec++){
				//scattering resistance
				mccNetlist.println("R" + (numRes++) + " " + scatteringRes[curShell][curSec][0] + " " + scatteringRes[curShell][curSec][1] + " " + scatteringResistance[curShell]);
				//kinetic inductance
				mccNetlist.println("L" + (numInd++) + " " + kineticInd[curShell][curSec][0] + " " + kineticInd[curShell][curSec][1] + " " + kineticInductance[curShell]);
				//quantum capacitance
				mccNetlist.println("C" + (numCap++) + " " + quantumCap[curShell][curSec][0] + " " + quantumCap[curShell][curSec][1] + " " + quantumCapacitance[curShell]);
				
				if(curShell != numberOfShells - 1){
					//inner shell conductance
					mccNetlist.println("R" + (numRes++) + " " + innerCond[curShell][curSec][0] + " " + innerCond[curShell][curSec][1] + " " + innerShellConductance[curShell]);
					//inner shell capacitance
					mccNetlist.println("C" + (numCap++) + " " + innerCap[curShell][curSec][0] + " " + innerCap[curShell][curSec][1] + " " + electrostaticCapacitance[curShell]);
				}
				else if(curShell == numberOfShells - 1){
					//electrostatic capacitance to ground
					mccNetlist.println("C" + (numCap++) + " " + electroCap[curSec][0] + " " + electroCap[curSec][1] + " " + electrostaticCapacitance[curShell]);
				}
			}
			//quantum contact resistance
			mccNetlist.println("R" + (numRes++) + " " + quantumResBack[curShell][0] + " " + quantumResBack[curShell][1] + " " + contactQuantumResistance);
			curShell++;
		}
		//imperfect contact resistance
		mccNetlist.println("R" + (numRes++) + " " + contactRes[1][0] + " " + contactRes[1][1] + " " + imperfectContactResistance);
		
		mccNetlist.println(".end");
		mccNetlist.close();
	}
	
	//convert SI units to section length parameters (multiply by length / num sections)
	public void printESC(PrintWriter writer) throws FileNotFoundException{
		double imperfectContactResistance = 0;
		double contactQuantumResistance = 0;
		double scatteringResistance = 0;
		double kineticInductance = 0;
		
		//sum resistances and inductance in parallel, because the shells are in parallel
		for(Shell s : shells){
			imperfectContactResistance += 1.0/s.imperfectContactResistance;
			contactQuantumResistance += 1.0/s.contactQuantumResistance;
			scatteringResistance += 1.0/s.scatteringResistance;
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
		
		writer.println("***************ESC Model*********************");
		//writer.println("Number of Sections: " + calculateNumberOfSections());
		writer.println("Imperfect Contact Resistance: " + imperfectContactResistance);
		writer.println("Contact Quantum Resistance: " + contactQuantumResistance);
		writer.println("");
		writer.println("SI Unit Parameters:");
		writer.println("Scattering Resistance: " + scatteringResistance);
		writer.println("Kinetic Inductance: " + kineticInductance);
		writer.println("Quantum Capacitance: " + quantumCapacitance);
		writer.println("Electrostatic Capacitance: " + electrostaticCapacitance);
		
		numberOfSections = calculateNumberOfSections(kineticInductance, addReciprocal(quantumCapacitance, electrostaticCapacitance));
		scatteringResistance = scatteringResistance*(lengthOfNanotube/numberOfSections);
		kineticInductance = kineticInductance*(lengthOfNanotube/numberOfSections);
		quantumCapacitance = quantumCapacitance*(lengthOfNanotube/numberOfSections);
		electrostaticCapacitance = electrostaticCapacitance*(lengthOfNanotube/numberOfSections);
		writer.println("");
		writer.println("Per Section Parameters:");
		writer.println("Number of Sections: " + numberOfSections);
		writer.println("Scattering Resistance: " + scatteringResistance);
		writer.println("Kinetic Inductance: " + kineticInductance);
		writer.println("Quantum Capacitance: " + quantumCapacitance);
		writer.println("Electrostatic Capacitance: " + electrostaticCapacitance);
		
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
		
		for(int i = 0; i < numberOfSections; i++){
			if(i == 0){
				nodeOne = startNode;
				nodeTwo = startNode + 1;
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
