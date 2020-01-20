package Fortress;

import battlecode.common.*;
import com.sun.org.apache.xml.internal.utils.res.XResourceBundle;


import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

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
    static boolean dirtFull = false;
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
    static  MapLocation HQ;
    static MapLocation scoutDest;
    static MapLocation mapCenter;
    static RobotInfo heldUnit;
    static ArrayList<MapLocation> scouted = new ArrayList<>();

    static int robotsBuilt;
    static boolean moveOnce = false;

    /**MapLocation arrays containing all the relevent MapLocations **/

    static Direction randomInitialDirection;

    static MapLocation water;
    static ArrayList<MapLocation> soup = new ArrayList<>();
    static ArrayList<MapLocation> refineries = new ArrayList<>();
    static ArrayList<MapLocation> oppNet = new ArrayList<>();
    static ArrayList<MapLocation> offensiveEnemyBuildings = new ArrayList<>();
    static MapLocation EnemyHQ;

    static ArrayList<MapLocation> digLoc = new ArrayList<>();

    static ArrayList<MapLocation> irWater = new ArrayList<>();
    static ArrayList<MapLocation> irSoup = new ArrayList<>();
    static boolean checkHQ = true;
    static int initialRound;

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
        robotsBuilt = 0;
        schoolsBuilt = 0;
        factoriesBuilt = 0;
        souploc = null;
        enHQDest = null;
        EnemyHQ = null;
        path = Direction.CENTER;
        HQ = getHQLocation();
        scoutDest = null;
        mapCenter = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);

        //landscaper task determiner
        if(rc.getType() == RobotType.LANDSCAPER){
                task = "castle";
        }
        if(rc.getType() == RobotType.MINER){
            if (rc.getRoundNum()<=150){
                task="first3";
            }
        }
        //drone task determiner
        if(rc.getType() == RobotType.DELIVERY_DRONE){
            if(rc.getRoundNum() < 150){
                task = "scout";
            }else if(rc.getRoundNum() < 300 && rc.getRoundNum() > 150) {
                task = "hover";
            }
            else if(rc.getRoundNum() < 600 && rc.getRoundNum() > 199){
                task = "killEnemy";
            }else{
                task = "defend";
            }
//            task = "crunch";
//            task = "defend";
        }
//        if(rc.getType() == RobotType.DELIVERY_DRONE){
//            droneTask = "cow";
//            //find soup and water?
//            droneTask = "scoutWithMiner";
//            droneTask = "helpMyLandscaper";
//            droneTask = "killEnemy";
//        }

        initialLoc = rc.getLocation();
        initialRound=rc.getRoundNum();
        randomInitialDirection=randomDirection();
