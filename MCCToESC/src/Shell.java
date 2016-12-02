public class Shell{
	//constants
	public static final double PLANCKS_CONSTANT = Math.pow(10, -34) * 6.62607004;
	public static final double VANDER_WAALS_GAP = Math.pow(10, -9) * 0.34;
	public static final double FERMI_VELOCITY = Math.pow(10, 5) * 8;
	public static final double COULOMBS_CONSTANT = Math.pow(10, -19) * 1.60217662;
	public static final double EPSILON_O = Math.pow(10, -12) * 8.85418782;
	//TODO make this an input parameter
	public static final double EPSILON_R = 1;
	
	//instance variables
	protected double imperfectContactResistance = 100000;
	protected double contactQuantumResistance;
	protected double scatteringResistance;
	protected double kineticInductance;
	protected double quantumCapacitance;
	protected double electrostaticCapacitance;
	protected double numConductingChannels;
	protected double innerShellConductance;
	
	//parameters passed in
	protected double shellDiameter;
	protected int currentShell;
	protected Nanotube nanotube;
	
	//constructor
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
		scatteringResistance = PLANCKS_CONSTANT/
				(2 * COULOMBS_CONSTANT * COULOMBS_CONSTANT * numConductingChannels * nanotube.meanFreePath);
	}
	
	private void calcKineticInductance(){
		//normally 8 nH/uM
		//kineticInductance = 8 * Math.pow(10, -9);
		kineticInductance = PLANCKS_CONSTANT/
				(4 * COULOMBS_CONSTANT * COULOMBS_CONSTANT * FERMI_VELOCITY * numConductingChannels);
	}
	
	private void calcQuantumCapacitance(){
		quantumCapacitance = 4 * COULOMBS_CONSTANT * COULOMBS_CONSTANT * numConductingChannels / 
				(PLANCKS_CONSTANT * FERMI_VELOCITY);
	}
	
	//TODO fix these formulas
	private void calcElectrostaticCapacitance(){
		if(currentShell < nanotube.numberOfShells){
			electrostaticCapacitance = 2 * Math.PI * EPSILON_O * EPSILON_R/
					Math.log((shellDiameter + (2 * Nanotube.distanceBetweenShells))/shellDiameter);
		}
		else
		{
			double r = nanotube.diameterOutermostShell / 2.0;
			double H = nanotube.distanceToGroundPlane;
			electrostaticCapacitance = 2 * Math.PI * EPSILON_O * EPSILON_R / Math.log(((H+r)/r) + Math.sqrt(((H+r)*(H+r)/(r*r)) - 1));
			//electrostaticCapacitance = Math.PI * EPSILON_O / Math.log((2 * 50 * Math.pow(10, -9)/nanotube.diameterOutermostShell)-1);
		}
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
