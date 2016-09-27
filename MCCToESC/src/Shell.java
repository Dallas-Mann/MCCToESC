
public class Shell {
	// constants
	final double planksConstant = Math.pow(10, -34) * 6.62;
	final double vanderWaalsGap = Math.pow(10, -9) * 0.34;
	final double fermiVelocity = Math.pow(10, 5) * 8;
	final double coulombsConstant = Math.pow(10, -19) * 1.6;
	final double epsillon = 8.85418782 * Math.pow(10, -12);
	final double epsillonKnot = 2;
	
	// instance variables
	protected double contactQuantumResistance;
	protected double scatteringResistance;
	protected double kineticInductance;
	protected double quantumCapacitance;
	protected double electrostaticCapacitance;
	
	protected double numConductingChannels;
	protected double innerShellConductance;
	protected double shellDiameter;
	protected double meanFreePath;
	protected int currentShell;
	protected double numberOfShells;
	protected double distanceBetweenShells;
	protected double diameterOutermostShell;
	
	public Shell(double shellDiameter, double meanFreePath, 
			int currentShell, int numberOfShells, double distanceBetweenShells, double diameterOutermostShell){
		this.shellDiameter = shellDiameter;
		this.meanFreePath = meanFreePath;
		this.currentShell = currentShell;
		this.numberOfShells = numberOfShells;
		this.distanceBetweenShells = distanceBetweenShells;
		this.diameterOutermostShell = diameterOutermostShell;
		calcNumConductingChannels();
		calcContactQuantumResistance();
		calcScatteringResistance();
		calcKineticInductance();
		calcQuantumCapacitance();
		calcElectrostaticCapacitance();
		calcInnerShellConductance();
	}

	private void calcNumConductingChannels(){
		if(shellDiameter > 4.3 * Math.pow(10, -9)){
			numConductingChannels = 0.116 * shellDiameter + 0.2;
		}
		else
			numConductingChannels = 2.0/3.0;
	}
	
	private void calcContactQuantumResistance(){
		contactQuantumResistance = 12.9 * Math.pow(10, 3) / numConductingChannels;
	}
	
	private void calcScatteringResistance(){
		scatteringResistance = planksConstant/(2 * coulombsConstant * coulombsConstant * numConductingChannels * meanFreePath);
	}
	
	private void calcKineticInductance(){
		// making a method to calculate this value later
		// normally 8 nH/uM so we set it as such
		kineticInductance = 8 * Math.pow(10, -9);
	}
	
	private void calcQuantumCapacitance(){
		quantumCapacitance = 4 * coulombsConstant * coulombsConstant * numConductingChannels / (planksConstant * fermiVelocity);
	}
	
	private void calcElectrostaticCapacitance(){
		if(currentShell < numberOfShells){
			electrostaticCapacitance = 2 * Math.PI * epsillon /
					Math.log((shellDiameter + distanceBetweenShells)/shellDiameter);
		}
		else
			electrostaticCapacitance = Math.PI * epsillon / Math.log((2 * 22 * Math.pow(10, -9)/diameterOutermostShell)-1);
	}
	
	private void calcInnerShellConductance(){
		if(currentShell != numberOfShells){
			innerShellConductance = Math.pow(10, 4) * Math.PI * shellDiameter / vanderWaalsGap;
		}
		else
			innerShellConductance = 0;
	}
}