//        System.out.println("INITIAL LOCATION IS: " + initialLoc);

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
    	MapLocation base = rc.getLocation();
        if(rc.getRoundNum() == 1) {
    		postLocation(1, base.x, base.y, 1);
    	}
    	RobotInfo[] r = rc.senseNearbyRobots();
    	for(RobotInfo s : r){
    	    if(s.getTeam() != rc.getTeam() && rc.canShootUnit(s.getID())){
    	        rc.shootUnit(s.getID());
            }
        }
    	if (rc.getRoundNum() < 20) {
            for (Direction dir : randomDirections()) {
                if(tryBuild(RobotType.MINER, dir)) {
                    robotsBuilt++;
                }
            }
        }
        if(robotsBuilt < 3 && rc.getRoundNum() < 150){
            for (Direction dir : randomDirections()) {
                if(tryBuild(RobotType.MINER, dir)) {
                    robotsBuilt++;
                }
            }
        }else if(robotsBuilt < 6 && rc.getRoundNum() > 150){
            for (Direction dir : randomDirections()) {
                if(tryBuild(RobotType.MINER, dir)) {
                    robotsBuilt++;
                }
            }
        }
    	//TODO: add some way to rebroadcast important info like enemyHQ
    	//updateEnemyHQLocation();
        //}
    }

    static boolean defenseUp(MapLocation m) throws GameActionException{
        int numL = 0;
        for(Direction d : directions){
            MapLocation nex = m.add(d);
            RobotInfo rob =rc.senseRobotAtLocation(nex);
            if(rob != null && rob.getType() == RobotType.LANDSCAPER){
                numL++;
            }
        }
        return numL > 3;
    }

    //__________________________________________________________________________________________________________________
    //MINER CODE BELOW
    static void runMiner() throws GameActionException {
        if(rc.getRoundNum() > 10) {
            receiveBroadcast(rc.getRoundNum() - 1);
        }
//        if(EnemyHQ != null){
//            System.out.println(soup);
//            System.out.println(oppNet);
//            System.out.println(offensiveEnemyBuildings);
//            System.out.println(EnemyHQ);
//        }
        MapLocation curr = rc.getLocation();
        if(HQ == null){
            HQ = getHQLocation();
//            refineries.add(HQ);
        }
        boolean stay = openEyes(curr);
        //build design school
//        System.out.println("robots built: "+ robotsBuilt);
        if(rc.getTeamSoup() > 200 && curr.isAdjacentTo(HQ)){
            moveTo(curr.add(curr.directionTo(HQ).opposite()));
        }
        if (schoolsBuilt < 1 && task.equals("first3")) {
        	for(Direction d : directions) {
                if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, d) && curr.add(d).distanceSquaredTo(HQ) > 8 && curr.add(d).distanceSquaredTo(HQ) < 16) {
                    schoolsBuilt++;
                    addAndBroadcast(new Information(0,3,3));
                    tryBuild(RobotType.DESIGN_SCHOOL, d);
                }
            }
        }

        if (factoriesBuilt < 1 && task.equals("first3")) {
            for(Direction d : directions) {
                if (rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, d) && curr.add(d).distanceSquaredTo(HQ) > 8 && curr.add(d).distanceSquaredTo(HQ) < 64) {
                    factoriesBuilt++;
                    addAndBroadcast(new Information(0,4,4));
                    tryBuild(RobotType.FULFILLMENT_CENTER, d);
                }
            }
        }
        if(rc.getRoundNum() > 200 && rc.getTeamSoup() > 499) {
            if (curr.distanceSquaredTo(HQ) > 144) {
                moveTo(HQ);
            } else {

                for (Direction dir : directions) {
                    if (rc.canBuildRobot(RobotType.VAPORATOR, dir) && curr.add(dir).distanceSquaredTo(HQ) > 8 && curr.add(dir).distanceSquaredTo(HQ) < 81) {
                        rc.buildRobot(RobotType.VAPORATOR, dir);
                    }
                }
            }
        }
        //System.out.println("SCHOOLS BUILT: " + schoolsBuilt);

        //NOTE: schoolsBuilt is saved per miner, meaning each miner will want to make its own design school
        //MINE SOUP

        if (souploc != null && rc.getSoupCarrying() < 96){
            mineSoup();
        }
        //MOVE BACK TO HQ AND DEPOSIT SOUP
        else if (rc.getSoupCarrying() > 95){
            for (Direction dir : directions){
                if(rc.canDepositSoup(dir)){
                    rc.depositSoup(dir, rc.getSoupCarrying());
                }
            }
            if(!stay || curr.distanceSquaredTo(HQ) < 144) {
                moveTo(getClosestRefine(curr));
            }
        }
        //FIND SOUP
        else {
            if (soup.isEmpty()){
                //scout in an expanding circle starting at HQ
                scoutMiner(curr);
                //scout(curr);
            }
            else{
                souploc= getClosestSoup(curr);
                moveTo(souploc);
            }
        }

    }
    static boolean openEyes(MapLocation loc) throws GameActionException{
        HashMap<Integer, ArrayList<MapLocation>> news = new HashMap<>();
        for(int i = 1; i < 6; i++){
            news.put(i, new ArrayList<MapLocation>());
        }

        if(souploc != null && rc.canSenseLocation(souploc)){
            if(rc.senseSoup(souploc) == 0){
                soup.remove(souploc);
                souploc = null;
            }
        }
        MapLocation[] miso = rc.senseNearbySoup();
        int totS = rc.getSoupCarrying();
        for(MapLocation m : miso) {
            if (!soup.contains(m) && soup.size() < 6) {
                    soup.add(m);
                    news.get(2).add(m);
            }
            if(rc.canSenseLocation(m)) {
                totS += rc.senseSoup(m);
            }
        }
        RobotInfo[] r2d2 = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
        for(RobotInfo i : r2d2){
            if(i.getType() == RobotType.REFINERY){
                refineries.add(i.getLocation());
            }else if(i.getType() == RobotType.DESIGN_SCHOOL){
                schoolsBuilt++;
            }
            else if(i.getType() == RobotType.FULFILLMENT_CENTER){
                factoriesBuilt++;
            }
        }
        if ((refineries.isEmpty() && totS > 1) || (totS > 200 && loc.distanceSquaredTo(getClosestRefine(loc)) >= 196)) {
            for (Direction d : directions) {
                if (rc.canBuildRobot(RobotType.REFINERY, d) && loc.add(d).distanceSquaredTo(HQ) > 8) {
                    refineries.add(loc.add(d));
                    rc.buildRobot(RobotType.REFINERY, d);
                }
            }
            return true;
        }
        return false;
    }

    static MapLocation getClosestRefine( MapLocation m){
        if(refineries.isEmpty()){
            return HQ;
        }else {
            MapLocation o = null;
            int diss = 1000000;
            for (MapLocation n : refineries) {
                int ned = m.distanceSquaredTo(n);
                if (ned < diss) {
                    diss = ned;
                    o = n;
                }
            }
            return o;
        }
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
                if (diss<25){
                    return o;
                }
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
//        if (scoutDest == null) {
//            int q = quadrantIn(HQ);
//            if(q == 1){
//                scoutDest = new MapLocation(rc.getMapWidth(), rc.getMapHeight());
//            }else if(q == 2){
//                scoutDest = new MapLocation(1, rc.getMapHeight());
//            }else if(q == 3){
//                scoutDest = new MapLocation(1, 1);
//            }else if(q == 4){
//                scoutDest = new MapLocation(rc.getMapWidth(), 1);
//            }
//        }
//        int xAdd = rc.getMapWidth() / 6;
//        int yAdd = rc.getMapHeight() / 6;
//        if (at.distanceSquaredTo(scoutDest) < 16) {
//            int q = quadrantIn(scoutDest);
//            if (q == 1) {
//                MapLocation newDest = new MapLocation(at.x, rc.getMapHeight() - at.y);
//                if (!scouted.contains(newDest)) {
//                    scoutDest = new MapLocation(at.x, rc.getMapHeight() - at.y + yAdd);
//                    if (rc.onTheMap(scoutDest)) {
//                        scouted.add(scoutDest);
//                    } else {
//                        scoutDest = HQ;
//                        scouted = new ArrayList<>();
//                    }
//                }
//            } else if (q == 2) {
//                MapLocation newDest = new MapLocation(rc.getMapWidth() - at.x, at.y);
//                if (!scouted.contains(newDest)) {
//                    scoutDest = new MapLocation(rc.getMapWidth() - at.x - xAdd, at.y);
//                    if (rc.onTheMap(scoutDest)) {
//                        scouted.add(scoutDest);
//                    } else {
//                        scoutDest = HQ;
//                        scouted = new ArrayList<>();
//                    }
//                }
//            } else if (q == 3) {
//                MapLocation newDest = new MapLocation(at.x, rc.getMapHeight() - at.y);
//                if (!scouted.contains(newDest)) {
//                    scoutDest = new MapLocation(at.x, rc.getMapHeight() - at.y - yAdd);
//                    if (rc.onTheMap(scoutDest)) {
//                        scouted.add(scoutDest);
//                    } else {
//                        scoutDest = HQ;
//                        scouted = new ArrayList<>();
//                    }
//                }
//            } else if (q == 4) {
//                MapLocation newDest = new MapLocation(rc.getMapWidth() - at.x, at.y);
//                if (!scouted.contains(newDest)) {
//                    scoutDest = new MapLocation(rc.getMapWidth() - at.x + xAdd, at.y);
//                    if (rc.onTheMap(scoutDest)) {
//                        scouted.add(scoutDest);
//                    } else {
//                        scoutDest = HQ;
//                        scouted = new ArrayList<>();
//                    }
//                }
//            }
//        }
//        if(isPath(at, scoutDest)) {
//            moveTo(scoutDest);
//        }else{
//            scoutDest = HQ;
//            scouted.add(scoutDest);
//            moveTo(scoutDest);
//        }
        moveTo(at.add(randomInitialDirection));

    }

    static boolean isPath(MapLocation at, MapLocation to) throws  GameActionException{
        Direction moo = at.directionTo(to);
        Direction[] paths = {moo, moo.rotateLeft(), moo.rotateRight(), Direction.CENTER};
        for( Direction i : paths){
                MapLocation here = at.add(i);
                if(!(rc.senseFlooding(here)) && Math.abs(rc.senseElevation(here) - rc.senseElevation(at)) < 4){
                    return true;
            }
        }
        return false;
    }

    static int qHQ(MapLocation m){
        if(m.x < HQ.x){
            if(m.y < HQ.y){
                return 3;
            }else{
                return 2;
            }
        }else{
            if(m.y < HQ.y){
                return 4;
            }else{
                return 1;
            }
        }
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
        if(robotsBuilt < 4 && rc.getRoundNum() < 400 && rc.getTeamSoup() > 155) {
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
                }
        }else if(robotsBuilt < 8 && rc.getRoundNum() >= 300 && rc.getTeamSoup() > 205){
            for (Direction dir : directions)

                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
                }
        }
    }
    //Builds Drones
    static void runFulfillmentCenter() throws GameActionException {
        if (robotsBuilt < 1 && rc.getRoundNum() < 125) {
            for (Direction dir : randomDirections())
                if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
//                    broadcastQueue.add(new Information(0,1,1));
//                    tryBroadcast(1);
                    robotsBuilt++;
                    rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
//                    break;
                }
        } else if (rc.getRoundNum() < 200 && rc.getRoundNum() > 124 && robotsBuilt < 3 && rc.getTeamSoup() > 205) {
            for (Direction dir : randomDirections()) {
                if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                    robotsBuilt++;
                    rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                }
            }
        }/**else if(rc.getRoundNum() > 300  && rc.getTeamSoup() > 200 && robotsBuilt < 8){
         for (Direction dir : randomDirections()) {
         if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
         robotsBuilt++;
         rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
         }
         }
         }**/
        else if (rc.getRoundNum() > 249 && rc.getTeamSoup() > 210) {
            for (Direction dir : randomDirections()) {
                if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                    robotsBuilt++;
                    rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                }
            }
        }
    }
