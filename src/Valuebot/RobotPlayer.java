package Valuebot;

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
    static boolean initialRun;
    static boolean carryingteammate = false;
    static MapLocation enHQDest;
    static MapLocation pos1;
    static MapLocation pos2;
    static MapLocation pos3;
    static int factoriesBuilt;
    static  MapLocation HQ;
    static MapLocation scoutDest;
    static MapLocation mapCenter;
    static RobotInfo heldUnit;
    static MapLocation well1;
    static MapLocation well2;
    static MapLocation well3;
    static MapLocation well4;
    static int baseX1;
    static int baseX2;
    static int baseY1;
    static int baseY2;
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
    static ArrayList<MapLocation> vaps = new ArrayList<>();
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
        initialRun = false;
        EnemyHQ = null;
        path = Direction.CENTER;
        HQ = getHQLocation();
        if(rc.getType() == RobotType.DELIVERY_DRONE) {
            pos1 = new MapLocation(rc.getMapWidth() - HQ.x - 1, HQ.y);
            pos2 = new MapLocation(rc.getMapWidth() - HQ.x - 1, rc.getMapHeight() - HQ.y - 1);
            pos3 = new MapLocation(HQ.x, rc.getMapHeight() - HQ.y - 1);
        }
        scoutDest = null;
        mapCenter = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);


        //landscaper task determiner
        if(rc.getType() == RobotType.LANDSCAPER){
            if(rc.getRoundNum() < 150) {
                task = "castle";
            }else{
                task = "terraform";
            }
        }
        if(rc.getType() == RobotType.MINER){
            if (rc.getRoundNum()<=150){
                task="first3";
            }else{
                task = "the other guys";
            }
        }
        //drone task determiner
        if(rc.getType() == RobotType.DELIVERY_DRONE){
            if(rc.getRoundNum() < 150){
                task = "scout";
            }else if(rc.getRoundNum() < 500 && rc.getRoundNum() > 149) {
                task = "hover";
            }
            else if(rc.getRoundNum() > 499){
                task = "killEnemy";
            }
        }
        if(rc.getRoundNum() > 10){
            if(HQ.x < 7){
                baseX1 = 0;
                baseX2 = 12;
                if(HQ.y < 7) {
                    baseY1 = 0;
                    baseY2 = 12;
                    well1 = null;
                    well2 = new MapLocation(HQ.x, HQ.y + 2);
                    well3 = null;
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }else if(rc.getMapHeight() - HQ.y < 7) {
                    baseY1 = rc.getMapHeight() - 12;
                    baseX2 = rc.getMapHeight();
                    well1 = null;
                    well2 = new MapLocation(HQ.x, HQ.y + 2);
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = null;
                }else{
                    baseY1 = HQ.y - 6;
                    baseY2 = HQ.y + 6;
                    well1 = null;
                    well2 = new MapLocation(HQ.x, HQ.y + 2);
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }

            }else if(rc.getMapWidth() - HQ.x < 7){
                baseX1 = rc.getMapWidth() - 12;
                baseX2 = rc.getMapWidth();
                if(HQ.y < 7) {
                    baseY1 = 0;
                    baseY2 = 12;
                    well1 = new MapLocation(HQ.x, HQ.y - 2);
                    well2 = null;
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = null;
                }else if(rc.getMapHeight() - HQ.y < 7) {
                    baseY1 = rc.getMapHeight() - 12;
                    baseX2 = rc.getMapHeight();
                    well1 = new MapLocation(HQ.x, HQ.y - 2);
                    well2 = null;
                    well3 = null;
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }else{
                    baseY1 = HQ.y - 6;
                    baseY2 = HQ.y + 6;
                    well1 = new MapLocation(HQ.x, HQ.y - 2);
                    well2 = null;
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }
            }else if(HQ.y < 7){
                baseY1 = 0;
                baseY2 = 12;
                baseX1 = HQ.x - 6;
                baseX2 = HQ.x + 6;
                well1 = new MapLocation(HQ.x, HQ.y - 2);
                well2 = new MapLocation(HQ.x, HQ.y + 2);
                well3 = null;
                well4 = new MapLocation(HQ.x + 2, HQ.y);
            }else if(rc.getMapHeight() - HQ.y < 7){
                baseY1 = rc.getMapHeight() - 12;
                baseX2 = rc.getMapHeight();
                baseX1 = HQ.x - 6;
                baseX2 = HQ.x + 6;
                well1 = new MapLocation(HQ.x, HQ.y - 2);
                well2 = new MapLocation(HQ.x, HQ.y + 2);
                well3 = new MapLocation(HQ.x - 2, HQ.y);
                well4 = null;
            }else{
                baseX1 = HQ.x - 6;
                baseX2 = HQ.x + 6;
                baseY1 = HQ.y - 6;
                baseY2 = HQ.y + 6;
                well1 = new MapLocation(HQ.x, HQ.y - 2);
                well2 = new MapLocation(HQ.x, HQ.y + 2);
                well3 = new MapLocation(HQ.x - 2, HQ.y);
                well4 = new MapLocation(HQ.x + 2, HQ.y);
            }
        }

        initialLoc = rc.getLocation();
        initialRound=rc.getRoundNum();
        randomInitialDirection=randomDirection();

        //fill up dirHash 1:Direction.NORTH, etc
        for (int i = 0; i < directions.length; i++){
            dirHash.put(i+1, directions[i]);
        }


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
    	if(robotsBuilt < 6 && rc.getRoundNum() > 100 && rc.getTeamSoup()>204){
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
        MapLocation curr = rc.getLocation();
        if(HQ == null){
            HQ = getHQLocation();
        }
        boolean stay = openEyes(curr);
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
        	if(rc.getTeamSoup() > 150) {
                moveTo(HQ);
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
            if (curr.distanceSquaredTo(HQ) > 81) {
                moveTo(HQ);
            } else {
                for (Direction dir : directions) {
                    if (rc.canBuildRobot(RobotType.VAPORATOR, dir) && curr.add(dir).distanceSquaredTo(HQ) > 8 && curr.add(dir).distanceSquaredTo(HQ) <= 81) {
                        rc.buildRobot(RobotType.VAPORATOR, dir);
                    }
                }
            }
        }

        //NOTE: schoolsBuilt is saved per miner, meaning each miner will want to make its own design school
        //MINE SOUP

        if (souploc != null && rc.getSoupCarrying() < 96){
            mineSoup(curr);
        }
        //MOVE BACK TO HQ AND DEPOSIT SOUP
        else if (rc.getSoupCarrying() > 95){
            for (Direction dir : directions){
                if(rc.canDepositSoup(dir)){
                    rc.depositSoup(dir, rc.getSoupCarrying());
                }
            }
            if(!stay || curr.distanceSquaredTo(HQ) < 225 || rc.getRoundNum() < 125) {
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
    static boolean openEyes(MapLocation loc) throws GameActionException{

        if(souploc != null && rc.canSenseLocation(souploc)){
            if(rc.senseSoup(souploc) == 0){
                soup.remove(souploc);
                souploc = null;
            }
        }
        MapLocation[] miso = rc.senseNearbySoup();
        int totS = rc.getSoupCarrying();
        for(MapLocation m : miso) {
            if (!soup.contains(m)) {
                    soup.add(m);
                    if (soup.size()==1){
                        addAndBroadcast(new Information(2,m.x,m.y));
                    }
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
            }else if(i.getType() == RobotType.VAPORATOR && !vaps.contains(i.getLocation())){
                vaps.add(i.getLocation());
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

    static void mineSoup(MapLocation at) throws GameActionException{
        for (Direction l : directions) {
            if (rc.canMineSoup(l)) {
                rc.mineSoup(l);
            }
        }
        for(MapLocation k : rc.senseNearbySoup()){
            if(at.distanceSquaredTo(k) < at.distanceSquaredTo(souploc)){
                moveTo(k);
            }
        }
        moveTo(souploc);
    }

    static void scoutMiner(MapLocation at) throws GameActionException {
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
    }

    static void runVaporator() throws GameActionException {

    }

    //Builds Landscapers
    static void runDesignSchool() throws GameActionException {
        if(robotsBuilt < 2 && rc.getRoundNum() < 150) {
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
                }
        }else if(robotsBuilt < 8 && rc.getRoundNum() >= 150 && rc.getTeamSoup() > 210){
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
                }
        }else if(rc.getRoundNum() >= 800 && rc.getTeamSoup() > 510){
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
                }
        }
    }
    //Builds Drones
    static void runFulfillmentCenter() throws GameActionException {
        if(!closeEnemyNetGun()) {
            if (robotsBuilt < 1 && rc.getRoundNum() < 150) {
                for (Direction dir : randomDirections())
                    if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                        robotsBuilt++;
                        rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                    }
            } else if (rc.getRoundNum() < 650 && rc.getRoundNum() > 149 && robotsBuilt < 5 && rc.getTeamSoup() > 505) {
                for (Direction dir : randomDirections()) {
                    if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                        robotsBuilt++;
                        rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                    }
                }
            } else if (rc.getRoundNum() > 650 && rc.getTeamSoup() > 210) {
                for (Direction dir : randomDirections()) {
                    if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                        robotsBuilt++;
                        rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                    }
                }
            }
        }
    }

    static boolean closeEnemyNetGun(){
        for(RobotInfo r : rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam())){
            if(r.getType() == RobotType.NET_GUN){
                return true;
            }
        }
        return false;
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
        if(at.distanceSquaredTo(home) <= 2) {
        	task = "castle";
        }
        enRush(at);
        if((rc.getDirtCarrying() < 25 && !nextToWall(at)) || rc.getDirtCarrying() == 0){
            for(Direction d : directions){
                MapLocation m = at.add(d);
                if((rc.senseElevation(m) > 3 && rc.senseElevation(m) < 101 && !onTheWall(m) && !rc.isLocationOccupied(m)) || isWell(m)){
                    rc.digDirt(d);
                }
            }
            findPile(at);
        }
        for(Direction d : directions){
            MapLocation m = at.add(d);
            if(rc.senseElevation(m) < 3 && rc.senseElevation(m) > -50 && !rc.isLocationOccupied(m)){
                rc.depositDirt(d);
            }
        }
        if(!nextToWall(at)){
            moveToWall(at);
        }else{
            for(Direction d : directions){
                MapLocation n = at.add(d);
                if(onTheWall(n) && rc.senseElevation(n) < 8){
                    rc.depositDirt(d);
                }
            }
            if(at.x == baseX1 + 1){
                if(at.y == baseY2 - 1){
                    moveTo(new MapLocation(baseX2-1, baseY2-1));
                }
                moveTo(new MapLocation(baseX1+1, baseY2-1));
            }else if(at.x == baseX2 - 1){
                if(at.y == baseY1 + 1){
                    moveTo(new MapLocation(baseX1+1, baseY1+1));
                }
                moveTo(new MapLocation(baseX2-1, baseY1+1));
            }
            else if(at.y == baseY1 + 1){
                moveTo(new MapLocation(baseX1+1, baseY1+1));
            }
            else if(at.y == baseY2 - 1){
                moveTo(new MapLocation(baseX2-1, baseY2-1));
            }
        }

