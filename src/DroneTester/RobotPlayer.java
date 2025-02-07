package DroneTester;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.PriorityQueue;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    static HashMap<Integer, Direction> dirHash = new HashMap<Integer, Direction>();
    static MapLocation initialLoc;
    static MapLocation souploc;
    static Direction path;
    static String task;
    static int numBuilt;
    static int mapQuadrant;
    static int schoolsBuilt;
    static MapLocation enHQDest;
    static int factoriesBuilt;

    static int fulfillmentsBuilt;

    static  MapLocation HQ;
    static MapLocation scoutDest;
    static MapLocation mapCenter;
    static ArrayList<MapLocation> scouted = new ArrayList<>();

    static int robotsBuilt;
    static boolean moveOnce = false;

    /**MapLocation arrays containing all the relevent MapLocations **/


    static MapLocation water;
    static ArrayList<MapLocation> soup = new ArrayList<>();
    static ArrayList<MapLocation> refineries = new ArrayList<>();
    static ArrayList<MapLocation> oppNet = new ArrayList<>();
    static ArrayList<MapLocation> offensiveEnemyBuildings = new ArrayList<>();
    static MapLocation EnemyHQ;
    
    static ArrayList<MapLocation> digLoc = new ArrayList<>();

    static ArrayList<MapLocation> irWater = new ArrayList<>();
    static ArrayList<MapLocation> irSoup = new ArrayList<>();

    //__________________________________________________________________________________________________________________
    //RUN CODE BELOW
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        turnCount = 0;
        schoolsBuilt = 0;
        factoriesBuilt = 0;

        fulfillmentsBuilt = 0;

        souploc = null;
        enHQDest = null;
        EnemyHQ = null;
        path = Direction.CENTER;
        HQ = getHQLocation();
        scoutDest = null;
        mapCenter = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);

        //landscaper task determiner
        if(rc.getType() == RobotType.LANDSCAPER){
                task = "terraform";
        }
        //drone task determiner
        if(rc.getType() == RobotType.DELIVERY_DRONE){
                task = "killEnemy";
        }