//        if(robotsBuilt < 20) {
//            for (Direction dir : directions)
//                if (tryBuild(RobotType.DELIVERY_DRONE, dir)) {
//                    robotsBuilt++;
//                }
//        }

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
        if(at.distanceSquaredTo(home) <= 2) {
        	task = "castle";
        }
        if(at.distanceSquaredTo(home) <= 8) {
        	zergRush(at.add(dir.opposite()));
        }
        if(at.distanceSquaredTo(home) > 8) {
        	if(digLoc.isEmpty() && at.distanceSquaredTo(home) > 18) {
        		zergRush(home);
        	}
        	
        	else if(digLoc.isEmpty()) {
        		if(rc.isReady() && rc.canDigDirt(dir)) {
            		rc.digDirt(dir);
            		digLoc.add(at.add(dir));
        		}
        	}
        	
        	MapLocation frontRow[] = new MapLocation[9];
        	frontRow[0] = at;
        	frontRow[1] = frontRow[0].add(dir.rotateRight().rotateRight());
        	frontRow[2] = frontRow[0].add(dir.rotateLeft().rotateLeft());
        	frontRow[3] = frontRow[1].add(dir.rotateRight().rotateRight());
        	frontRow[4] = frontRow[2].add(dir.rotateLeft().rotateLeft());
        	frontRow[5] = frontRow[3].add(dir.rotateRight().rotateRight());
        	frontRow[6] = frontRow[4].add(dir.rotateLeft().rotateLeft());
        	
        	if(rc.getDirtCarrying() == 25) {
        		dirtFull = true;
        	}
        	
        	if(rc.getDirtCarrying() == 0) {
        		dirtFull = false;
        	}
        	
        	if(!dirtFull) {
        		if(at.distanceSquaredTo(digLoc.get(0)) > 2 && at.distanceSquaredTo(home) > 18) {
        			zergRush(digLoc.get(0));
        		}
        		else if(rc.isReady() && rc.canDigDirt(dir)) {
            		rc.digDirt(dir);
        		}
        	}
        	
        	
        	
        	if(dirtFull) {
        		for(int i = 1; i < 7; i++) {
        			if(rc.canSenseLocation(frontRow[i]) && !digLoc.contains(frontRow[i]) && rc.senseElevation(frontRow[i]) > -10) {
        				if(rc.senseElevation(frontRow[i]) < rc.senseElevation(frontRow[0]) && rc.senseRobotAtLocation(frontRow[i]) == null) {
        					if(at.distanceSquaredTo(frontRow[i]) > 2 && rc.isReady()) {
        						zergRush(frontRow[i]);
        					}
        					else if(rc.canDepositDirt(at.directionTo(frontRow[i]))){
        						rc.depositDirt(at.directionTo(frontRow[i]));
        					}
        				}
        			}
        		}
        		if(rc.canSenseLocation(at.add(dir.opposite()))) {
        			if(rc.senseElevation(at) - rc.senseElevation(at.add(dir.opposite())) < 3 && rc.isReady()) {
        				rc.depositDirt(Direction.CENTER);
        			}
        			else if(rc.isReady()) {
        				tryMove(dir.opposite());
        				tryMove(dir.opposite().rotateLeft());
        				tryMove(dir.opposite().rotateRight());
        			}
        		}
        		else {
        			dirtFull = false;
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
        if(at.distanceSquaredTo(home) > 16){
            zergRush(home);
        }
        else if (at.distanceSquaredTo(home) > 2){
        	for(int i = 0; i < 8; i++) {
        		if(rc.canSenseLocation(build[i]) && !rc.isLocationOccupied(build[i])) {
        			moveTo(build[i]);
        			break;
        		}
        	}
//        	if(rc.isReady()) {
//        		task = "terraform";
//        	}
        }
        else if(rc.canDigDirt(dir) && rc.getDirtCarrying() < 25) {
        	rc.digDirt(dir);
        }
        else if (at.distanceSquaredTo(home) > 1) {	
        	MapLocation left = at.add(dir.rotateLeft());
        	MapLocation right = at.add(dir.rotateRight());
        	if (rc.onTheMap(left) && (rc.senseElevation(left) < rc.senseElevation(at)) && rc.onTheMap(right) && rc.senseElevation(left) < rc.senseElevation(right) && ((rc.getRoundNum() > 650) || rc.senseElevation(left)<3)) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(left));
        		else if(rc.canDigDirt(dir.opposite().rotateLeft().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight().rotateRight());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        	}
        	else if(rc.onTheMap(right) && rc.senseElevation(right) < rc.senseElevation(at) && ((rc.getRoundNum() > 650) || rc.senseElevation(right)<3)) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(right));
        		else if(rc.canDigDirt(dir.opposite().rotateLeft().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight().rotateRight());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        	}
        	else {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(Direction.CENTER);
        		else if(rc.canDigDirt(dir.opposite().rotateLeft().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight().rotateRight());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        	}
        }
        else if (at.distanceSquaredTo(home) == 1) {	
        	MapLocation left = at.add(dir.rotateLeft().rotateLeft());
        	MapLocation right = at.add(dir.rotateRight().rotateRight());
        	MapLocation dleft = at.add(dir.rotateLeft());
        	MapLocation dright = at.add(dir.rotateRight());
        	
        	if (rc.onTheMap(dleft) && (rc.getRoundNum() > 650) || rc.senseElevation(dleft)<3) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(dleft));
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        	else if(rc.onTheMap(dright) && (rc.getRoundNum() > 650) || rc.senseElevation(dright)<3) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(dright));
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        	if (rc.onTheMap(left) && rc.senseElevation(left) < rc.senseElevation(at) && rc.onTheMap(right) && rc.senseElevation(left) < rc.senseElevation(right) && ((rc.getRoundNum() > 650) || rc.senseElevation(left)<3)) {
        		if(rc.getDirtCarrying() > 0)
                    rc.depositDirt(at.directionTo(left));
        		else if(rc.canDigDirt(dir.opposite()))
                    rc.digDirt(dir.opposite());
        		else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft());
        		else if(rc.canDigDirt(dir.opposite().rotateRight()))
                    rc.digDirt(dir.opposite().rotateRight());
        	}
        	else if(rc.onTheMap(right) && rc.senseElevation(right) < rc.senseElevation(at) && ((rc.getRoundNum() > 650) || rc.senseElevation(right)<3)) {
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

    static boolean isHQFull(MapLocation at) throws GameActionException{
        if(rc.onTheMap(HQ.add(Direction.NORTH)) && rc.canSenseLocation(HQ.add(Direction.NORTH)) &&!rc.isLocationOccupied(HQ.add(Direction.NORTH))){
            return false;
        }else if(rc.onTheMap(HQ.add(Direction.NORTHEAST)) && rc.canSenseLocation(HQ.add(Direction.NORTHEAST)) && !rc.isLocationOccupied(HQ.add(Direction.NORTHEAST))){
            return false;
        }else if(rc.onTheMap(HQ.add(Direction.NORTHWEST)) && rc.canSenseLocation(HQ.add(Direction.NORTHWEST)) &&!rc.isLocationOccupied(HQ.add(Direction.NORTHWEST))){
            return false;
        }else if(rc.onTheMap(HQ.add(Direction.SOUTH)) && rc.canSenseLocation(HQ.add(Direction.SOUTH)) &&!rc.isLocationOccupied(HQ.add(Direction.SOUTH))){
            return false;
        }else if(rc.onTheMap(HQ.add(Direction.SOUTHEAST)) && rc.canSenseLocation(HQ.add(Direction.SOUTHEAST)) &&!rc.isLocationOccupied(HQ.add(Direction.SOUTHEAST))){
            return false;
        }else if(rc.onTheMap(HQ.add(Direction.SOUTHWEST)) && rc.canSenseLocation(HQ.add(Direction.SOUTHWEST)) &&!rc.isLocationOccupied(HQ.add(Direction.SOUTHWEST))){
            return false;
        }else if(rc.onTheMap(HQ.add(Direction.WEST)) && rc.canSenseLocation(HQ.add(Direction.WEST)) &&!rc.isLocationOccupied(HQ.add(Direction.WEST))){
            return false;
        }else if(rc.onTheMap(HQ.add(Direction.EAST)) && rc.canSenseLocation(HQ.add(Direction.EAST)) &&!rc.isLocationOccupied(HQ.add(Direction.EAST))){
            return false;
        }
        return true;
    }
    //__________________________________________________________________________________________________________________
    //DELIVERY DRONE CODE BELOW
    static void runDeliveryDrone() throws GameActionException {
        if(rc.getRoundNum() > 1000 && !task.equals("defend")){
            task = "crunch";
        }
//        if(rc.getRoundNum() > 800 && task.equals("hover")){
//            task = "defend";
//        }
        if(task.equals("scout")){
            if(rc.getRoundNum()%4 == 0 || broadcastQueue.size()>11){
                tryBroadcast(1);
            }
            MapLocation loc = rc.getLocation();
            updateBroadcast(scan(loc));
            scout(loc);

        }
        if(task.equals("defend")){
            MapLocation at = rc.getLocation();
            if(at.distanceSquaredTo(HQ) > 8){
                moveToDrone(HQ);
            }
        }
        if(task.equals(("crunch"))){
            MapLocation loc = rc.getLocation();
            scan(loc);
            if(rc.isCurrentlyHoldingUnit()){
                if(loc.isAdjacentTo(water)){
                    rc.dropUnit(loc.directionTo(water));
                }
                for(Direction g : directions){
                    if(rc.senseFlooding(loc.add(g))){
                        rc.dropUnit(g);
                    }
                }
                moveToDrone(water);
            }
            RobotInfo[] C3PO = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
            for(RobotInfo z : C3PO){
                if(rc.canPickUpUnit(z.getID())){
                    rc.pickUpUnit(z.getID());
                }
            }
            moveToCrunch(EnemyHQ);
        }
        if(task.equals("hover")){
            MapLocation at = rc.getLocation();
            scan(at);
            Team enemy = rc.getTeam().opponent();
                if(rc.isCurrentlyHoldingUnit()) {
                    if(heldUnit.getType() == RobotType.MINER && heldUnit.getTeam() == rc.getTeam()) {
                        for (Direction g : directions) {
                            if (rc.canDropUnit(g) && at.add(g).distanceSquaredTo(HQ) > 8) {
                                rc.dropUnit(g);
                            }
                        }
                        moveToDrone(at.add(at.directionTo(HQ).opposite()));
                    }else if(heldUnit.getType() == RobotType.LANDSCAPER && heldUnit.getTeam() == rc.getTeam()){
                        for (Direction g : directions) {
                            if (rc.canDropUnit(g) && at.add(g).isAdjacentTo(HQ)) {
                                rc.dropUnit(g);
                            }
                        }
                        moveToDroneHover(at.add(at.directionTo(HQ)));
                    }else
                    if (water != null) {
                        if (at.isAdjacentTo(water) && rc.canDropUnit(at.directionTo(water)) && heldUnit.getTeam() != rc.getTeam()) {
                            rc.dropUnit(at.directionTo(water));
                        }
                        if (rc.senseFlooding(at) && rc.canDropUnit(Direction.CENTER)) {
                            rc.dropUnit(Direction.CENTER);
                        }
                        for (Direction g : directions) {
                            if (rc.senseFlooding(at.add(g)) && rc.canDropUnit(g)) {
                                rc.dropUnit(g);
                            }
                        }
                        moveToDroneHover(water);
                    } else {
                        scout(at);
                    }
                }else {
                        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), enemy);
                        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
                        ArrayList<RobotInfo> nearbyRobots = new ArrayList<>();
                        for (RobotInfo r : nearbyEnemies) {
                            if ((r.getType() == RobotType.MINER || r.getType() == RobotType.LANDSCAPER) && quadrantIn(r.getLocation()) == quadrantIn(HQ)) {
                                nearbyRobots.add(r);
                            }
                        }
                        for (RobotInfo r : nearbyFriendlies) {
                            if (quadrantIn(r.getLocation()) == quadrantIn(HQ) && r.getType() == RobotType.MINER && r.getLocation().isAdjacentTo(HQ)) {
                                nearbyRobots.add(r);
                            }
                        }
                        if (nearbyRobots.size() > 0) {
                            for (RobotInfo targetEnemy : nearbyRobots) {
                                int enemyID = targetEnemy.getID();
                                if (rc.canPickUpUnit(targetEnemy.getID())) {
                                    heldUnit = targetEnemy;
                                    rc.pickUpUnit(targetEnemy.getID());
                                }
                            }
                            moveToDroneHover(closestEnemyRobot(at, nearbyRobots));
                        }
                        if (!isHQFull(at)) {
                            for(RobotInfo r : nearbyFriendlies){
                                if(r.getType() == RobotType.LANDSCAPER && !r.getLocation().isAdjacentTo(HQ)){
                                    if(rc.canPickUpUnit(r.getID())){
                                        heldUnit = r;
                                        rc.pickUpUnit(r.getID());
                                    }
                                }
                            }
                        }
                            moveToDroneHover(HQ);

                }
        }
        if (task.equals("killEnemy")){
            MapLocation at = rc.getLocation();
            scan(at);
            Team enemy = rc.getTeam().opponent();

            if(at.distanceSquaredTo(HQ) < 16){
                checkHQ = false;
            }
            if(checkHQ){
                moveToDrone(HQ);
            }
//            System.out.println(EnemyHQ);
            if (!rc.isCurrentlyHoldingUnit()) {
//                System.out.println("NOT CARRYING ROBOT");
                RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), enemy);
                RobotInfo[] nearbyCows = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), Team.NEUTRAL);
                ArrayList<RobotInfo> nearbyRobots = new ArrayList<>();
                for(RobotInfo r : nearbyEnemies){
                    if(r.getType() == RobotType.MINER || r.getType() == RobotType.LANDSCAPER) {
                        nearbyRobots.add(r);
                    }
                }
                for(RobotInfo r : nearbyCows){
                    if(quadrantIn(r.getLocation()) == quadrantIn(HQ)) {
                        nearbyRobots.add(r);
                    }
                }
                // TODO: replace next line with enemy HQ LOC
                //NOTE: this was tested on CENTRAL LAKE

                if(nearbyRobots.isEmpty()){
                    if(EnemyHQ != null) {
                        moveToDrone(EnemyHQ);
//                        System.out.println("MOVING");
                    }else{
                        findEnemyHQ(at);
                    }
                }
                else{
//                    System.out.println("I DETECT ROBOTS: " + nearbyRobots.length);
                    for (RobotInfo targetEnemy: nearbyRobots){
                        int enemyID = targetEnemy.getID();
                        //moveTo(targetEnemy.getLocation().add(rando));
                        //TODO: make picking up enemies faster and more consistent
                        if (rc.canPickUpUnit(targetEnemy.getID())) {
                                rc.pickUpUnit(targetEnemy.getID());
//                            System.out.println("PICKED UP UNIT");
                        }
                    }
                    moveToDrone(closestEnemyRobot(at, nearbyRobots));

                }
            }
            else {
                if (water != null || rc.senseFlooding(at)) {
                    if (at.isAdjacentTo(water) && rc.canDropUnit(at.directionTo(water))) {
                        rc.dropUnit(at.directionTo(water));
                    }
                    if(rc.senseFlooding(at) && rc.canDropUnit(Direction.CENTER)){
                        rc.dropUnit(Direction.CENTER);
                    }
                    /*
                    for (Direction g : directions) {
                        if (rc.senseFlooding(at.add(g)) && rc.canDropUnit(g)) {
                            rc.dropUnit(g);
                        }
                    }
                    */
                    moveToDrone(water);
                }else{
                    scout(at);
                }
            }
        }
        if (task.equals("dropCow")){
            MapLocation at = rc.getLocation();
            scan(at);
            Team enemy = rc.getTeam().opponent();

            if(at.distanceSquaredTo(HQ) < 16){
                checkHQ = false;
            }
            if(checkHQ){
                moveToDrone(HQ);
            }
//            System.out.println(EnemyHQ);
            if (!rc.isCurrentlyHoldingUnit()) {
//                System.out.println("NOT CARRYING ROBOT");
                //RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), enemy);
                RobotInfo[] nearbyCows = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), Team.NEUTRAL);
                ArrayList<RobotInfo> nearbyRobots = new ArrayList<>();
                for(RobotInfo r : nearbyCows){
                    //if(quadrantIn(r.getLocation()) == quadrantIn(HQ)) {
                        nearbyRobots.add(r);
                    //}
                }

                //tryMove(Direction.EAST);
                //moveToDrone(getEnemyHQLocation());

                // TODO: replace next line with enemy HQ LOC
                //NOTE: this was tested on CENTRAL LAKE

                if(nearbyRobots.isEmpty()){
                    scout(at);
                }
                else{
//                    System.out.println("I DETECT ROBOTS: " + nearbyRobots.length);
                    for (RobotInfo targetEnemy: nearbyRobots){
                        int enemyID = targetEnemy.getID();
                        //moveTo(targetEnemy.getLocation().add(rando));
                        //TODO: make picking up enemies faster and more consistent
                        if (rc.canPickUpUnit(targetEnemy.getID())) {
                            rc.pickUpUnit(targetEnemy.getID());
//                            System.out.println("PICKED UP UNIT");
                        }
                    }
                    moveToDrone(closestEnemyRobot(at, nearbyRobots));

                }
            }
            else {
                if(EnemyHQ != null) {
                    if(at.distanceSquaredTo(EnemyHQ) < 49 && rc.canDropUnit(Direction.CENTER)){
                        rc.dropUnit(Direction.CENTER);
                    }
                    moveToDrone(EnemyHQ);
//                        System.out.println("MOVING");
                }else{
                    findEnemyHQ(at);
                    if(at.distanceSquaredTo(EnemyHQ) < 49 && rc.canDropUnit(Direction.CENTER)){
                        rc.dropUnit(Direction.CENTER);
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

    static MapLocation closestEnemyRobot(MapLocation at, ArrayList<RobotInfo> m){
        MapLocation o = null;
        int diss = 1000000;
        for (RobotInfo n : m){
            int ned = at.distanceSquaredTo(n.getLocation());
            if(ned < diss){
                diss = ned;
                o = n.getLocation();
            }
        }
        return o;
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
//            System.out.println(i.getType());
            if(i.getType() == RobotType.NET_GUN && !oppNet.contains(i.getLocation())){
                oppNet.add(i.getLocation());
                news.get(5).add(i.getLocation());
            }
            if((i.getType() == RobotType.DESIGN_SCHOOL || i.getType() == RobotType.NET_GUN || i.getType() == RobotType.FULFILLMENT_CENTER || i.getType() == RobotType.REFINERY) && quadrantIn(i.getLocation()) == quadrantIn(HQ) && !offensiveEnemyBuildings.contains(i.getLocation())){
                offensiveEnemyBuildings.add(i.getLocation());
                news.get(3).add(i.getLocation());
            }
            if(i.getType() == RobotType.HQ && EnemyHQ == null){
//                System.out.println("FOUND HQ");
                EnemyHQ = i.getLocation();
                oppNet.add(i.getLocation());
                news.get(1).add(i.getLocation());
                news.get(5).add(i.getLocation());
            }
        }
        for(RobotInfo i : r2d2){
            if(i.getType() == RobotType.REFINERY){
                refineries.add(i.getLocation());
                news.get(4).add(i.getLocation());
            }
        }

        MapLocation[] miso = rc.senseNearbySoup();
        int totS = 0;
        for(MapLocation s : soup){
            if(rc.canSenseLocation(s) && rc.senseSoup(s) == 0){
                soup.remove(s);
            }
        }
        for(MapLocation m : miso) {
            if (!soup.contains(m) && soup.size() < 5) {
                soup.add(m);
                news.get(2).add(m);
            }
        }
        int x = 0;
        int y = 0;
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
        int xAdd = rc.getMapWidth()/6;
        int yAdd = rc.getMapHeight()/6;
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
        if(rc.senseElevation(rc.adjacentLocation(moveDirection)) > 2 && rc.senseElevation(rc.adjacentLocation(moveDirection.rotateLeft())) - rc.senseElevation(loc) > 3 && rc.senseElevation(rc.adjacentLocation(moveDirection.rotateRight())) - rc.senseElevation(loc) > 3){
            if(rc.getDirtCarrying() == 25 && rc.canDepositDirt(moveDirection.rotateLeft().rotateLeft())){
                rc.depositDirt(moveDirection.rotateLeft().rotateLeft());
            }
            if(rc.canDigDirt(moveDirection)) {
            	rc.digDirt(moveDirection);
            }
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
    static MapLocation prevdest=null;
    static HashSet<MapLocation> prevLocations = new HashSet<>();
    static void moveTo(MapLocation dest) throws GameActionException{
        if (!dest.equals(prevdest)){
            prevdest=dest;
            prevLocations.clear();
        }

        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);
        rc.setIndicatorLine(loc, dest, 0, 0, 0);
        //See if general direction is valid

        prevLocations.add(loc);

        if(canMoveTo(loc, moveDirection)){
            path = moveDirection.opposite();
            tryMove(moveDirection);
        }else if(canMoveTo(loc, moveDirection.rotateLeft())){
            path = moveDirection.rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight())) {
            path = moveDirection.rotateRight().opposite();
            tryMove(moveDirection.rotateRight());
        }else if(canMoveTo(loc, moveDirection.rotateLeft().rotateLeft())) {
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight().rotateRight())) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight());
        }else if(canMoveTo(loc, moveDirection.rotateLeft().rotateLeft().rotateLeft())) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight().rotateRight().rotateRight())) {
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight().rotateRight());
        }else if(canMoveTo(loc, moveDirection.opposite())) {
            path = moveDirection;
            tryMove(moveDirection.opposite());
        } else{
            prevLocations.remove(loc.add(path));
            tryMove(path);

        }
    }

    static boolean canMoveTo(MapLocation m, Direction dir) throws GameActionException{
        return (rc.canMove(dir) && !rc.senseFlooding(m.add(dir)) && dir != path && !prevLocations.contains(m.add(dir)));
    }

    static void moveToDrone(MapLocation dest) throws GameActionException{
        if (!dest.equals(prevdest)){
            prevdest=dest;
            prevLocations.clear();
        }
        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);
        Direction[] nonoDirections;
        if(loc.distanceSquaredTo(HQ) < 64) {
            nonoDirections = new Direction[]{path};
        }else {
            nonoDirections = new Direction[]{Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST, path};
        }
        ArrayList<Direction> ew = new ArrayList<>();
        for( Direction d : nonoDirections){
            ew.add(d);
        }
        prevLocations.add(loc);
        //See if general direction is valid
        if(rc.canMove(moveDirection) && !ew.contains(moveDirection) && !netGunInRange(loc.add(moveDirection))&& !prevLocations.contains(loc.add(moveDirection))){
            path = moveDirection.opposite();
            tryMoveD(moveDirection);
        }else if(rc.canMove(moveDirection.rotateLeft()) && !ew.contains(moveDirection.rotateLeft()) && !netGunInRange(loc.add(moveDirection.rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft()))){
            path = moveDirection.rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight()) && !ew.contains(moveDirection.rotateRight()) && !netGunInRange(loc.add(moveDirection.rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight()))) {
            path = moveDirection.rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft()) && !ew.contains(moveDirection.rotateLeft().rotateLeft()) && !netGunInRange(loc.add(moveDirection.rotateLeft().rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft().rotateLeft()))){
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight()) && !ew.contains(moveDirection.rotateRight().rotateRight()) && !netGunInRange(loc.add(moveDirection.rotateRight().rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight().rotateRight()))) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft().rotateLeft()) && !ew.contains(moveDirection.rotateLeft().rotateLeft().rotateLeft()) && !netGunInRange(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft()))) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight().rotateRight()) && !ew.contains(moveDirection.rotateRight().rotateRight().rotateRight()) && !netGunInRange(loc.add(moveDirection.rotateRight().rotateRight().rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight().rotateRight().rotateRight()))){
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.opposite()) && !ew.contains(moveDirection.opposite()) && !netGunInRange(loc.add(moveDirection.opposite()))&& !prevLocations.contains(loc.add(moveDirection.opposite()))) {
            path = moveDirection;
            tryMoveD(moveDirection.opposite());
        } else{
            prevLocations.remove(loc.add(path));
            tryMoveD(path);
        }
    }

    static void moveToCrunch(MapLocation dest) throws GameActionException{
        if (!dest.equals(prevdest)){
            prevdest=dest;
            prevLocations.clear();
        }
        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);
        prevLocations.add(loc);
        //See if general direction is valid
        if(rc.canMove(moveDirection)){
            path = moveDirection.opposite();
            tryMoveD(moveDirection);
        }else if(rc.canMove(moveDirection.rotateLeft())){
            path = moveDirection.rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight())) {
            path = moveDirection.rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft())){
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight())) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft().rotateLeft())) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight().rotateRight())){
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight().rotateRight());
        } else{
            prevLocations.remove(loc.add(path));
            tryMoveD(path);
        }
    }
    static void moveToDroneHover(MapLocation dest) throws GameActionException{

        if (!dest.equals(prevdest) || (dest==HQ && dest.distanceSquaredTo(rc.getLocation())>30)){
            prevdest=dest;
            prevLocations.clear();
        }

        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);
        prevLocations.add(loc);
        //See if general direction is valid
        if(rc.canMove(moveDirection) && !hQInRange(loc.add(moveDirection))&& !prevLocations.contains(loc.add(moveDirection))){
            path = moveDirection.opposite();
            tryMoveD(moveDirection);
        }else if(rc.canMove(moveDirection.rotateLeft()) && !hQInRange(loc.add(moveDirection.rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft()))){
            path = moveDirection.rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight()) && !hQInRange(loc.add(moveDirection.rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight()))) {
            path = moveDirection.rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft()) && !hQInRange(loc.add(moveDirection.rotateLeft().rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft().rotateLeft()))){
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight()) && !hQInRange(loc.add(moveDirection.rotateRight().rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight().rotateRight()))) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft().rotateLeft()) && !hQInRange(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft()))) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight().rotateRight()) && !hQInRange(loc.add(moveDirection.rotateRight().rotateRight().rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight().rotateRight().rotateRight()))){
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.opposite()) && !prevLocations.contains(loc.add(moveDirection.opposite()))) {
            path = moveDirection;
            tryMoveD(moveDirection.opposite());
        } else{
            prevLocations.remove(loc.add(path));
            tryMoveD(path);
        }
    }

    static boolean netGunInRange(MapLocation move){
//        System.out.println(oppNet);
//        System.out.println(EnemyHQ);
        if(oppNet.isEmpty()){
            return false;
        }
        for(MapLocation m : oppNet){
            if(move.distanceSquaredTo(m) < 16){
                return true;
            }
        }
        return false;
    }
    static boolean hQInRange(MapLocation move){
//        System.out.println(oppNet);
//        System.out.println(EnemyHQ);
            if(move.distanceSquaredTo(HQ) < 4) {
                return true;
            }
        return false;
    }

    static void goHome(MapLocation m) throws GameActionException {
        Direction d = randomDirection();
        for (Direction l : directions) {
            if (rc.canDepositSoup(l)) {
//                System.out.println("I deposited soup");
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
//                            System.out.println(location);
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
//                            System.out.println(location);
                            return location;
                        }
                    }
                }
            }
        }
        return location;
    }