//        if(at.distanceSquaredTo(home) <= 8) {
//        	zergRush(at.add(dir.opposite()));
//        }
//        if(at.distanceSquaredTo(home) > 8) {
//        	if(digLoc.isEmpty() && at.distanceSquaredTo(home) > 18) {
//        		zergRush(home);
//        	}
//
//        	else if(digLoc.isEmpty()) {
//        		if(rc.isReady() && rc.canDigDirt(dir)) {
//            		rc.digDirt(dir);
//            		digLoc.add(at.add(dir));
//        		}
//        	}
//
//        	MapLocation frontRow[] = new MapLocation[9];
//        	frontRow[0] = at;
//        	frontRow[1] = frontRow[0].add(dir.rotateRight().rotateRight());
//        	frontRow[2] = frontRow[0].add(dir.rotateLeft().rotateLeft());
//        	frontRow[3] = frontRow[1].add(dir.rotateRight().rotateRight());
//        	frontRow[4] = frontRow[2].add(dir.rotateLeft().rotateLeft());
//        	frontRow[5] = frontRow[3].add(dir.rotateRight().rotateRight());
//        	frontRow[6] = frontRow[4].add(dir.rotateLeft().rotateLeft());
//
//        	if(rc.getDirtCarrying() == 25) {
//        		dirtFull = true;
//        	}
//
//        	if(rc.getDirtCarrying() == 0) {
//        		dirtFull = false;
//        	}
//
//        	if(!dirtFull) {
//        		if(at.distanceSquaredTo(digLoc.get(0)) > 2 && at.distanceSquaredTo(home) > 18) {
//        			zergRush(digLoc.get(0));
//        		}
//        		else if(rc.isReady() && rc.canDigDirt(dir)) {
//            		rc.digDirt(dir);
//        		}
//        	}
//
//
//
//        	if(dirtFull) {
//        		for(int i = 1; i < 7; i++) {
//        			if(rc.canSenseLocation(frontRow[i]) && !digLoc.contains(frontRow[i]) && rc.senseElevation(frontRow[i]) > -10) {
//        				if(rc.senseElevation(frontRow[i]) < rc.senseElevation(frontRow[0]) && rc.senseRobotAtLocation(frontRow[i]) == null) {
//        					if(at.distanceSquaredTo(frontRow[i]) > 2 && rc.isReady()) {
//        						zergRush(frontRow[i]);
//        					}
//        					else if(rc.canDepositDirt(at.directionTo(frontRow[i]))){
//        						rc.depositDirt(at.directionTo(frontRow[i]));
//        					}
//        				}
//        			}
//        		}
//        		if(rc.canSenseLocation(at.add(dir.opposite()))) {
//        			if(rc.senseElevation(at) - rc.senseElevation(at.add(dir.opposite())) < 3 && rc.isReady()) {
//        				rc.depositDirt(Direction.CENTER);
//        			}
//        			else if(rc.isReady()) {
//        				tryMove(dir.opposite());
//        				tryMove(dir.opposite().rotateLeft());
//        				tryMove(dir.opposite().rotateRight());
//        			}
//        		}
//        		else {
//        			dirtFull = false;
//        		}
//        	}
//        }
    }

    static boolean isWell(MapLocation m){
        if(m.equals(well1)){
            return true;
        }
        if(m.equals(well2)){
            return true;
        }
        if(m.equals(well3)){
            return true;
        }
        if(m.equals(well4)){
            return true;
        }
        return false;
    }

    static void enRush(MapLocation at) throws  GameActionException{
        for(RobotInfo r : rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent())){
            if(withinWalls(r.getLocation()) && (r.getType() == RobotType.NET_GUN || r.getType() == RobotType.DESIGN_SCHOOL)){
                if(rc.getDirtCarrying() == 0){
                    Direction dir = at.directionTo(HQ).opposite();
                    if(rc.canDigDirt(dir)) {
                        rc.digDirt(dir);
                    }else if(rc.canDigDirt(dir.rotateRight().rotateRight()))
                        rc.digDirt(dir.rotateRight().rotateRight());
                    else if(rc.canDigDirt(dir.rotateLeft()))
                        rc.digDirt(dir.rotateLeft());
                }else if(at.isAdjacentTo(r.getLocation())){
                    rc.depositDirt(at.directionTo(r.getLocation()));
                }else{
                    moveTo(r.getLocation());
                }
            }
        }
    }

    static void moveToWall(MapLocation at) throws  GameActionException{
        for(int i = 0; i < 7; i++){
            for(int l = 0; l < 7; l++){
                MapLocation c = new MapLocation(i, l);
                if(rc.canSenseLocation(c) && onTheWall(c) && rc.senseElevation(c) < 8){
                    moveTo(c);
                }
            }
        }
        if(at.x < HQ.x){
            moveTo(new MapLocation(baseX1, at.y));
        }else{
            moveTo(new MapLocation(baseX2, at.y));
        }
    }

    static boolean onTheWall(MapLocation m){
        if(m.x == baseX1 || m.x ==baseX2 || m.y == baseY1 || m.y == baseY2){
            return true;
        }
        return false;
    }

    static boolean nextToWall(MapLocation at){
        if(at.x == baseX1+1 || at.x ==baseX2-1 || at.y == baseY1+1 || at.y == baseY2-1){
            return true;
        }
        return false;
    }
    static void findPile(MapLocation at) throws GameActionException{

        for(int i = 0; i < 7; i++){
            for(int l = 0; l < 7; l++){
                MapLocation dirt = new MapLocation(i, l);
                if(rc.canSenseLocation(dirt) && rc.senseElevation(dirt) > 3 && withinWalls(dirt)){
                    moveTo(dirt);
                }
            }
        }
        moveTo(closestWell(at));
    }

    static boolean withinWalls(MapLocation m){
        if(m.x > baseX1 && m.x < baseX2 && m.y > baseY1 && m.y < baseY2){
            return true;
        }
        return false;
    }

    static MapLocation closestWell(MapLocation at){
        MapLocation w = well1;
        int diss = 1000000;
        if(well1 != null) {
             diss = at.distanceSquaredTo(w);
        }
        if(well2 != null && at.distanceSquaredTo(well2) < diss){
            diss = at.distanceSquaredTo(well2);
            w = well2;
        }
        if(well3 != null && at.distanceSquaredTo(well3) < diss){
            diss = at.distanceSquaredTo(well3);
            w = well3;
        }
        if(well4 != null && at.distanceSquaredTo(well4) < diss){
            diss = at.distanceSquaredTo(well4);
            w = well4;
        }
        return w;
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
//    	Direction[] direction = {
//    	        Direction.NORTH,
//    	        Direction.EAST,
//    	        Direction.SOUTH,
//    	        Direction.WEST,
//    	        Direction.NORTHEAST,
//    	        Direction.SOUTHEAST,
//    	        Direction.SOUTHWEST,
//    	        Direction.NORTHWEST
//    	    };
    	MapLocation home = HQ;
        MapLocation at = rc.getLocation();
        Direction dir = at.directionTo(home);
//        MapLocation[] build = new MapLocation[8];
        MapLocation hRight = home.add(Direction.WEST);
        MapLocation hLeft = home.add(Direction.EAST);
        MapLocation hTop= home.add(Direction.NORTH);
        MapLocation hBottom = home.add(Direction.SOUTH);
//        for(int i = 0; i < 8; i++) {
//        	build[i] = home.add(direction[i]);
//        }

        if(at.distanceSquaredTo(home) > 16){
            zergRush(home);
        }else if(rc.onTheMap(hLeft) && rc.canSenseLocation(hLeft) && !rc.isLocationOccupied(hLeft) && !at.equals(hRight) && !at.equals(hTop) && !at.equals(hBottom)){
            moveTo(hLeft);
        } else if(rc.onTheMap(hRight) && rc.canSenseLocation(hRight) && !rc.isLocationOccupied(hRight) && !at.equals(hLeft) && !at.equals(hTop) && !at.equals(hBottom)){
            moveTo(hRight);
        }else if(rc.onTheMap(hTop) && rc.canSenseLocation(hTop) && !rc.isLocationOccupied(hTop) && !at.equals(hRight) && !at.equals(hLeft) && !at.equals(hBottom)){
            moveTo(hTop);
        } else if(rc.onTheMap(hBottom) && rc.canSenseLocation(hBottom) && !rc.isLocationOccupied(hBottom) && !at.equals(hRight) && !at.equals(hTop) && !at.equals(hLeft)){
            moveTo(hBottom);
        }
//        else if (at.distanceSquaredTo(home) > 2){
//        	for(int i = 0; i < 8; i++) {
//        		if(rc.canSenseLocation(build[i]) && !rc.isLocationOccupied(build[i])) {
//        			moveTo(build[i]);
//        			break;
//        		}
//        	}
//        }
        else if(rc.canDigDirt(dir) && rc.getDirtCarrying() < 25) {
        	rc.digDirt(dir);
        }else if(enemyRush(at)){
            rc.digDirt(dir.opposite());
        } else if (at.distanceSquaredTo(home) > 1) {
            MapLocation left = at.add(dir.rotateLeft());
            MapLocation right = at.add(dir.rotateRight());

            MapLocation[] dirs = {left, right};
            MapLocation lowest = at;
            for(MapLocation m : dirs){
                if(rc.onTheMap(lowest) && rc.onTheMap(m) && rc.senseElevation(m) < rc.senseElevation(lowest) && rc.getRoundNum() > 300){
                    lowest = m;
                }
            }
            if(rc.getDirtCarrying() > 8 || rc.getRoundNum() > 1000)
                rc.depositDirt(at.directionTo(lowest));
            else if(rc.canDigDirt(dir.opposite().rotateLeft().rotateLeft()))
                    rc.digDirt(dir.opposite().rotateLeft().rotateLeft());
            else if(rc.canDigDirt(dir.opposite().rotateRight().rotateRight()))
                rc.digDirt(dir.opposite().rotateRight().rotateRight());
            else if(rc.canDigDirt(dir.opposite().rotateLeft()))
                rc.digDirt(dir.opposite().rotateLeft());

        } else if (at.distanceSquaredTo(home) == 1) {
        	MapLocation left = at.add(dir.rotateLeft().rotateLeft());
        	MapLocation right = at.add(dir.rotateRight().rotateRight());
        	MapLocation dleft = at.add(dir.rotateLeft());
        	MapLocation dright = at.add(dir.rotateRight());

        	MapLocation[] dirs = {left, right, dleft, dright};
        	MapLocation lowest = at;
        	for(MapLocation m : dirs){
        	    if(rc.onTheMap(m) && rc.senseElevation(m) < rc.senseElevation(lowest) && rc.getRoundNum() > 300){
        	        lowest = m;
                }
            }
            if(rc.getDirtCarrying() > 8 || rc.getRoundNum() > 1000)
                rc.depositDirt(at.directionTo(lowest));
            else if(rc.canDigDirt(dir.opposite()) && !HQ.isAdjacentTo(at.add(dir.opposite())))
                rc.digDirt(dir.opposite());
            else if(rc.canDigDirt(dir.opposite().rotateLeft()) && !HQ.isAdjacentTo(at.add(dir.opposite().rotateLeft())))
                rc.digDirt(dir.opposite().rotateLeft());
            else if(rc.canDigDirt(dir.opposite().rotateRight()) && !HQ.isAdjacentTo(at.add(dir.opposite().rotateRight())))
                rc.digDirt(dir.opposite().rotateRight());
        }
    }

    static boolean isHQFull(MapLocation at) throws GameActionException{
        for(Direction d : directions) {
            MapLocation nextTo = HQ.add(d);
            if (rc.onTheMap(nextTo) && rc.canSenseLocation(nextTo) && !rc.isLocationOccupied(nextTo)) {
                return false;
            }
        }
        return true;
    }
    static boolean enemyRush(MapLocation at) throws GameActionException{
        for(RobotInfo r : rc.senseNearbyRobots(9, rc.getTeam().opponent())){
            if(r.getLocation().isAdjacentTo(at) && rc.canDepositDirt(at.directionTo(r.getLocation()))){
                rc.depositDirt(at.directionTo(r.getLocation()));
                return true;
            }
        }
        return false;
    }
    //__________________________________________________________________________________________________________________
    //DELIVERY DRONE CODE BELOW
    static void runDeliveryDrone() throws GameActionException {
        if(rc.getRoundNum() > 1000 && task.equals("hover")){
            task = "defend";
        }
        if(rc.getRoundNum() > 1500 && !task.equals("defend")){
            task = "crunch";
        }

        if(task.equals("scout")){
            if(rc.getRoundNum()%10 == 0 || broadcastQueue.size()>14){
                tryBroadcast(1);
            }
            MapLocation loc = rc.getLocation();
            updateBroadcast(scan(loc));

            if(rc.isCurrentlyHoldingUnit()){
                for(Direction g : directions){
                    MapLocation check= loc.add(g);
                    if(rc.onTheMap(check) && rc.senseFlooding(check) && rc.canDropUnit(g)){
                        rc.dropUnit(g);
                    }
                }
            }

            scout(loc);

        }
        if(task.equals("defend")){
            MapLocation at = rc.getLocation();
            scan(at);
            if(rc.isCurrentlyHoldingUnit()){
                dropHeldUnit(at);
            }
            if(at.distanceSquaredTo(HQ) > 8){
                moveToDrone(HQ);
            }

            RobotInfo[] C3PO = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
            for(RobotInfo z : C3PO){
                if(rc.canPickUpUnit(z.getID()) && z.getLocation().isAdjacentTo(HQ)){
                    rc.pickUpUnit(z.getID());
                }
            }
        }
        if(task.equals(("crunch"))){
            MapLocation loc = rc.getLocation();
            scan(loc);

            if(rc.isCurrentlyHoldingUnit() && carryingteammate == true){
                for(Direction g : directions){
                    MapLocation check= loc.add(g);
                    if(rc.onTheMap(check) && check.distanceSquaredTo(EnemyHQ) < 9 && !rc.senseFlooding(check) && rc.canDropUnit(g)){
                        rc.dropUnit(g);
                    }
                }
                moveToCrunch(EnemyHQ);
            }
            if(rc.isCurrentlyHoldingUnit() && carryingteammate == false){
                    for(Direction g : directions){
                        MapLocation check= loc.add(g);
                        if(rc.onTheMap(check) && rc.senseFlooding(check)&& rc.canDropUnit(g)){
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
            if(rc.getRoundNum() > 10) {
                receiveBroadcast(rc.getRoundNum() - 1);
            }
            MapLocation at = rc.getLocation();
            scan(at);
            if(rc.getRoundNum() > 250){
                if(rc.isCurrentlyHoldingUnit() && heldUnit.getType() == RobotType.MINER){
                    if(at.distanceSquaredTo(HQ) > 64 || souploc == null){
                        souploc = getClosestSoup(at);
                    }
                    if(at.isWithinDistanceSquared(souploc, 8)){
                        Direction tos = at.directionTo(souploc);
                        if(rc.canDropUnit(tos) && rc.canSenseLocation(at.add(tos)) && at.add(tos).isAdjacentTo(souploc) && !rc.senseFlooding(at.add(tos)) && (rc.senseElevation(at.add(tos)) - rc.senseElevation(souploc) > 3 || rc.senseElevation(at.add(tos)) - rc.senseElevation(souploc) < 3)) {
                            rc.dropUnit(tos);
                        }else if(rc.canDropUnit(tos.rotateRight()) && rc.canSenseLocation(at.add(tos.rotateRight())) && at.add(tos).isAdjacentTo(souploc) && !rc.senseFlooding(at.add(tos.rotateLeft())) && (rc.senseElevation(at.add(tos.rotateRight())) - rc.senseElevation(souploc) > 3 || rc.senseElevation(at.add(tos.rotateRight())) - rc.senseElevation(souploc) < 3)){
                            rc.dropUnit(tos.rotateRight());
                        }else if (rc.canDropUnit(tos.rotateLeft()) && rc.canSenseLocation(at.add(tos.rotateLeft())) && at.add(tos).isAdjacentTo(souploc) && !rc.senseFlooding(at.add(tos.rotateRight())) && (rc.senseElevation(at.add(tos.rotateLeft())) - rc.senseElevation(souploc) > 3 || rc.senseElevation(at.add(tos.rotateLeft())) - rc.senseElevation(souploc) < 3)){
                            rc.dropUnit(tos.rotateLeft());
                        }
                    }
                    moveToDrone(souploc);
                }else {
                    if (at.distanceSquaredTo(HQ) > 64){
                        moveToDroneHover(HQ);
                    }else {
                        for (MapLocation m : soup) {
                            if (m.distanceSquaredTo(HQ) > 121) {
                                souploc = m;
                                pickUpMiner(at);
                            }
                        }
                    }
                }
            }
            if(rc.isCurrentlyHoldingUnit()) {
                dropHeldUnit(at);
                scout(at);
            }else {
                findUnit(at);
                moveToDroneHover(HQ);
            }
        }
        if (task.equals("killEnemy")){
            if(rc.getRoundNum() > 10) {
                receiveBroadcast(rc.getRoundNum() - 1);
            }
            MapLocation at = rc.getLocation();
            scan(at);
            Team enemy = rc.getTeam().opponent();


            if(at.distanceSquaredTo(HQ) < 16){
                checkHQ = false;
            }
            if(checkHQ){
                moveToDrone(HQ);
            }
            if(at.distanceSquaredTo(HQ) > 16  && rc.getRoundNum() > 1000 && !rc.isCurrentlyHoldingUnit()){
                RobotInfo[] R2D2 = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
                for(RobotInfo x : R2D2){
                    if(rc.canPickUpUnit(x.getID()) && x.getType().name() == "LANDSCAPER"){
                        rc.pickUpUnit(x.getID());
                        carryingteammate = true;
                        checkHQ = false;
                        break;
                    }
                }
            }
            if (!rc.isCurrentlyHoldingUnit() || carryingteammate) {
                findUnit(at);
                if(EnemyHQ != null){
                    moveToDrone(EnemyHQ);
                }else{
                    findEnemyHQ(at);
                }
            }
            else if (!carryingteammate){
                dropHeldUnit(at);
                scout(at);
            }
        }
    }

    static void findUnit(MapLocation at) throws  GameActionException{
        Team enemy = rc.getTeam().opponent();
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), enemy);
        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
        RobotInfo[] nearbyCows = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), Team.NEUTRAL);
        ArrayList<RobotInfo> nearbyRobots = new ArrayList<>();
        if (!isHQFull(at) && rc.getRoundNum() > 500) {
            ArrayList<RobotInfo> nearbyLandscapers = new ArrayList<>();
            for(RobotInfo r : nearbyFriendlies){
                if(r.getType() == RobotType.LANDSCAPER && !r.getLocation().isAdjacentTo(HQ)){
                    if(rc.canPickUpUnit(r.getID())){
                        heldUnit = r;
                        rc.pickUpUnit(r.getID());
                    }
                    nearbyLandscapers.add(r);
                }
            }
            if(nearbyLandscapers.size() > 0){
                moveToDrone(closestEnemyRobot(at, nearbyLandscapers));
            }
        }

        for (RobotInfo r : nearbyEnemies) {
            if ((r.getType() == RobotType.MINER || r.getType() == RobotType.LANDSCAPER) && (quadrantIn(r.getLocation()) == quadrantIn(HQ) || task == "killEnemy")) {
                nearbyRobots.add(r);
            }
        }
        for (RobotInfo r : nearbyFriendlies) {
            if (rc.getRoundNum() > 300 && r.getType() == RobotType.MINER && r.getLocation().isAdjacentTo(HQ)) {
                nearbyRobots.add(r);
            }
        }
        for (RobotInfo r : nearbyCows) {
            if (quadrantIn(r.getLocation()) == quadrantIn(HQ) || r.getLocation().isWithinDistanceSquared(HQ, 16)) {
                nearbyRobots.add(r);
            }
        }
        if (nearbyRobots.size() > 0) {
            for (RobotInfo targetEnemy : nearbyRobots) {
                int enemyID = targetEnemy.getID();
                if (rc.canPickUpUnit(enemyID)) {
                    heldUnit = targetEnemy;
                    rc.pickUpUnit(enemyID);
                }
            }
            moveToDrone(closestEnemyRobot(at, nearbyRobots));
        }


    }

    static void dropHeldUnit(MapLocation at) throws GameActionException{
        if(heldUnit.getType() == RobotType.MINER && heldUnit.getTeam() == rc.getTeam()) {
            for (Direction g : directions) {
                if (rc.canDropUnit(g) && at.add(g).distanceSquaredTo(HQ) > 8) {
                    rc.dropUnit(g);
                }
            }
            moveToDrone(at.add(at.directionTo(HQ).opposite()));
        }else if(heldUnit.getType() == RobotType.LANDSCAPER && heldUnit.getTeam() == rc.getTeam()){
            if(isHQFull(at)){
                for (Direction g : directions) {
                    if (rc.canDropUnit(g)) {
                        rc.dropUnit(g);
                    }
                }
            }
            for (Direction g : directions) {
                if (rc.canDropUnit(g) && at.add(g).isAdjacentTo(HQ)) {
                    rc.dropUnit(g);
                }
            }
            moveToDrone(HQ);
        }else
        if (water != null) {
            for (Direction g : directions) {
                if (rc.onTheMap(at.add(g)) && rc.senseFlooding(at.add(g)) && rc.canDropUnit(g)) {
                    rc.dropUnit(g);
                }
            }
            moveToDrone(water);
        }
    }

    static void pickUpMiner(MapLocation at) throws GameActionException{
        RobotInfo[] friend = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
        ArrayList<RobotInfo> minors = new ArrayList<>();

        for(RobotInfo i : friend){
            if(i.getType() == RobotType.MINER) {
                minors.add(i);
                if (rc.canPickUpUnit(i.getID())) {
                    heldUnit = i;
                    rc.pickUpUnit(i.getID());
                }
            }
        }
        if(!minors.isEmpty()) {
            moveToDrone(closestEnemyRobot(at, minors));
        }
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
            if(i.getType() == RobotType.NET_GUN && !oppNet.contains(i.getLocation())){
                oppNet.add(i.getLocation());
                news.get(5).add(i.getLocation());
            }
            if((i.getType() == RobotType.DESIGN_SCHOOL || i.getType() == RobotType.NET_GUN || i.getType() == RobotType.FULFILLMENT_CENTER || i.getType() == RobotType.REFINERY) && quadrantIn(i.getLocation()) == quadrantIn(HQ) && !offensiveEnemyBuildings.contains(i.getLocation())){
                offensiveEnemyBuildings.add(i.getLocation());
                news.get(3).add(i.getLocation());
            }
            if(i.getType() == RobotType.HQ && EnemyHQ == null){
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
            if (!soup.contains(m)) {
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

    static void scout(MapLocation at) throws GameActionException {
        if(scoutDest == null){
            scoutDest = pos1;
            scouted.add(pos1);
        }
        if(at.isWithinDistanceSquared(pos1, 25)){
            scoutDest = pos3;
            scouted.add(pos3);
        }
        if(at.isWithinDistanceSquared(pos3, 25) && scouted.contains(pos1)){
            scoutDest = mapCenter;
            initialRun = true;
            scouted.add(scoutDest);
        }
        if(initialRun) {
            int xAdd = rc.getMapWidth() / 6;
            int yAdd = rc.getMapHeight() / 6;
            if (at.distanceSquaredTo(scoutDest) < 16) {
                int q = quadrantIn(scoutDest);
                if (q == 1) {
                    MapLocation newDest = new MapLocation(at.x, rc.getMapHeight() - at.y);
                    if (!scouted.contains(newDest)) {
                        scoutDest = new MapLocation(at.x, rc.getMapHeight() - at.y - yAdd);
                        if (rc.onTheMap(scoutDest)) {
                            scouted.add(scoutDest);
                        } else {
                            scoutDest = mapCenter;
                            scouted = new ArrayList<>();
                        }
                    }
                } else if (q == 2) {
                    MapLocation newDest = new MapLocation(rc.getMapWidth() - at.x, at.y);
                    if (!scouted.contains(newDest)) {
                        scoutDest = new MapLocation(rc.getMapWidth() - at.x + xAdd, at.y);
                        if (rc.onTheMap(scoutDest)) {
                            scouted.add(scoutDest);
                        } else {
                            scoutDest = mapCenter;
                            scouted = new ArrayList<>();
                        }
                    }
                } else if (q == 3) {
                    MapLocation newDest = new MapLocation(at.x, rc.getMapHeight() - at.y);
                    if (!scouted.contains(newDest)) {
                        scoutDest = new MapLocation(at.x, rc.getMapHeight() - at.y + yAdd);
                        if (rc.onTheMap(scoutDest)) {
                            scouted.add(scoutDest);
                        } else {
                            scoutDest = mapCenter;
                            scouted = new ArrayList<>();
                        }
                    }
                } else if (q == 4) {
                    MapLocation newDest = new MapLocation(rc.getMapWidth() - at.x, at.y);
                    if (!scouted.contains(newDest)) {
                        scoutDest = new MapLocation(rc.getMapWidth() - at.x - xAdd, at.y);
                        if (rc.onTheMap(scoutDest)) {
                            scouted.add(scoutDest);
                        } else {
                            scoutDest = mapCenter;
                            scouted = new ArrayList<>();
                        }
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
    }
    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
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
//        if(loc.distanceSquaredTo(HQ) < 64) {
//            nonoDirections = new Direction[]{path};
//        }else {
//            nonoDirections = new Direction[]{Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST, path};
//        }
//        ArrayList<Direction> ew = new ArrayList<>();
//        for( Direction d : nonoDirections){
//            ew.add(d);
//        }
//        prevLocations.add(loc);
        //See if general direction is valid
        if(rc.canMove(moveDirection) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection))&& !prevLocations.contains(loc.add(moveDirection))){
            path = moveDirection.opposite();
            tryMoveD(moveDirection);
        }else if(rc.canMove(moveDirection.rotateLeft()) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection.rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft()))){
            path = moveDirection.rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight()) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection.rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight()))) {
            path = moveDirection.rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft()) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection.rotateLeft().rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft().rotateLeft()))){
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight()) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection.rotateRight().rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight().rotateRight()))) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft().rotateLeft()) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft()))&& !prevLocations.contains(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft()))) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMoveD(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight().rotateRight()) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection.rotateRight().rotateRight().rotateRight()))&& !prevLocations.contains(loc.add(moveDirection.rotateRight().rotateRight().rotateRight()))){
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMoveD(moveDirection.rotateRight().rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.opposite()) && !avoidEnHQ(loc.add(moveDirection)) && !netGunInRange(loc.add(moveDirection.opposite()))&& !prevLocations.contains(loc.add(moveDirection.opposite()))) {
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
//        prevLocations.add(loc);
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
    static boolean avoidEnHQ(MapLocation move){
        if(EnemyHQ != null){
            return false;
        }
        if(move.distanceSquaredTo(pos1) < 16){
            return true;
        }
        if(move.distanceSquaredTo(pos2) < 16){
            return true;
        }
        if(move.distanceSquaredTo(pos3) < 16){
            return true;
        }
        return false;
    }
    static boolean hQInRange(MapLocation move){
            if(move.distanceSquaredTo(HQ) < 4) {
                return true;
            }
        return false;
    }

    static void goHome(MapLocation m) throws GameActionException {
        Direction d = randomDirection();
        for (Direction l : directions) {
            if (rc.canDepositSoup(l)) {
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

    static void findEnemyHQ(MapLocation at) throws GameActionException{
        MapLocation home = getHQLocation();
        int hqX = home.x;
        int hqY = home.y;
        int mapW = rc.getMapWidth();
        int mapH = rc.getMapHeight();
        //straight across
        MapLocation dest1 = new MapLocation(mapW - hqX - 1, hqY);
        //straight down and straight across
        MapLocation dest2 = new MapLocation(mapW-hqX - 1, mapH-hqY - 1);

        if(at.isWithinDistanceSquared(dest2, 16)){
            EnemyHQ = new MapLocation( hqX, mapH - hqY - 1);
            oppNet.add(EnemyHQ);
        }
        if(enHQDest == null){
            enHQDest = dest1;
        }
        if(at.distanceSquaredTo(dest1) < 25){
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
            while(infocount<14 && !broadcastQueue.isEmpty()) {
                Information next = broadcastQueue.poll();
                index++;
                int type = next.getType();
                for (int i = 0; i < 3; i++) {

                    bitSet.set(index, type % 2 != 0);
                    index++;
                    type = type>>>1;
                }
                int x = next.getX();
                for (int i = 0; i < 6; i++) {

                    bitSet.set(index, x % 2 != 0);
                    index++;
                    x = x >>>1;
                }
                int y = next.getY();
                for (int i = 0; i < 6; i++) {

                    bitSet.set(index, y % 2 != 0);
                    index++;
                    y = y >>>1;
                }
                infocount++;
            }
            bitSet.set(0 * 16, true);
            //bitSet.set(1*16,true);
            bitSet.set(2 * 16, true);
            bitSet.set(3 * 16, true);
            bitSet.set(4 * 16, true);
            //bitSet.set(5*16,true);
            bitSet.set(6 * 16, true);
            //bitSet.set(7*16,true);
            //bitSet.set(8*16,true);
            //bitSet.set(9 * 16, true);

            for (int i = 0; i < 4; i++) {
                bitSet.set((16*(i+10)), infocount % 2 != 0);
                infocount = infocount>>> 1;
            }
            long[] longs=bitSet.toLongArray();
            message[0]= ((int)longs[0]);
            message[1]= ((int)(longs[0]>>>32));
            message[2]= ((int)longs[1]);
            message[3]= ((int)(longs[1]>>>32));
            if (longs.length>=3) {
                message[4]= ((int)longs[2]);
                message[5]= ((int)(longs[2]>>>32));
            }
            else {
                message[4]= 0;
                message[5]= 0;
            }

            if (longs.length==4) {
                message[6] = ((int) longs[3]);
            }
            else{
                message[6]=0;
            }

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
            if (    bitSet.get(0 * 16) &&
                    !bitSet.get(1*16) &&
                    bitSet.get(2 * 16) &&
                    bitSet.get(3 * 16) &&
                    bitSet.get(4 * 16) &&
                    !bitSet.get(5*16) &&
                    bitSet.get(6 * 16) &&
                    !bitSet.get(7*16) &&
                    !bitSet.get(8*16) &&
                    !bitSet.get(9 * 16)


            ) {
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
            for (int i = 0; i < 4; i++) {
                if (ours.get(16*(i+10))) count+=1L<<(i);
            }
            if (count>14){
                break;
            }
            for (int i = 0; i < count; i++) {
                int type=0;
                for (int j = 0; j < 3; j++) {
                    if (ours.get(i*16+j+1)) type+=1L<<j;
                }
                int x=0;
                for (int j = 3; j < 9; j++) {
                    if (ours.get(i*16+j+1)) x+=1L<<(j-3);
                }
                int y=0;
                for (int j = 9; j < 15; j++) {
                    if (ours.get(i*16+j+1)) y+=1L<<(j-9);
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
    static void reBroadcast() throws GameActionException{

    }
    //__________________________________________________________________________________________________________________
}