//        if(rc.getType() == RobotType.DELIVERY_DRONE){
//            droneTask = "cow";
//            //find soup and water?
//            droneTask = "scoutWithMiner";
//            droneTask = "helpMyLandscaper";
//            droneTask = "killEnemy";
//        }

        initialLoc = rc.getLocation();
        System.out.println("INITIAL LOCATION IS: " + initialLoc);

        //fill up dirHash 1:Direction.NORTH, etc
        for (int i = 0; i < directions.length; i++){
            dirHash.put(i+1, directions[i]);
        }
        //System.out.println(dirHash);

        //System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
    //__________________________________________________________________________________________________________________
    //HQ CODE BELOW
    static void runHQ() throws GameActionException {
    	if(rc.getRoundNum() == 1) {
    		postLocation(1, rc.getLocation().x, rc.getLocation().y, 2);
    	}
    	RobotInfo[] r = rc.senseNearbyRobots();
    	for(RobotInfo s : r){
    	    if(s.getTeam() != rc.getTeam() && rc.canShootUnit(s.getID())){
    	        rc.shootUnit(s.getID());
            }
        }
    	if (rc.getRoundNum() < 20) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.MINER, dir)) {
                    robotsBuilt++;
                }
            }
        }
    	updateEnemyHQLocation();
        //}
    }
    //__________________________________________________________________________________________________________________
    //MINER CODE BELOW
    static void runMiner() throws GameActionException {
        MapLocation curr = rc.getLocation();
        if(HQ == null){
            HQ = getHQLocation();
        }
        //scanForSoup(curr);
        //souploc = getSoupLocation();
        //build design school
//        System.out.println("robots built: "+ robotsBuilt);
//        if (schoolsBuilt < 1 && !scanForDesignSchool()) {
//        	MapLocation loc = getHQLocation();
//        	Direction away = curr.directionTo(loc).opposite();
//        	if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, away)){
//        	    schoolsBuilt++;
//                tryBuild(RobotType.DESIGN_SCHOOL, away);
//            }else if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, away.rotateLeft())) {
//                schoolsBuilt++;
//                tryBuild(RobotType.DESIGN_SCHOOL, away.rotateLeft());
//            }else if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, away.rotateRight())){
//                schoolsBuilt++;
//                tryBuild(RobotType.DESIGN_SCHOOL, away.rotateRight());
//            }
//        }
//        if (factoriesBuilt < 1 && !scanForDroneFactory()) {
//            MapLocation loc = getHQLocation();
//            Direction away = curr.directionTo(loc).opposite();
//            if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, away)){
//                factoriesBuilt++;
//                tryBuild(RobotType.FULFILLMENT_CENTER, away);
//            }else if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, away.rotateLeft())) {
//                factoriesBuilt++;
//                tryBuild(RobotType.FULFILLMENT_CENTER, away.rotateLeft());
//            }else if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, away.rotateRight())){
//                factoriesBuilt++;
//                tryBuild(RobotType.FULFILLMENT_CENTER, away.rotateRight());
//            }
//        }
        //System.out.println("SCHOOLS BUILT: " + schoolsBuilt);

        //NOTE: schoolsBuilt is saved per miner, meaning each miner will want to make its own design school
        //MINE SOUP
        openEyes(curr);

        if(fulfillmentsBuilt == 0){
            for(Direction d : directions) {
                if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, d)) {
                    //refineries.add(loc.add(d));
                    rc.buildRobot(RobotType.FULFILLMENT_CENTER, d);
                    fulfillmentsBuilt++;
                }
            }
        }

        if (souploc != null && rc.getSoupCarrying() < 96){
            mineSoup();
        }
        //MOVE BACK TO HQ AND DEPOSIT SOUP (todo: implement refineries)
        else if (rc.getSoupCarrying() > 95){
            for (Direction dir : directions){
                if(rc.canDepositSoup(dir)){
                    rc.depositSoup(dir, rc.getSoupCarrying());
                }
            }
            if(getClosestRefine(curr) == null) {
                moveTo(HQ);
            }else{
                moveTo(getClosestRefine(curr));
            }
        }
        //FIND SOUP
        else {
            if (soup.isEmpty()){
                //scout in an expanding circle starting at HQ
                scoutMiner(curr);
            }
            else{
                souploc= getClosestSoup(curr);
                moveTo(souploc);
            }
        }

    }
    static void openEyes(MapLocation loc) throws GameActionException{

        if(souploc != null && rc.canSenseLocation(souploc)){
            if(rc.senseSoup(souploc) == 0){
                soup.remove(souploc);
                souploc = null;
            }
        }

//        int selfX = loc.x;
//        int selfY = loc.y;
//        int x = 0;
//        int y = 0;
        MapLocation[] miso = rc.senseNearbySoup();
        int totS = 0;
        for(MapLocation m : miso) {
            if (!soup.contains(m)) {
                    soup.add(m);
            }
            if(rc.canSenseLocation(m)) {
                totS += rc.senseSoup(m);
            }
        }
        RobotInfo[] r2d2 = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
        for(RobotInfo i : r2d2){
            if(i.getType() == RobotType.REFINERY){
                refineries.add(i.getLocation());
            }
        }

        if(totS > 200 && (refineries.isEmpty()|| loc.distanceSquaredTo(getClosestRefine(loc)) > 150)){

            for(Direction d : directions) {
                if(rc.canBuildRobot(RobotType.REFINERY, d)) {
                    refineries.add(loc.add(d));
                    rc.buildRobot(RobotType.REFINERY, d);
                }
            }
        }
//        while( Math.abs(x) < 6 || Math.abs(y) < 6){
//            MapLocation n = new MapLocation(selfX + x, selfY + y);
//            if(rc.onTheMap(n) && rc.canSenseLocation(n)){
//                if(rc.senseSoup(n) > 0) {
//                    if(!soup.contains(n)){
//                        soup.add(n);
//                        if(soup.size() >= 2){
//                            x = 6;
//                            y = 6;
//                        }
//                    }
//                }
//            }
//            if( x >= 0 && y >= 0 && x == y){
//                x += 1;
//            }else if(x > 0 && Math.abs(x) > Math.abs(y)){
//                y -= 1;
//            }else if(x > 0 && y < 0 && Math.abs(x) == Math.abs(y)){
//                x -= 1;
//            }else if(y < 0 && Math.abs(x) < Math.abs(y)){
//                x -= 1;
//            }else if(x < 0 && y < 0 && Math.abs(x) == Math.abs(y)){
//                y += 1;
//            }else if(x < 0 && Math.abs(x) > Math.abs(y)){
//                y += 1;
//            }else if(x < 0 && y > 0 && Math.abs(x) == Math.abs(y)){
//                x += 1;
//            }else if(y > 0 && Math.abs(y) > Math.abs(x)){
//                x += 1;
//            }
//            System.out.println("["+x+","+y+"]");
////            if(x == 0 && y == -1){
////                x = 6;
////                y = 6;
////            }
//        }
    }

    static MapLocation getClosestRefine( MapLocation m){
        if(refineries.isEmpty()){
            return null;
        }
        MapLocation o = null;
        int diss = 1000000;
        for (MapLocation n : refineries){
            int ned = m.distanceSquaredTo(n);
            if(ned < diss){
                diss = ned;
                o = n;
            }
        }
        return o;
    }

    static MapLocation getClosestSoup( MapLocation m){
        if(soup.isEmpty()){
            return null;
        }
        MapLocation o = null;
        int diss = 1000000;
        for (MapLocation n : soup){
            int ned = m.distanceSquaredTo(n);
            if(ned < diss){
                diss = ned;
                o = n;
            }
        }
        return o;
    }
    static MapLocation getFarthestSoup( MapLocation m){
        if(soup.isEmpty()){
            return null;
        }
        MapLocation o = null;
        int diss = 0;
        for (MapLocation n : soup){
            int ned = m.distanceSquaredTo(n);
            if(ned > diss){
                diss = ned;
                o = n;
            }
        }
        return o;
    }

    /**robot mines soup **/
    static boolean canMine() throws GameActionException{
        for (Direction l : directions){
            if (rc.canMineSoup(l)){
                return true;
            }
        }
        return false;
    }

    static void mineSoup() throws GameActionException{
        for (Direction l : directions) {
            if (rc.canMineSoup(l)) {
                rc.mineSoup(l);
            }
        }
        moveTo(souploc);
    }

    static void scoutMiner(MapLocation at) throws GameActionException {
        if(scoutDest ==  null){
            scoutDest = HQ;
            scouted.add(scoutDest);
        }
        int xAdd = (rc.getMapWidth()-HQ.x)/4;
        int yAdd = (rc.getMapHeight()-HQ.y)/4;
        if(xAdd < 4){
            xAdd = 0;
        }
        if(yAdd < 4){
            yAdd = 0;
        }
        if (at.distanceSquaredTo(scoutDest) < 16) {
            int xRel = at.x - HQ.x;
            int yRel = at.y - HQ.y;
            if(xRel > 0 && yRel > 0){
                MapLocation newDest = new MapLocation( at.x ,rc.getMapHeight() - at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(at.x ,rc.getMapHeight() - at.y - yAdd);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            } else if(xRel < 0 && yRel > 0){
                MapLocation newDest = new MapLocation( rc.getMapWidth() - at.x ,at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(rc.getMapWidth() - at.x + xAdd, at.y);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            }else if(xRel < 0 && yRel < 0){
                MapLocation newDest = new MapLocation( at.x ,rc.getMapHeight() - at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(at.x ,rc.getMapHeight() - at.y + yAdd);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            }else if(xRel > 0 && yRel < 0){
                MapLocation newDest = new MapLocation( rc.getMapWidth() - at.x ,at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(rc.getMapWidth() - at.x - xAdd, at.y);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            }
        }

        moveToDrone(scoutDest);
    }

    static boolean scanForDesignSchool(){
        RobotInfo[] r = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());

        for(RobotInfo i : r){
            if(i.getType() == RobotType.DESIGN_SCHOOL){
                return true;
            }
        }
        return false;

    }

    static boolean scanForDroneFactory(){
        RobotInfo[] r = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());

        for(RobotInfo i : r){
            if(i.getType() == RobotType.FULFILLMENT_CENTER){
                return true;
            }
        }
        return false;

    }
    /*
        static void findSoup(MapLocation loc) throws GameActionException{
            Direction d = randomDirection();
            int selfX = loc.x;
            int selfY = loc.y;
            scanForSoup(loc);
    //        if(getSoupLocation() != null)
    //        	moveTo(getSoupLocation());
    //        else
                tryMove(d);
        }
    *
        static void scanForSoup(MapLocation loc) throws GameActionException{
            int selfX = loc.x;
            int selfY = loc.y;
            scan:
            for(int x = -5; x <6; x++){
                for (int y = -5; y < 6; y++){
                    MapLocation check = new MapLocation(selfX + x, selfY + y);
                    if (rc.canSenseLocation(check)){
                        if(rc.senseSoup(check) > 0){
                            //plusOrMinusOne makes Miner stop either left or right of soup
                            //double rando = Math.random();
                            //int plusOrMinusOne = (int)Math.signum(rando - 0.5);
                            if(getSoupLocation() == null)
                                postLocation(2, check.x, check.y, 1);
                            //moveTo(new MapLocation(check.x - 1, check.y));
                            break scan;
                            //upload location of soup to blockchain?
                        }
                    }
                }
            }
        }
    */


    //these are from the provided scaffold...
    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }
    //__________________________________________________________________________________________________________________
    //BUILDING CODE BELOW (REFINERY(miners), VAPORATOR(soup producer, pollution reducer),
    //DESIGN SCHOOL(landscapers), FULFILLMENT CENTER(drones)
    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    //Builds Landscapers
    static void runDesignSchool() throws GameActionException {
        for (Direction dir : directions)
            if (tryBuild(RobotType.LANDSCAPER, dir)) {
                robotsBuilt++;
            }
    }
    //Builds Drones
    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            if (tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                robotsBuilt++;
            }
//        if(robotsBuilt < 20) {
//            for (Direction dir : directions)
//                if (tryBuild(RobotType.DELIVERY_DRONE, dir)) {
//                    robotsBuilt++;
//                }
//        }
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }
    //__________________________________________________________________________________________________________________
    //LANDSCAPER CODE BELOW
    static void runLandscaper() throws GameActionException {
    	if(task.equals("castle")) {
            buildCastle();
        }
        else if(task.equals("terraform")){
            terraform();
        }
    }
    
    static void tryDig(Direction dir) throws GameActionException{
    	if(rc.isReady() && rc.canDigDirt(dir))
    		rc.digDirt(dir);
    		
    }

    static void terraform() throws GameActionException{
        MapLocation home = HQ;
        MapLocation at = rc.getLocation();
        Direction dir = at.directionTo(home);
        RobotInfo scan[] = rc.senseNearbyRobots();
        for(RobotInfo i : scan) {
        	if(i.getTeam() != rc.getTeam() && i.getType().isBuilding()) {
        		bury(i.getLocation(), at);
        	}
        }
        if(at.distanceSquaredTo(home) > 8) {
        	
        	MapLocation frontRow[] = new MapLocation[9];
        	frontRow[0] = at;
        	frontRow[1] = frontRow[0].add(dir.rotateRight().rotateRight());
        	frontRow[2] = frontRow[0].add(dir.rotateLeft().rotateLeft());
        	frontRow[3] = frontRow[1].add(dir.rotateRight().rotateRight());
        	frontRow[4] = frontRow[2].add(dir.rotateLeft().rotateLeft());
        	frontRow[5] = frontRow[3].add(dir.rotateRight().rotateRight());
        	frontRow[6] = frontRow[4].add(dir.rotateLeft().rotateLeft());
        	
        	if(rc.getDirtCarrying() < 25) {
        		
        		if(rc.isReady() && rc.canDigDirt(dir)) {
            		rc.digDirt(dir);
            		digLoc.add(at.add(dir));
        		}
        	}
        	
        	if(rc.getDirtCarrying() == 25) {
        		for(int i = 1; i < 7; i++) {
        			if(rc.canSenseLocation(frontRow[i]) && !digLoc.contains(frontRow[i])) {
        				if(rc.senseElevation(frontRow[i]) < rc.senseElevation(frontRow[0]) && rc.senseRobotAtLocation(frontRow[i]) == null) {
        					if(at.distanceSquaredTo(frontRow[i]) > 2) {
        						moveTo(frontRow[i]);
        					}
        					else if(rc.canDepositDirt(at.directionTo(frontRow[i]))){
        						rc.depositDirt(at.directionTo(frontRow[i]));
        					}
        				}
        			}
        			if(rc.senseElevation(at) < 8) {
    					rc.depositDirt(Direction.CENTER);
    				}
        		}
        		if(rc.isReady()) {
        			tryMove(dir.opposite());
        			tryMove(dir.opposite().rotateLeft());
        			tryMove(dir.opposite().rotateRight());
        		}
        	}
        }
    }
    
    static void bury(MapLocation target, MapLocation at) throws GameActionException{
    	if(at.distanceSquaredTo(target) > 2) {
    		moveTo(target);
    	}
    	else {
    		if(rc.getDirtCarrying() > 0)
                rc.depositDirt(at.directionTo(target));
    		else if(rc.senseRobotAtLocation(at.add(at.directionTo(target).opposite())) == null && rc.canDigDirt(at.directionTo(target).opposite()))
                rc.digDirt(at.directionTo(target).opposite());
    		else if(rc.senseRobotAtLocation(at.add(at.directionTo(target).rotateRight().rotateRight())) == null && rc.canDigDirt(at.directionTo(target).rotateRight().rotateRight()))
                rc.digDirt(at.directionTo(target).rotateRight().rotateRight());
    		else 
                rc.digDirt(at.directionTo(target).rotateLeft().rotateLeft());
    	}
    }
    
    static void buildCastle() throws GameActionException{
    	Direction[] direction = {
    	        Direction.NORTH,
    	        Direction.EAST,
    	        Direction.SOUTH,
    	        Direction.WEST,
    	        Direction.NORTHEAST,
    	        Direction.SOUTHEAST,
    	        Direction.SOUTHWEST,
    	        Direction.NORTHWEST
    	    };
    	MapLocation home = HQ;
        MapLocation at = rc.getLocation();
        Direction dir = at.directionTo(home);
        MapLocation[] build = new MapLocation[8];
        for(int i = 0; i < 8; i++) {
        	build[i] = home.add(direction[i]);
        }
        if(at.distanceSquaredTo(home) > 8){
            moveTo(home);
        }
        else if (at.distanceSquaredTo(home) > 2){
        	for(int i = 0; i < 8; i++) {
        		if(!rc.isLocationOccupied(build[i]) && rc.onTheMap(build[i])) {
        			moveTo(build[i]);
        			break;
        		}
        	}
        }
        else if(rc.canDigDirt(dir) && rc.getDirtCarrying() < 25) {
        	rc.digDirt(dir);
        }
        else if (at.distanceSquaredTo(home) > 1) {	
        	MapLocation left = at.add(dir.rotateLeft());
        	MapLocation right = at.add(dir.rotateRight());
        	if (rc.senseElevation(left) < rc.senseElevation(at) && rc.onTheMap(left) && (rc.senseRobotAtLocation(left).getType().name() == "LANDSCAPER" || rc.getRoundNum() > 400)) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(left));
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        	else if(rc.senseElevation(right) < rc.senseElevation(at) && rc.onTheMap(right) && (rc.senseRobotAtLocation(right).getType().name() == "LANDSCAPER" || rc.getRoundNum() > 400)) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(right));
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        	else {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(Direction.CENTER);
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        }
        else if (at.distanceSquaredTo(home) == 1) {	
        	MapLocation left = at.add(dir.rotateLeft().rotateLeft());
        	MapLocation right = at.add(dir.rotateRight().rotateRight());
        	if (rc.senseElevation(left) < rc.senseElevation(at) && rc.onTheMap(left) && (rc.senseRobotAtLocation(left).getType().name() == "LANDSCAPER" || rc.getRoundNum() > 400)) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(left));
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        	else if(rc.senseElevation(right) < rc.senseElevation(at) && rc.onTheMap(right) && (rc.senseRobotAtLocation(right).getType().name() == "LANDSCAPER" || rc.getRoundNum() > 400)) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(right));
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        	else {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(Direction.CENTER);
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        }
    }

    static boolean isEnemyHQFull(MapLocation en) throws GameActionException{
        MapLocation curr = rc.getLocation();
        if(!rc.isLocationOccupied(en)){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.NORTH))){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.NORTHEAST))){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.NORTHWEST))){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.SOUTH))){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.SOUTHEAST))){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.SOUTHWEST))){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.WEST))){
            return false;
        }else if(!rc.isLocationOccupied(en.add(Direction.EAST))){
            return false;
        }
        return true;
    }
    //__________________________________________________________________________________________________________________
    //DELIVERY DRONE CODE BELOW
    static void runDeliveryDrone() throws GameActionException {
        if(task.equals("scout")){
            MapLocation loc = rc.getLocation();
            scan(loc);
            if(EnemyHQ == null){
                findEnemyHQ(loc);
            }else{
                scout(loc);
            }

        }
        if (task.equals("killEnemy")){
            Team enemy = rc.getTeam().opponent();
            if (!rc.isCurrentlyHoldingUnit()) {
                System.out.println("NOT CARRYING ROBOT");
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, enemy);
                //tryMove(Direction.EAST);A
                //moveToDrone(getEnemyHQLocation());

                // TODO: replace next line with enemy HQ LOC
                //NOTE: this was tested on CENTRAL LAKE

                if(nearbyRobots.length == 0){
                    tryMoveD(Direction.NORTHEAST);
                    System.out.println("MOVING");
                }
                else{
                    System.out.println("I DETECT ROBOTS: " + nearbyRobots.length);
                    for (RobotInfo targetEnemy: nearbyRobots){
                        int enemyID = targetEnemy.getID();
                        moveToDrone(targetEnemy.getLocation());
                        //moveTo(targetEnemy.getLocation().add(rando));
                        //TODO: make picking up enemies faster and more consistent
                        if (rc.canPickUpUnit(targetEnemy.getID())) {
                            rc.pickUpUnit(targetEnemy.getID());
                            System.out.println("PICKED UP UNIT");
                        }
                    }
                }
            }else{
                System.out.println("IM CARRYING A ROBOT");
                //tryMoveD(Direction.SOUTH);
                outerloop:
                for (int i = 0; i <= 6; i += 2) {
                    //TODO: use map to find water
                    Direction dir = directions[i];
                    MapLocation adj = rc.adjacentLocation(dir);
                    while (rc.onTheMap(adj)) {
                        if (rc.senseFlooding(adj)) {
                            if (rc.canDropUnit(dir)) {
                                rc.dropUnit(dir);
                                System.out.println("DROPPED ENEMY INTO WATER");
                                break outerloop;

                            }
                        }
                        tryMoveD(dir);
                        adj = rc.adjacentLocation(dir);
                    }

                }
            }
        }
            //tryMove(Direction.EAST);
            //moveTo(getEnemyHQLocation());

        // if (!rc.isCurrentlyHoldingUnit()) {
        //     // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        //     RobotInfo[] nearbyRobots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

        //     if (nearbyRobots.length > 0) {
        //         // Pick up a first robot within range
        //         rc.pickUpUnit(nearbyRobots[0].getID());
        //         System.out.println("I picked up " + nearbyRobots[0].getID() + "!");
        //     }
        // } else {
        //     // No close robots, so search for robots within sight radius
        //     tryMove(randomDirection());
        // }
    }
    static HashMap<Integer, ArrayList<MapLocation>> scan(MapLocation at) throws GameActionException{
        RobotInfo[] r = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
        RobotInfo[] r2d2 = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
        HashMap<Integer, ArrayList<MapLocation>> news = new HashMap<>();
        for(int i = 1; i < 6; i++){
            news.put(i, new ArrayList<MapLocation>());
        }
        int myX = at.x;
        int myY = at.y;
        for(RobotInfo i : r){
            if(i.getType() == RobotType.NET_GUN){
                oppNet.add(i.getLocation());
                news.get(5).add(i.getLocation());
            }
            if((i.getType() == RobotType.DESIGN_SCHOOL || i.getType() == RobotType.NET_GUN || i.getType() == RobotType.FULFILLMENT_CENTER || i.getType() == RobotType.REFINERY) && quadrantIn(i.getLocation()) == quadrantIn(HQ)){
                offensiveEnemyBuildings.add(i.getLocation());
                news.get(3).add(i.getLocation());
            }
            if(i.getType() == RobotType.HQ){
                EnemyHQ = i.getLocation();
                news.get(1).add(i.getLocation());
            }
        }
        for(RobotInfo i : r2d2){
            if(i.getType() == RobotType.REFINERY){
                refineries.add(i.getLocation());
                news.get(4).add(i.getLocation());
            }
        }

        int x = 0;
        int y = 0;
        while( Math.abs(x) < 5){
            while(Math.abs(y) < 5){
                MapLocation n = new MapLocation(myX + x, myY + y);
                if(rc.onTheMap(n) && rc.canSenseLocation(n)){
                    if(rc.senseSoup(n) > 0){
                        if(!soup.contains(n) && !irSoup.contains(n)){
                            if(!senseSoupAround(n)) {
                                irSoup.add(n);
                            }else {
                                soup.add(n);
                                news.get(2).add(n);
                            }
                        }
                    }
                }
                if( y > 0){
                    y = y*(-1);
                }else{
                    y = y*(-1) + 1;
                }
            }
            if( x > 0){
                x = x*(-1);
            }else{
                x = x*(-1) +1;
            }
        }

        int j = 0;
        int q = 0;
        while( Math.abs(x) < 5){
            while(Math.abs(y) < 5){
                MapLocation n = new MapLocation(myX + x, myY + y);
                if(rc.onTheMap(n) && rc.canSenseLocation(n)){
                    if(rc.senseFlooding(n)){
                        water = n;
                        x = 5;
                        y = 5;
                    }
                }
                if( y > 0){
                    y = y*(-1);
                }else{
                    y = y*(-1) + 1;
                }
            }
            if( x > 0){
                x = x*(-1);
            }else{
                x = x*(-1) +1;
            }
        }
        return news;
    }

    static boolean senseSoupAround(MapLocation n) throws GameActionException{
        for(MapLocation d : soup){
            if(d.isAdjacentTo(n)){
                return false;
            }
        }
        return true;
    }
    static void scout(MapLocation at) throws GameActionException {
        if(scoutDest ==  null){
            scoutDest = mapCenter;
            scouted.add(scoutDest);
        }
        int xAdd = rc.getMapWidth()/8;
        int yAdd = rc.getMapHeight()/8;
        if (at.distanceSquaredTo(scoutDest) < 16) {
            int q = quadrantIn(scoutDest);
            if(q == 1){
                MapLocation newDest = new MapLocation( at.x ,rc.getMapHeight() - at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(at.x ,rc.getMapHeight() - at.y - yAdd);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            } else if(q == 2){
                MapLocation newDest = new MapLocation( rc.getMapWidth() - at.x ,at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(rc.getMapWidth() - at.x + xAdd, at.y);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            }else if(q == 3){
                MapLocation newDest = new MapLocation( at.x ,rc.getMapHeight() - at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(at.x ,rc.getMapHeight() - at.y + yAdd);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            }else if(q == 4){
                MapLocation newDest = new MapLocation( rc.getMapWidth() - at.x ,at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(rc.getMapWidth() - at.x - xAdd, at.y);
                    if(rc.onTheMap(scoutDest)) {
                        scouted.add(scoutDest);
                    }else{
                        scoutDest = mapCenter;
                        scouted = new ArrayList<>();
                    }
                }
            }
        }

        moveToDrone(scoutDest);
    }
    //__________________________________________________________________________________________________________________
    //NET GUN CODE BELOW
    static void runNetGun() throws GameActionException {
        RobotInfo[] r = rc.senseNearbyRobots();
        for(RobotInfo s : r){
            if(s.getTeam() != rc.getTeam() && rc.canShootUnit(s.getID())){
                rc.shootUnit(s.getID());
            }
        }
    }
    //__________________________________________________________________________________________________________________
    //MOVEMENT CODE BELOW
    static void zergRush(MapLocation dest) throws GameActionException{
        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);

        //dig through barriers
        if(rc.senseElevation(rc.adjacentLocation(moveDirection)) > 3 && rc.senseElevation(rc.adjacentLocation(moveDirection.rotateLeft())) - rc.senseElevation(loc) > 3 && rc.senseElevation(rc.adjacentLocation(moveDirection.rotateRight())) - rc.senseElevation(loc) > 3){
            if(rc.getDirtCarrying() == 25){
                rc.depositDirt(moveDirection.rotateLeft().rotateLeft());
            }
            rc.digDirt(moveDirection);
        }

//        if(rc.senseFlooding(rc.adjacentLocation(moveDirection)) && rc.senseFlooding(rc.adjacentLocation(moveDirection.rotateLeft())) && rc.senseFlooding(rc.adjacentLocation(moveDirection.rotateRight())) && rc.senseFlooding(rc.adjacentLocation(moveDirection.rotateRight())) && rc.senseFlooding(rc.adjacentLocation(moveDirection.rotateRight())) && rc.senseFlooding(rc.adjacentLocation(moveDirection.rotateRight())) && rc.senseFlooding(rc.adjacentLocation(moveDirection.rotateRight()))) {
//            if(rc.getDirtCarrying() > 0){
//                rc.depositDirt(moveDirection);
//            }
//            rc.digDirt(moveDirection.opposite().rotateLeft());
//        }
        //See if general direction is valid
        moveTo(dest);
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }
    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        MapLocation curr= rc.getLocation();
        MapLocation isFlooded = curr.add(dir);
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(isFlooded)) {
            rc.move(dir);
            return true;
        } else return false;
        //todo: add a case for it to move in the opposite direction when it senses flooding next to it
        //note: according to the hashmap, +4 positions from any one position leads to the OPPOSITE POSITION.
        //ig 1: NORTH, 5: SOUTH
        //of course, it'll have to loop around somehow.


    }

    static boolean tryMoveD(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
        //todo: add a case for it to move in the opposite direction when it senses flooding next to it
        //note: according to the hashmap, +4 positions from any one position leads to the OPPOSITE POSITION.
        //ig 1: NORTH, 5: SOUTH
        //of course, it'll have to loop around somehow.


    }

    static void moveTo(MapLocation dest) throws GameActionException{
        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);
        rc.setIndicatorLine(loc, dest, 0, 0, 0);
        System.out.println(moveDirection);
        //See if general direction is valid
        if(canMoveTo(loc, moveDirection)){
            System.out.println(1);
            path = moveDirection.opposite();
            tryMove(moveDirection);
        }else if(canMoveTo(loc, moveDirection.rotateLeft())){
            System.out.println(2);
            path = moveDirection.rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight())) {
            System.out.println(3);
            path = moveDirection.rotateRight().opposite();
            tryMove(moveDirection.rotateRight());
        }else if(canMoveTo(loc, moveDirection.rotateLeft().rotateLeft())) {
            System.out.println(4);
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight().rotateRight())) {
            System.out.println(5);
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight());
        }else if(canMoveTo(loc, moveDirection.rotateLeft().rotateLeft().rotateLeft())) {
            System.out.println(6);
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight().rotateRight().rotateRight())) {
            System.out.println(7);
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight().rotateRight());
        } else{
            System.out.println(8);
            tryMove(path);
        }
    }

    static boolean canMoveTo(MapLocation m, Direction dir) throws GameActionException{
        return (rc.canMove(dir) && !rc.senseFlooding(m.add(dir)) && dir != path);
    }

    static void moveToDrone(MapLocation dest) throws GameActionException{
        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);


        Direction bad = Direction.CENTER;
        Direction notgood = Direction.CENTER;

        //look for enemy netguns in range
        for(MapLocation gun : oppNet){
            if(loc.distanceSquaredTo(gun) < 26){
                if(Math.abs(gun.x - loc.x) < 5 && Math.abs(gun.x - loc.x) > 2){
                    if(gun.x - loc.x > 0){
                        bad = Direction.EAST;
                    }else{
                        bad = Direction.WEST;
                    }
                }
                if(Math.abs(gun.y - loc.y) < 5 && Math.abs(gun.y - loc.y) > 2){
                    if(gun.x - loc.x > 0){
                        notgood = Direction.NORTH;
                    }else{
                        notgood = Direction.SOUTH;
                    }
                }
            }
        }


        Direction[] nonoDirections = {Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST, bad, notgood, path};
        ArrayList<Direction> ew = new ArrayList<>();
        for( Direction d : nonoDirections){
            ew.add(d);
        }

        //See if general direction is valid
        if(rc.canMove(moveDirection)){
            path = moveDirection.opposite();
            tryMoveD(moveDirection);
        }else if(rc.canMove(moveDirection.rotateLeft()) && !ew.contains(moveDirection.rotateLeft())){
            path = moveDirection.rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight()) && !ew.contains(moveDirection.rotateRight())) {
            path = moveDirection.rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft()) && !ew.contains(moveDirection.rotateLeft().rotateLeft())){
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight()) && !ew.contains(moveDirection.rotateRight().rotateRight())) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft().rotateLeft()) && !ew.contains(moveDirection.rotateLeft().rotateLeft().rotateLeft())) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight().rotateRight()) && !ew.contains(moveDirection.rotateRight().rotateRight().rotateRight())) {
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight().rotateRight());
        } else{
            tryMoveD(moveDirection.opposite());
        }
    }

    static void goHome(MapLocation m) throws GameActionException {
        Direction d = randomDirection();
        for (Direction l : directions) {
            if (rc.canDepositSoup(l)) {
                System.out.println("I deposited soup");
                rc.depositSoup(l, rc.getSoupCarrying());
            }
        }
        tryMove(d);
    }
    //__________________________________________________________________________________________________________________
    //BLOCKCHAIN CODE BELOW
    static void postLocation(int code, int x, int y, int cost) throws GameActionException {
        /* Code to be placed in array[2]
         * 1 : HQ
         * 2 : Soup
         * 3 : Enemy HQ
         */
        int[] message = new int[7];
        message[1] = 998997;
        message[2] = code;
        message[3] = x;
        message[4] = y;
        if (rc.canSubmitTransaction(message, cost))
            rc.submitTransaction(message, cost);
    }

    static MapLocation getHQLocation() throws GameActionException {
        //returns the location of HQ
        MapLocation location = null;
        for(int k = 1; k < rc.getRoundNum()-1; k++) {
            Transaction[] block = rc.getBlock(k);
            if(block.length != 0) {
                for(int i = 0; i < block.length; i++) {
                    int[] message = block[i].getMessage();
                    if(message[1] == 998997 && message[2] == 1) {
                        location = new MapLocation(message[3], message[4]);
                        return location;
                    }
                }
            }
        }
        return location;
    }

    static MapLocation getSoupLocation() throws GameActionException {
        //does not currently work
        MapLocation location = null;
        for(int k = rc.getRoundNum()-20; k < rc.getRoundNum()-1; k++) {
            if(k > 0) {
                Transaction[] block = rc.getBlock(k);
                if(block.length != 0) {
                    for(int i = 0; i < block.length; i++) {
                        int[] message = block[i].getMessage();
                        if(message[1] == 998997 && message[2] == 2) {
                            location = new MapLocation(message[3], message[4]);
                            System.out.println(location);
                            return location;
                        }
                    }
                }
            }
        }
        return location;

    }

    static MapLocation getEnemyHQLocation() throws GameActionException {
        //returns the enemy HQ location as a MapLocation
        MapLocation location = null;
        for(int k = rc.getRoundNum()-60; k < rc.getRoundNum()-1; k++) {
            if(k > 0) {
                Transaction[] block = rc.getBlock(k);
                if(block.length != 0) {
                    for(int i = 0; i < block.length; i++) {
                        int[] message = block[i].getMessage();
                        if(message[1] == 998997 && message[2] == 3) {
                            location = new MapLocation(message[3], message[4]);
                            System.out.println(location);
                            return location;
                        }
                    }
                }
            }
        }
        return location;
    }

    static MapLocation getEnemyHQLocationDrone() throws GameActionException {
        //returns the enemy HQ location as a MapLocation
        MapLocation location = null;
        for(int k = 1; k < rc.getRoundNum()-1; k++) {
            if(k > 0) {
                Transaction[] block = rc.getBlock(k);
                if(block.length != 0) {
                    for(int i = 0; i < block.length; i++) {
                        int[] message = block[i].getMessage();
                        if(message[1] == 998997 && message[2] == 3) {
                            location = new MapLocation(message[3], message[4]);
                            System.out.println(location);
                            return location;
                        }
                    }
                }
            }
        }
        return location;
    }

    static void updateEnemyHQLocation() throws GameActionException {
        //looks for enemy hq location in block chain and moves it to a more recent round
        for(int k = rc.getRoundNum()-61; k < rc.getRoundNum()-59; k++) {
            if(k > 0) {
                Transaction[] block = rc.getBlock(k);
                if(block.length != 0) {
                    for(int i = 0; i < block.length; i++) {
                        int[] message = block[i].getMessage();
                        if(message[1] == 998997 && message[2] == 3) {
                            postLocation(3, message[3], message[4], 2);
                        }
                    }
                }
            }
        }
    }

    static void findEnemyHQ(MapLocation at) throws GameActionException{
        MapLocation home = getHQLocation();
        int hqX = home.x;
        int hqY = home.y;
        int mapW = rc.getMapWidth();
        int mapH = rc.getMapHeight();
        //straight across
        MapLocation dest1 = new MapLocation(mapW - hqX, hqY);
        //straight down and straight across
        MapLocation dest2 = new MapLocation(mapW-hqX, mapH-hqY);

        if(at.x == dest2.x && at.y==dest2.y){
            EnemyHQ = new MapLocation( hqX, mapH - hqY);
        }
        if(enHQDest == null){
            enHQDest = dest1;
        }
        if(at.distanceSquaredTo(dest1) < 16){
            enHQDest = dest2;
        }
        moveToDrone(enHQDest);
    }
    

    static PriorityQueue<Information> broadcastQueue = new PriorityQueue<>();

    static void updateBroadcast(HashMap<Integer,ArrayList<MapLocation>> out) throws GameActionException {
        for (Integer i :
                out.keySet()) {
            ArrayList<MapLocation> currlist=out.get(i);
            for (MapLocation l :
                    currlist) {
                broadcastQueue.add(new Information(i, l.x,l.y));
            }
        }
    }

    static boolean tryBroadcast(int cost) throws GameActionException {
        if (rc.getTeamSoup()>=cost && !broadcastQueue.isEmpty()) {
            int[] message = new int[7];
            BitSet bitSet=new BitSet(224);
            int infocount=0;
            int index=0;
            while(infocount<11 && !broadcastQueue.isEmpty()) {
                Information next = broadcastQueue.poll();
                index++;
                for (int i = 0; i < 3; i++) {
                    int type = next.getType();
                    bitSet.set(index, type % 2 == 1);
                    index++;
                    type /= 2;
                }
                for (int i = 0; i < 8; i++) {
                    int x = next.getX();
                    bitSet.set(index, x % 2 == 1);
                    index++;
                    x /= 2;
                }
                for (int i = 0; i < 8; i++) {
                    int y = next.getY();
                    bitSet.set(index, y % 2 == 1);
                    index++;
                    y /= 2;
                }
                infocount++;
            }
            bitSet.set(0 * 20, true);
            //bitSet.set(1*20,true);
            bitSet.set(2 * 20, true);
            bitSet.set(3 * 20, true);
            bitSet.set(4 * 20, true);
            //bitSet.set(5*20,true);
            bitSet.set(6 * 20, true);
            //bitSet.set(7*20,true);
            //bitSet.set(8*20,true);
            bitSet.set(9 * 20, true);
            bitSet.set(10 * 20, true);
            for (int i = 220; i < 224; i++) {
                bitSet.set(i, infocount % 2 == 1);
                infocount /= 2;
            }
            long[] longs=bitSet.toLongArray();
            message[0]= ((int)longs[0]);
            message[1]= ((int)(longs[0]>>>32));
            message[2]= ((int)longs[1]);
            message[3]= ((int)(longs[1]>>>32));
            message[4]= ((int)longs[2]);
            message[5]= ((int)(longs[2]>>>32));
            message[6]= ((int)longs[3]);

            rc.submitTransaction(message, cost);
            return true;
        }
        else{
            return false;
        }
    }

    static ArrayList<BitSet> findTransaction(Transaction[] transactions){
        ArrayList<BitSet> out=new ArrayList<>();
        for (int i = 0; i < 7 ; i++) {
            int[] curr=transactions[i].getMessage();
            long[] longs=new long[4];
            longs[0] = ((long)curr[0]) | (((long) curr[1] << 32));
            longs[1] = ((long)curr[2]) | (((long) curr[3] << 32));
            longs[2] = ((long)curr[4]) | (((long) curr[5] << 32));
            longs[3] = (long)curr[6];
            BitSet bitSet=BitSet.valueOf(longs);
            if (    bitSet.get(0 * 20)&&
                    !bitSet.get(1 * 20)&&
                    bitSet.get(2 * 20)&&
                    bitSet.get(3 * 20)&&
                    bitSet.get(4 * 20)&&
                    !bitSet.get(5 * 20)&&
                    bitSet.get(6 * 20)&&
                    !bitSet.get(7 * 20)&&
                    !bitSet.get(8 * 20)&&
                    bitSet.get(9 * 20)&&
                    bitSet.get(10 * 20)){
                out.add(bitSet);
            }
        }
        return out;

    }

    static void receiveBroadcast(int round) throws  GameActionException{
        Transaction[] transactions=rc.getBlock(round);
        ArrayList<BitSet> ourlist=findTransaction(transactions);
        for (BitSet ours :
                ourlist) {
            int count=0;
            for (int i = 220; i < 224; i++) {
                count*=2;
                if (ours.get(i)) count++;
            }
            for (int i = 0; i < count; i++) {
                int type=0;
                for (int j = 0; j < 3; j++) {
                    type*=2;
                    if (ours.get(i*20+j)) type++;
                }
                int x=0;
                for (int j = 3; j < 11; j++) {
                    x*=2;
                    if (ours.get(i*20+j)) x++;
                }
                int y=0;
                for (int j = 11; j < 19; j++) {
                    y*=2;
                    if (ours.get(i*20+j)) y++;
                }
                switch (type){
                    case 1:
                        EnemyHQ=new MapLocation(x,y);
                        break;
                    case 2:
                        soup.add(new MapLocation(x,y));
                        break;
                    case 3:
                        offensiveEnemyBuildings.add(new MapLocation(x,y));
                        break;
                    case 4:
                        refineries.add(new MapLocation(x,y));
                        break;
                    case 5:
                        oppNet.add(new MapLocation(x,y));
                        break;
                }


            }
        }
    }

    static int quadrantIn(MapLocation m) throws GameActionException{
        if(m.x < rc.getMapWidth()/2){
            if(m.y < rc.getMapHeight()/2){
                return 3;
            }else{
                return 2;
            }
        }else{
            if(m.y < rc.getMapHeight()/2){
                return 4;
            }else{
                return 1;
            }
        }
    }
    //__________________________________________________________________________________________________________________
}