//    These 2 methods use old broadcasts and should not exist
//    static MapLocation getEnemyHQLocationDrone() throws GameActionException {
//        //returns the enemy HQ location as a MapLocation
//        MapLocation location = null;
//        for(int k = 1; k < rc.getRoundNum()-1; k++) {
//            if(k > 0) {
//                Transaction[] block = rc.getBlock(k);
//                if(block.length != 0) {
//                    for(int i = 0; i < block.length; i++) {
//                        int[] message = block[i].getMessage();
//                        if(message[1] == 998997 && message[2] == 3) {
//                            location = new MapLocation(message[3], message[4]);
//                            System.out.println(location);
//                            return location;
//                        }
//                    }
//                }
//            }
//        }
//        return location;
//    }
//
//    static void updateEnemyHQLocation() throws GameActionException {
//        //looks for enemy hq location in block chain and moves it to a more recent round
//        for(int k = rc.getRoundNum()-61; k < rc.getRoundNum()-59; k++) {
//            if(k > 0) {
//                Transaction[] block = rc.getBlock(k);
//                if(block.length != 0) {
//                    for(int i = 0; i < block.length; i++) {
//                        int[] message = block[i].getMessage();
//                        if(message[1] == 998997 && message[2] == 3) {
//                            postLocation(3, message[3], message[4], 2);
//                        }
//                    }
//                }
//            }
//        }
//    }

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
            oppNet.add(EnemyHQ);
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
                int type = next.getType();
                for (int i = 0; i < 3; i++) {

                    bitSet.set(index, type % 2 != 0);
                    index++;
                    type = type>>>1;
                }
                int x = next.getX();
                for (int i = 0; i < 8; i++) {

                    bitSet.set(index, x % 2 != 0);
                    index++;
                    x = x >>>1;
                }
                int y = next.getY();
                for (int i = 0; i < 8; i++) {

                    bitSet.set(index, y % 2 != 0);
                    index++;
                    y = y >>>1;
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
                bitSet.set(i, infocount % 2 != 0);
                infocount = infocount>>> 1;
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
        for (int i = 0; i < transactions.length ; i++) {
            int[] curr=transactions[i].getMessage();
            long[] longs=new long[4];
            longs[0] = (long) curr[1] << 32 | curr[0] & 0xFFFFFFFFL;
            longs[1] = (long) curr[3] << 32 | curr[2] & 0xFFFFFFFFL;
            longs[2] = (long) curr[5] << 32 | curr[4] & 0xFFFFFFFFL;
            longs[3] = (long) curr[6] & 0xFFFFFFFFL;
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
                if (ours.get(i)) count+=1L<<(i-220);
            }
            if (count>11){
                break;
            }
            for (int i = 0; i < count; i++) {
                int type=0;
                for (int j = 0; j < 3; j++) {
                    if (ours.get(i*20+j+1)) type+=1L<<j;
                }
                int x=0;
                for (int j = 3; j < 11; j++) {
                    if (ours.get(i*20+j+1)) x+=1L<<(j-3);
                }
                int y=0;
                for (int j = 11; j < 19; j++) {
                    if (ours.get(i*20+j+1)) y+=1L<<(j-11);
                }
                MapLocation next= new MapLocation(x,y);
                switch (type){
                    case 0:
                        if(x==0 && y==0 && rc.getType()==RobotType.DELIVERY_DRONE) {
                            task="crunch";
                        }
                        else if (round-initialRound <= 10 &&  x==1 && y==1 && rc.getType()==RobotType.DELIVERY_DRONE) {
                            task="scout";
                        }
                        else if (x==3 && y==3 && rc.getType()==RobotType.MINER) {
                            schoolsBuilt++;
                        }
                        else if (x==4 && y==4 && rc.getType()==RobotType.MINER) {
                            factoriesBuilt++;
                        }
                            break;

                    case 1:
                        EnemyHQ=next;
                        break;
                    case 2:
                        if(!soup.contains(next)) {
                            soup.add(next);
                        }
                        break;
                    case 3:
                        if(!offensiveEnemyBuildings.contains(next)) {
                            offensiveEnemyBuildings.add(next);
                        }
                        break;
                    case 4:
                        if(!offensiveEnemyBuildings.contains(next)) {
                            offensiveEnemyBuildings.add(next);
                        }
                        if(!refineries.contains(next)) {
                            refineries.add(next);
                        }
                        break;
                    case 5:
                        if(!oppNet.contains(next)) {
                            oppNet.add(next);
                        }
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

    static Direction[] randomDirections(){
        List<Direction> intList = Arrays.asList(directions);

        Collections.shuffle(intList);

         return intList.toArray(new Direction[0]);

    }

    static void commandDroneAttack(int cost) throws GameActionException{
        broadcastQueue.add(new Information(0, 0,0));
        tryBroadcast(cost);
    }
    static void addAndBroadcast(Information i) throws  GameActionException{
        broadcastQueue.add(i);
        tryBroadcast(1);
    }
    //__________________________________________________________________________________________________________________
}

