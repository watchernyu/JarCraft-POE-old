/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;
import genetic.Population;

import java.util.Random;

public class Player_Watcher7 extends Player {
	boolean showBestDna = false;
	boolean allowInitRandomMutation = false;
	private String NAME;
	
	private int _id = 0;
	private int enemy;
	Random ran;
	ArrayList<Player> scripts;
	int scriptsSize;
	int numOfUnits=0;
	long timeLimit = 40000000;
	int EVALUTIONMETHOD = 1;//0 means LTD2, 1 means playout
	int futureSteps = 3;
	int numOfMutations = 10;
	Population P;
	boolean firstTimeInit;

	public Player_Watcher7(int playerID) {
		firstTimeInit=true;
		_id = playerID;
		setID(playerID);
		enemy = GameState.getEnemy(_id);
		ran = new Random();
		
		initStrong();//use this to initialize scripts.
	}
	
	public void initWeak(){
		NAME = "Portfolio Online Evolution with 2 old scripts";
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(_id));
		scripts.add(new Player_KiteDPS(_id));
		scriptsSize = scripts.size();
	}
	
	public void initStrong(){
		NAME = "Portfolio Online Evolution with 6 new scripts";
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(_id));
		scripts.add(new Player_NOKAVBack(_id));
		scripts.add(new Player_NOKAVForward(_id));
		scripts.add(new Player_NOKAVForwardFar(_id));
		scripts.add(new Player_NOKAVBackClose(_id));
		scripts.add(new Player_NOKAVBackFar(_id));
		scriptsSize = scripts.size();
	}

	public void setToTestOnly(){
		futureSteps = 1;
		numOfMutations = 1;
	}
	
	public void setFirstTimeInit(){
		//NEED TO SET THIS AT EACH BEGGINNING OF THE GAME (WHEN UNIT NUMBER CHANGES)
		firstTimeInit = true;
	}
	
	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		moveVec.clear();
		List<UnitAction> actions;
		int bestMoveIndex = 0;
		UnitAction move;

		//System.out.println(moves.size());
		
		long startTime = System.nanoTime();
		//System.out.println("nano time: "+System.nanoTime());
		int numOfScripts = scripts.size();
		//DNA initialization
		//fix here: we need to add the sense of population.
		
		if(firstTimeInit){
			P = new Population(this,state,futureSteps,numOfScripts,numOfUnits,scripts);
			P.initialize();
			firstTimeInit = false;
		}
		
		P.reinitialize(state,startTime);

		int evolveCount = 0;
		for(int e=0;e<numOfMutations;e++){
			if(System.nanoTime()-startTime>timeLimit){
				break;
			}
			P.evolve(1);
			evolveCount++;
		}
		ArrayList<Integer> bestDnaPiece = P.bestDna().get(0);
		if(showBestDna){
			System.out.println("BestDNA: "+bestDnaPiece);
		}
		//System.out.println("Evolve Count: "+evolveCount+" Time used: "+(System.nanoTime()-startTime)/1000000);
		
		dnaMoves(state,bestDnaPiece,moves,moveVec);

	}

	public void dnaMoves(GameState state, ArrayList<Integer> DNAi,
			HashMap<Integer,List<UnitAction>> moves, List<UnitAction> moveVec){
		
		ArrayList<ArrayList<Integer>> su = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<scriptsSize;i++){
			su.add(new ArrayList<Integer>());
		}
		//script-units
		
		for(Integer u : moves.keySet()){
			int scriptN = DNAi.get(u);
			su.get(scriptN).add(u);//add each unit to its script.
		}
		for(int s=0;s<scriptsSize;s++){
			int numUnitsInScript = su.get(s).size();
			if(numUnitsInScript>0){
				Player scriptToUse = scripts.get(s);
				HashMap<Integer,List<UnitAction>> scriptUnitMap = new HashMap<Integer,List<UnitAction>>();
				for(int z=0;z<numUnitsInScript;z++){
					int unit = su.get(s).get(z);
					scriptUnitMap.put(unit, moves.get(unit));
				}
				scriptToUse.getMoves(state, scriptUnitMap, moveVec);
			}
		}
	}
	
	/*
	public void dnaMoves(GameState state, ArrayList<Integer> DNAi,
			HashMap<Integer,List<UnitAction>> moves, List<UnitAction> moveVec){
		for (Integer u : moves.keySet()){
			//this u is a unit index!!
			int scriptN = DNAi.get(u);
			
			Player scriptToUse = this.scripts.get(scriptN); //
			
			HashMap<Integer,List<UnitAction>> oneUnitMap = new HashMap<Integer,List<UnitAction>>();
			oneUnitMap.put(u, moves.get(u));
			
			scriptToUse.getMoves(state, oneUnitMap, moveVec);
		}
	}*/

	public void setNumUnit(int n){
		this.numOfUnits = n;
		this.setFirstTimeInit();
	}
	
	public String toString() {
		return NAME;
	}
}
