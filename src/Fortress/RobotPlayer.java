package Fortress;

import battlecode.common.*;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    static int factoriesBuilt;
    static  MapLocation HQ;
    static MapLocation scoutDest;
    static MapLocation mapCenter;
    static ArrayList<MapLocation> scouted = new ArrayList<>();

    static int robotsBuilt;
    static boolean moveOnce = false;

    /**MapLocation arrays containing all the relevent MapLocations **/


    static ArrayList<MapLocation> water = new ArrayList<>();
    static ArrayList<MapLocation> soup = new ArrayList<>();
    static ArrayList<MapLocation> refineries = new ArrayList<>();
    static ArrayList<MapLocation> oppNet = new ArrayList<>();
    static ArrayList<MapLocation> offensiveEnemyBuildings = new ArrayList<>();
    static MapLocation EnemyHQ;
    
    static MapLocation digLoc[];

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
        souploc = null;
        EnemyHQ = null;
        path = Direction.CENTER;
        HQ = getHQLocation();
        scoutDest = null;
        mapCenter = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);

        //landscaper task determiner
        if(rc.getType() == RobotType.LANDSCAPER){
                task = "castle";
        }
        //drone task determiner
        if(rc.getType() == RobotType.DELIVERY_DRONE){
            if(rc.getRoundNum() < 300){
                task = "scout";
            }else{
                task = "killEnemy";
            }
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
        //scanForSoup(curr);
        //souploc = getSoupLocation();
        //build design school
        //System.out.println("robots built: "+ robotsBuilt);
        if (schoolsBuilt < 1 && !scanForDesignSchool()) {
        	MapLocation loc = getHQLocation();
        	Direction away = curr.directionTo(loc).opposite();
        	if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, away)){
        	    schoolsBuilt++;
                tryBuild(RobotType.DESIGN_SCHOOL, away);
            }else if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, away.rotateLeft())) {
                schoolsBuilt++;
                tryBuild(RobotType.DESIGN_SCHOOL, away.rotateLeft());
            }else if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, away.rotateRight())){
                schoolsBuilt++;
                tryBuild(RobotType.DESIGN_SCHOOL, away.rotateRight());
            }
        }
        if (factoriesBuilt < 1 && !scanForDroneFactory()) {
            MapLocation loc = getHQLocation();
            Direction away = curr.directionTo(loc).opposite();
            if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, away)){
                factoriesBuilt++;
                tryBuild(RobotType.FULFILLMENT_CENTER, away);
            }else if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, away.rotateLeft())) {
                factoriesBuilt++;
                tryBuild(RobotType.FULFILLMENT_CENTER, away.rotateLeft());
            }else if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, away.rotateRight())){
                factoriesBuilt++;
                tryBuild(RobotType.FULFILLMENT_CENTER, away.rotateRight());
            }
        }
        openEyes(curr);
        //MINE SOUP
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
            moveTo(initialLoc);
        }
        //FIND SOUP
        else {
            if (soup.isEmpty()){
                moveTo(curr.subtract(curr.directionTo(initialLoc)));
            }
            else{
                int dist=10;
                MapLocation index=null;
                for (MapLocation l : soup) {
                    int currdist=curr.distanceSquaredTo(l);
                    if (currdist<dist) {
                        index=l;
                        dist=currdist;
                    }
                }
                souploc=index;
                moveTo(souploc);

            }
        }

    }
    static void openEyes(MapLocation loc) throws GameActionException{
        int selfX = loc.x;
        int selfY = loc.y;
        scan:
        for(int x = -5; x <6; x++){
            for (int y = -5; y < 6; y++){
                MapLocation check = new MapLocation(selfX + x, selfY + y);
                if (rc.canSenseLocation(check)){
                    if(rc.senseSoup(check) > 5){
                        soup.add(check);
                        break scan;
                    }
                }
            }
        }
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
        //if(at.distanceSquaredTo(home) < 45 && at.distanceSquaredTo(home) > 8)
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
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);
                //tryMove(Direction.EAST);
                moveToDrone(getEnemyHQLocationDrone());
                if (nearbyRobots.length > 0) {
                    Direction rando = randomDirection();
                    RobotInfo targetEnemy = nearbyRobots[0];
                    int enemyID = targetEnemy.getID();
                    //may this dont work
                    moveTo(targetEnemy.getLocation().add(rando));
                    if (rc.canPickUpUnit(targetEnemy.getID())) {
                        rc.pickUpUnit(targetEnemy.getID());
                    }
                }
            }else{
                for (Direction dir : directions) {
                    MapLocation adj = rc.adjacentLocation(dir);
                    while (rc.onTheMap(adj)) {
                        if (rc.senseFlooding(adj)) {
                            if (rc.canDropUnit(dir)) {
                                rc.dropUnit(dir);
                            }
                        }
                        tryMove(dir);
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
        RobotInfo[] r2d2 = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
        HashMap<Integer, ArrayList<MapLocation>> news = new HashMap<>();
        for(int i = 2; i < 7; i++){
            news.put(1, new ArrayList<MapLocation>());
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
                news.get(6).add(i.getLocation());
            }
            if(i.getType() == RobotType.HQ){
                EnemyHQ = i.getLocation();
            }
        }
        for(RobotInfo i : r2d2){
            if(i.getType() == RobotType.REFINERY){
                refineries.add(i.getLocation());
                news.get(4).add(i.getLocation());
            }
        }

        for(int x = -4; x < 5; x++){
            for(int y = -4; y < 5; y++){
                MapLocation n = new MapLocation(myX + x, myY + y);
                if(rc.onTheMap(n) && rc.canSenseLocation(n)){
                    if (rc.senseFlooding(n) && !senseFloodingAround(n)){
                        if(!water.contains(n)) {
                            water.add(n);
                            news.get(3).add(n);
                        }
                    }
                    if(rc.senseSoup(n) > 0 && !senseSoupAround(n)){
                        if(!soup.contains(n)){
                            soup.add(n);
                            news.get(2).add(n);
                        }
                    }
                }
            }
        }
        return news;
    }
    static boolean senseFloodingAround(MapLocation n) throws GameActionException{
        int directionsFlooded = 0;
        for(Direction d : directions){
            if(rc.onTheMap(n.add(d))) {
                if (rc.canSenseLocation(n.add(d))) {
                    if (rc.senseFlooding(n.add(d))) {
                        directionsFlooded++;
                    }
                }
            }
        }
        return (directionsFlooded > 5);
    }

    static boolean senseSoupAround(MapLocation n) throws GameActionException{
        int directionsSoup = 0;
        for(Direction d : directions){
            if(rc.onTheMap(n.add(d))){
                if (rc.canSenseLocation(n.add(d))) {
                    if (rc.senseSoup(n.add(d)) > 0) {
                        directionsSoup++;
                    }
                }
            }
        }
        return (directionsSoup > 5);
    }
    static void scout(MapLocation at) throws GameActionException {
        if(scoutDest ==  null){
            scoutDest = mapCenter;
            scouted.add(scoutDest);
        }
        if (at.distanceSquaredTo(scoutDest) < 3) {
            int q = quadrantIn(scoutDest);
            if(q == 1){
                MapLocation newDest = new MapLocation( at.x ,rc.getMapHeight() - at.y);
                if(!scouted.contains(newDest)){
                    scoutDest = new MapLocation(at.x ,rc.getMapHeight() - at.y - 8);
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
                    scoutDest = new MapLocation(rc.getMapWidth() - at.x - 8 , at.y);
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
                    scoutDest = new MapLocation(at.x ,rc.getMapHeight() - at.y + 8);
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
                    scoutDest = new MapLocation(rc.getMapWidth() - at.x + 8 , at.y);
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

        //See if general direction is valid
        if(rc.canMove(moveDirection)){
            path = moveDirection.opposite();
            tryMove(moveDirection);
        }else if(rc.canMove(moveDirection.rotateLeft()) && moveDirection.rotateLeft() != path){
            path = moveDirection.rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight()) && moveDirection.rotateRight() != path) {
            path = moveDirection.rotateRight().opposite();
            tryMove(moveDirection.rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft()) && moveDirection.rotateLeft().rotateLeft() != path) {
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight()) && moveDirection.rotateRight().rotateRight() != path) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight());
        }else if(rc.canMove(moveDirection.rotateLeft().rotateLeft().rotateLeft()) && moveDirection.rotateLeft().rotateLeft().rotateLeft() != path) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(rc.canMove(moveDirection.rotateRight().rotateRight().rotateRight()) && moveDirection.rotateRight().rotateRight().rotateRight() != path) {
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight().rotateRight());
        } else{
            tryMove(randomDirection());
        }
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
        if(hqX < mapW/2) {
            if (at.x < dest1.x - 3) {
                moveToDrone(dest1);
            } else {
                moveToDrone(dest2);
            }
        }else{
            if (at.x > dest1.x + 3) {
                moveToDrone(dest1);
            } else {
                moveToDrone(dest2);
            }
        }
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
    static boolean tryBroadcast(int cost){
        return true;
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

