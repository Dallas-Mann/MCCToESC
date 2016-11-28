public class Shell{
	// constants
	public static final double PLANKS_CONSTANT = Math.pow(10, -34) * 6.62;
	public static final double VANDER_WAALS_GAP = Math.pow(10, -9) * 0.34;
	public static final double FERMI_VELOCITY = Math.pow(10, 5) * 8;
	public static final double COULOMBS_CONSTANT = Math.pow(10, -19) * 1.602;
	public static final double EPSILON = 8.85418782 * Math.pow(10, -12);
	
	// not currently used constant, but we think it might be used.
	public static final double EPSILON_NAUGHT = 2;
	// instance variables
	protected double imperfectContactResistance = 100000;
	protected double contactQuantumResistance;
	protected double scatteringResistance;
	protected double kineticInductance;
	protected double quantumCapacitance;
	protected double electrostaticCapacitance;
	protected double numConductingChannels;
	protected double innerShellConductance;
	
	// parameters passed in
	protected double shellDiameter;
	protected int currentShell;
	protected Nanotube nanotube;
	
	public Shell(double shellDiameter, int currentShell, Nanotube nanotube){
		this.shellDiameter = shellDiameter;
		this.currentShell = currentShell;
		this.nanotube = nanotube;
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
			numConductingChannels = 0.2 + (0.116 * shellDiameter * Math.pow(10, 9));
		}
		else
			numConductingChannels = 2.0/3.0;
	}
	
	private void calcContactQuantumResistance(){
		contactQuantumResistance = 12.9 * Math.pow(10, 3) / numConductingChannels;
	}
	
	private void calcScatteringResistance(){
		scatteringResistance = PLANKS_CONSTANT*Math.pow(10, -6)/
				(2 * COULOMBS_CONSTANT * COULOMBS_CONSTANT * numConductingChannels * nanotube.meanFreePath);
	}
	
	private void calcKineticInductance(){
		// making a method to calculate this value later
		// normally 8 nH/uM so we set it as such
		kineticInductance = 8 * Math.pow(10, -9);
	}
	
	private void calcQuantumCapacitance(){
		quantumCapacitance = 4 * COULOMBS_CONSTANT * COULOMBS_CONSTANT * numConductingChannels / 
				(PLANKS_CONSTANT * FERMI_VELOCITY);
	}
	
	private void calcElectrostaticCapacitance(){
		if(currentShell < nanotube.numberOfShells){
			electrostaticCapacitance = 2 * Math.PI * EPSILON /
					Math.log((shellDiameter + Nanotube.distanceBetweenShells)/shellDiameter);
		}
		else
			electrostaticCapacitance = Math.PI * EPSILON / Math.log((2 * 50 * Math.pow(10, -9)/nanotube.diameterOutermostShell)-1);
	}
	
	private void calcInnerShellConductance(){
		if(currentShell != nanotube.numberOfShells){
			innerShellConductance = Math.pow(10, -2)
					* Math.PI * shellDiameter / VANDER_WAALS_GAP;
		}
		else
			innerShellConductance = 0;
	}
}
