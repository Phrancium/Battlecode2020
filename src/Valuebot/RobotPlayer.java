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
    static Direction[] directionsc = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER,
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    static boolean dirtFull = false;
    static HashMap<Integer, Direction> dirHash = new HashMap<Integer, Direction>();
    static MapLocation initialLoc;
    static MapLocation souploc;
    static Direction path;
    static String task="";
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
    static MapLocation fir;
    static MapLocation sec;
    static int baseX1;
    static int baseX2;
    static int baseY1;
    static int baseY2;
    static int baseLevel;
    static Direction moveDir;
    static int closest;
    static MapLocation toGo;
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
    static ArrayList<MapLocation> walls = new ArrayList<>();

    static final int STATUSFREQ=50;
    static final int SUPERSECRET=274321;
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
        closest = 1000000;
        toGo = null;
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
        if(rc.getRoundNum() > 50) {
            checkStatus(rc.getRoundNum());
        }
        if(rc.getRoundNum() > 20) {
            int dq = quadrantIn(HQ);
            if (dq == 1) {
                fir = new MapLocation(5, rc.getMapHeight() - 6);
                sec = new MapLocation(rc.getMapWidth() - 6, 5);
            } else if (dq == 2) {
                fir = new MapLocation(rc.getMapWidth() - 6, rc.getMapHeight() - 6);
                sec = new MapLocation(5, 5);
            } else if (dq == 3) {
                sec = new MapLocation(5, rc.getMapHeight() - 6);
                fir = new MapLocation(rc.getMapWidth() - 6, 5);

            } else if (dq == 4) {
                sec = new MapLocation(rc.getMapWidth() - 6, rc.getMapHeight() - 6);
                fir = new MapLocation(5, 5);
            }
        }

        //landscaper task determiner
        if(rc.getType() == RobotType.LANDSCAPER){
            if(rc.getRoundNum() < 125) {
                task = "castle";
            }else{
                task = "terraform";
            }
        }
        if(rc.getType() == RobotType.MINER){
            if(rc.getRoundNum() < 3){
                task = "first";
            }else if (rc.getRoundNum() <= 30){
                task="first3";
            }else{
                task = "the other guys";
            }
        }
        //drone task determiner // we got new one based on broadcast
        if(rc.getType() == RobotType.DELIVERY_DRONE){
            if(rc.getRoundNum() < 125){
                task = "scout";
            }else if(rc.getRoundNum() < 900 && rc.getRoundNum() > 125) {
                task = "hover";
            }
            else if(rc.getRoundNum() > 899){
                task = "killEnemy";
            }
        }
        baseLevel = 3;
        if(rc.getRoundNum() > 20){
            if(HQ.x < 6){
                baseX1 = -1;
                baseX2 = 10;
                if(HQ.y < 6) {
                    baseY1 = -1;
                    baseY2 = 10;
                    well1 = null;
                    well2 = new MapLocation(HQ.x, HQ.y + 2);
                    well3 = null;
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }else if(rc.getMapHeight() - HQ.y < 6) {
                    baseY1 = rc.getMapHeight() - 11;
                    baseY2 = rc.getMapHeight();
                    well1 = null;
                    well2 = new MapLocation(HQ.x, HQ.y + 2);
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = null;
                }else{
                    baseY1 = HQ.y - 5;
                    baseY2 = HQ.y + 5;
                    well1 = null;
                    well2 = new MapLocation(HQ.x, HQ.y + 2);
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }

            }else if(rc.getMapWidth() - HQ.x < 6){
                baseX1 = rc.getMapWidth() - 11;
                baseX2 = rc.getMapWidth();
                if(HQ.y < 6) {
                    baseY1 = -1;
                    baseY2 = 10;
                    well1 = new MapLocation(HQ.x, HQ.y - 2);
                    well2 = null;
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = null;
                }else if(rc.getMapHeight() - HQ.y < 6) {
                    baseY1 = rc.getMapHeight() - 11;
                    baseY2 = rc.getMapHeight();
                    well1 = new MapLocation(HQ.x, HQ.y - 2);
                    well2 = null;
                    well3 = null;
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }else{
                    baseY1 = HQ.y - 5;
                    baseY2 = HQ.y + 5;
                    well1 = new MapLocation(HQ.x, HQ.y - 2);
                    well2 = null;
                    well3 = new MapLocation(HQ.x - 2, HQ.y);
                    well4 = new MapLocation(HQ.x + 2, HQ.y);
                }
            }else if(HQ.y < 6){
                baseY1 = -1;
                baseY2 = 10;
                baseX1 = HQ.x - 5;
                baseX2 = HQ.x + 5;
                well1 = new MapLocation(HQ.x, HQ.y - 2);
                well2 = new MapLocation(HQ.x, HQ.y + 2);
                well3 = null;
                well4 = new MapLocation(HQ.x + 2, HQ.y);
            }else if(rc.getMapHeight() - HQ.y < 6){
                baseY1 = rc.getMapHeight() - 11;
                baseY2 = rc.getMapHeight();
                baseX1 = HQ.x - 5;
                baseX2 = HQ.x + 5;
                well1 = new MapLocation(HQ.x, HQ.y - 2);
                well2 = new MapLocation(HQ.x, HQ.y + 2);
                well3 = new MapLocation(HQ.x - 2, HQ.y);
                well4 = null;
            }else{
                baseX1 = HQ.x - 5;
                baseX2 = HQ.x + 5;
                baseY1 = HQ.y - 5;
                baseY2 = HQ.y + 5;
                well1 = new MapLocation(HQ.x, HQ.y - 2);
                well2 = new MapLocation(HQ.x, HQ.y + 2);
                well3 = new MapLocation(HQ.x - 2, HQ.y);
                well4 = new MapLocation(HQ.x + 2, HQ.y);
            }
        }
//        for(int i = baseX1; i <= baseX2; i++){
//            walls.add(new MapLocation(i, baseY1));
//            walls.add(new MapLocation(i, baseY2));
//        }
//        for(int i = baseY1; i <= baseY2; i++){
//            walls.add(new MapLocation(baseX1, i));
//            walls.add(new MapLocation(baseX2, i));
//        }

        initialLoc = rc.getLocation();
        initialRound=rc.getRoundNum();
        randomInitialDirection=randomDirection();

        //fill up dirHash 1:Direction.NORTH, etc
//        for (int i = 0; i < directions.length; i++){
//            dirHash.put(i+1, directions[i]);
//        }

//        if(rc.getRoundNum() > 20) {
//            receiveBroadcast(rc.getRoundNum() - 1);
//        }


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
    static Direction[] hqdirections;
    static void runHQ() throws GameActionException {
    	MapLocation base = rc.getLocation();
    	HQ=base;

        if (rc.getRoundNum() == 1){
            MapLocation[] s=rc.senseNearbySoup(8);
            if (s.length!=0){
                Direction d=base.directionTo(s[0]);
                hqdirections= new Direction[]{
                        d, d.rotateLeft(), d.rotateRight(), d.rotateLeft().rotateLeft(), d.rotateRight().rotateRight(), d.rotateLeft().rotateLeft().rotateLeft(), d.rotateRight().rotateRight().rotateRight(), d.opposite()
                };
            }
            else {
                s=rc.senseNearbySoup(18);
                if (s.length!=0){
                    Direction d=base.directionTo(s[0]);
                    hqdirections= new Direction[]{
                            d, d.rotateLeft(), d.rotateRight(), d.rotateLeft().rotateLeft(), d.rotateRight().rotateRight(), d.rotateLeft().rotateLeft().rotateLeft(), d.rotateRight().rotateRight().rotateRight(), d.opposite()
                    };
                }
                else {
                    s=rc.senseNearbySoup(32);
                    if (s.length!=0){
                        Direction d=base.directionTo(s[0]);
                        hqdirections= new Direction[]{
                                d, d.rotateLeft(), d.rotateRight(), d.rotateLeft().rotateLeft(), d.rotateRight().rotateRight(), d.rotateLeft().rotateLeft().rotateLeft(), d.rotateRight().rotateRight().rotateRight(), d.opposite()
                        };
                    }
                    else {
                        s=rc.senseNearbySoup();
                        if (s.length!=0){
                            Direction d=base.directionTo(s[0]);
                            hqdirections= new Direction[]{
                                    d, d.rotateLeft(), d.rotateRight(), d.rotateLeft().rotateLeft(), d.rotateRight().rotateRight(), d.rotateLeft().rotateLeft().rotateLeft(), d.rotateRight().rotateRight().rotateRight(), d.opposite()
                            };
                        }
                        else {
                            hqdirections=randomDirections();
                        }
                    }
                }
            }
        }

        if (rc.getRoundNum() < 20) {
            for (Direction dir : hqdirections) {
                if(tryBuild(RobotType.MINER, dir)) {
                    robotsBuilt++;
                }
            }
        }

        if(rc.getRoundNum() == 11) {
    		postLocation(1, base.x, base.y, 1);
    	}
    	RobotInfo[] r = rc.senseNearbyRobots();
    	for(RobotInfo s : r){
    	    if(s.getTeam() != rc.getTeam() && rc.canShootUnit(s.getID())){
    	        rc.shootUnit(s.getID());
            }
        }

    	if(robotsBuilt < 6 && rc.getRoundNum() > 100 && rc.getTeamSoup()>204){
            for (Direction dir : hqdirections) {
                if(tryBuild(RobotType.MINER, dir)) {
                    robotsBuilt++;
                }
            }
        }
    	if (rc.getRoundNum() % STATUSFREQ==0){
            tryBroadcast(1);
        }
    	//
    	//updateEnemyHQLocation();
        //}

    }

    //__________________________________________________________________________________________________________________
    //MINER CODE BELOW
    static void runMiner() throws GameActionException {
        if(rc.getRoundNum() > 20) {
            receiveBroadcast(rc.getRoundNum() - 1);
        }
        MapLocation curr = rc.getLocation();
        if(HQ == null){
            HQ = getHQLocation();
        }

        boolean stay = openEyes(curr);
        if ((rc.getTeamSoup() >= 150 && schoolsBuilt < 1 && (task.equals("first3") || task.equals("first"))) || rc.getRoundNum() > 1000) {
        	for(Direction d : directions) {
                if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, d) && curr.add(d).distanceSquaredTo(HQ) > 8 && curr.add(d).distanceSquaredTo(HQ) < 16) {
                    schoolsBuilt++;
                    addAndBroadcast(new Information(0,3,3));
                    tryBuild(RobotType.DESIGN_SCHOOL, d);
                }
            }
        }

        if (rc.getTeamSoup() >= 150 && factoriesBuilt < 1 && (task.equals("first3") || task.equals("first"))) {
            for(Direction d : directions) {
                if (rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, d) && curr.add(d).distanceSquaredTo(HQ) > 8 && curr.add(d).distanceSquaredTo(HQ) < 25) {
                    factoriesBuilt++;
                    addAndBroadcast(new Information(0,4,4));
                    tryBuild(RobotType.FULFILLMENT_CENTER, d);
                }
            }
        }
        if(rc.getRoundNum() > 125 && rc.getTeamSoup() > 499 && task.equals("first")) {
                for (Direction dir : directions) {
                    if (rc.canBuildRobot(RobotType.VAPORATOR, dir) && curr.add(dir).distanceSquaredTo(HQ) > 8 && curr.add(dir).distanceSquaredTo(HQ) < 25) {
                        rc.buildRobot(RobotType.VAPORATOR, dir);
                    }
                }
        }
        if (rc.getSoupCarrying() > 95 || (rc.getSoupCarrying() > 25 && rc.senseNearbySoup().length == 0)){
            for (Direction dir : directions){
                if(rc.canDepositSoup(dir)){
                    rc.depositSoup(dir, rc.getSoupCarrying());
                }
            }
            if(!stay || curr.distanceSquaredTo(HQ) < 225 || rc.getRoundNum() < 300 && rc.isReady()) {
                moveTo(getClosestRefine(curr));
            }
        }
        if(rc.getTeamSoup() > 200 && curr.isAdjacentTo(HQ)){
            moveTo(curr.add(curr.directionTo(HQ).opposite()));
        }if (!soup.isEmpty() && rc.getSoupCarrying() < 96 && rc.isReady()){
            mineSoup(curr);
        } else if(rc.isReady()){
            if (soup.isEmpty()){
                scoutMiner(curr);
            }
        }

    }
    static boolean openEyes(MapLocation loc) throws GameActionException{

        if(souploc != null && rc.canSenseLocation(souploc)){
            if(rc.senseSoup(souploc) == 0 || cantReach(loc, souploc)){
                soup.remove(souploc);
                if(!soup.isEmpty()){
                    souploc = getClosestSoup(loc);
                }else {
                    souploc = null;
                }
            }
        }
        MapLocation[] miso = rc.senseNearbySoup();;
        int totS = rc.getSoupCarrying();
        int limit = 0;
        for(MapLocation m : miso) {
            if (!soup.contains(m)) {
                    limit++;
                    soup.add(m);
                    if (soup.size()==1){
                        addAndBroadcast(new Information(2,m.x,m.y));
                    }
                    break;
            }
            if(rc.canSenseLocation(m)) {
                totS += rc.senseSoup(m);
            }
        }
        RobotInfo[] r2d2 = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
        for(RobotInfo i : r2d2){
            if(i.getType() == RobotType.REFINERY && !refineries.contains(i.getLocation())){
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
        if ((rc.getTeamSoup() > 200 && refineries.isEmpty() && totS > 1 && rc.getRoundNum() > 300)) {
            for (Direction d : directions) {
                if (rc.canBuildRobot(RobotType.REFINERY, d) && loc.add(d).distanceSquaredTo(HQ) > 8) {
                    refineries.add(loc.add(d));
                    addAndBroadcast(new Information(4,loc.add(d).x,loc.add(d).y));
                    rc.buildRobot(RobotType.REFINERY, d);
                }
            }
            return true;
        }else if((rc.getTeamSoup() > 200 && totS > 200 && (refineries.isEmpty() || loc.distanceSquaredTo(getClosestRefine(loc)) >= 144))){
            for (Direction d : directions) {
                if (rc.canBuildRobot(RobotType.REFINERY, d) && loc.add(d).distanceSquaredTo(HQ) > 8) {
                    refineries.add(loc.add(d));
                    addAndBroadcast(new Information(4,loc.add(d).x,loc.add(d).y));
                    rc.buildRobot(RobotType.REFINERY, d);
                }
            }
            return true;
        }
        return false;
    }

    static boolean cantReach(MapLocation at, MapLocation m) throws GameActionException{
        if(rc.senseFlooding(m) || rc.senseElevation(m) - rc.senseElevation(at) > 3 || rc.senseElevation(m) - rc.senseElevation(at) < 3){
            if(!canMoveTo(at.add(at.directionTo(m)), at.directionTo(m))){
                return true;
            }
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
            if(rc.getRoundNum() < 300){
                if(m.distanceSquaredTo(HQ) < diss){
                    return HQ;
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
        if(rc.canMineSoup(Direction.CENTER)){
            rc.mineSoup(Direction.CENTER);
        }else {
            for (Direction l : directionsc) {
                if (rc.canMineSoup(l)) {
                    rc.mineSoup(l);
                }
            }
        }
        if(souploc == null) {
            souploc = getClosestSoup(at);
        }
        MapLocation ns = souploc;
        int diss = at.distanceSquaredTo(ns);
        for(MapLocation k : rc.senseNearbySoup()){
            if(at.distanceSquaredTo(k) < diss){
                ns = k;
                diss = at.distanceSquaredTo(k);
            }
        }
        if(rc.isReady()) {
            moveTo(ns);
        }
    }

    static void scoutMiner(MapLocation at) throws GameActionException {
        int i = rc.getRoundNum() - rc.getRoundNum()%10;
        int u = 0;
        while(u < rc.getRoundNum()){
            receiveBroadcast(i);
            u += 10;
            i -= 10;
        }
        if(at.distanceSquaredTo(HQ) > 16) {
            moveTo(HQ);
        }
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
        if(robotsBuilt < 2 && rc.getRoundNum() < 125) {
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
//                    addAndBroadcast(new Information(0,2,0));
                    robotsBuilt++;
                }
        }else if(robotsBuilt < 6 && rc.getRoundNum() >= 125 && rc.getTeamSoup() > 505){
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
//                    if (robotsBuilt==3 || robotsBuilt==4){
//                        addAndBroadcast(new Information(0,2,1));
//                    }
//                    else {
//                        addAndBroadcast(new Information(0,2,0));
//                    }

                }
        }else if(rc.getRoundNum() > 600 && robotsBuilt < 8){
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
                }
        }else if(rc.getRoundNum() > 1000){
            for (Direction dir : directions)
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    robotsBuilt++;
                }
        }
    }
    //Builds Drones
    static void runFulfillmentCenter() throws GameActionException {
        if(!closeEnemyNetGun()) {
            if (robotsBuilt < 1) {
                for (Direction dir : randomDirections())
                    if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                        robotsBuilt++;
//                        addAndBroadcast(new Information(0,1,0));//scout
                        rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                    }
            } else if (rc.getRoundNum() < 300 && rc.getRoundNum() > 149 && robotsBuilt < 3 && rc.getTeamSoup() > 205 /*&& rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent()).length > 0*/) {
                for (Direction dir : randomDirections()) {
                    if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                        robotsBuilt++;
//                        if (robotsBuilt==2) {
//                            addAndBroadcast(new Information(0, 1, 1));//hover
//                        }
//                        else if (robotsBuilt==3){
//                            addAndBroadcast(new Information(0,1,2));//killEnemy
//                        }
//                        else if (robotsBuilt==4){
//                            addAndBroadcast(new Information(0,1,2));
//                        }
//                        else if (robotsBuilt==5){
//                            addAndBroadcast(new Information(0,1,1));
//                        }
                        rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                    }
                }
            } else if (rc.getRoundNum() > 900 && rc.getTeamSoup() > 210) {
                for (Direction dir : randomDirections()) {
                    if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                        robotsBuilt++;
//                        addAndBroadcast(new Information(0,1,2));//killEnemy
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
        System.out.println(task);
        if(rc.getRoundNum() > 20) {
            receiveBroadcast(rc.getRoundNum() - 1);
        }
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
        if(at.distanceSquaredTo(home) < 3) {
        	task = "castle";
        }
        enRush(at);

        if(nextToWell(at) && rc.getDirtCarrying() < 25){
            rc.digDirt(at.directionTo(closestWell(at)));
        }
        if(rc.getDirtCarrying() == 0){
            moveTo(closestWell(at));
        }
        if(isDip(at)){
            rc.depositDirt(Direction.CENTER);
        }
        for(Direction d : directions){
            MapLocation m = at.add(d);
            if(isDip(m)){
                rc.depositDirt(d);
            }
        }
        if(!findDip(at) && baseLevel< 9){
            baseLevel += 3;
        }
        moveTo(HQ);
    }

    static boolean isDip(MapLocation m) throws GameActionException{
        return (rc.canSenseLocation(m) && rc.senseElevation(m) < baseLevel && rc.senseElevation(m) > -50 && !m.isAdjacentTo(HQ) && !isWell(m) && withinWalls(m) && !hasBuilding(m));
    }

    static boolean findDip(MapLocation at) throws GameActionException{
        for(int x = -4; x < 5; x ++){
            for(int y = -4; y < 5; y++){
                MapLocation m = new MapLocation(at.x + x, at.y + y);
                if(isDip(m)){
                    moveTo(m);
                    return true;
                }
            }
        }
        return false;
    }

    static boolean hasBuilding(MapLocation m){
        for(RobotInfo r : rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam())){
            if((r.getType() == RobotType.DESIGN_SCHOOL || r.getType() == RobotType.VAPORATOR || r.getType() == RobotType.REFINERY || r.getType() == RobotType.HQ || r.getType() == RobotType.FULFILLMENT_CENTER)){
                if(r.getLocation().equals(m)){
                    return true;
                }
            }
        }
        return  false;
    }

    static boolean nextToWell(MapLocation at){
        if(well1 != null && at.isAdjacentTo(well1)){
            return true;
        }
        if(well2 != null && at.isAdjacentTo(well2)){
            return true;
        }
        if(well3 != null && at.isAdjacentTo(well3)){
            return true;
        }
        if(well4 != null && at.isAdjacentTo(well4)){
            return true;
        }
        return false;
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

    static boolean onTheWall(MapLocation m) {
        if ((m.x == baseX1 || m.x == baseX2 || m.y == baseY1 || m.y == baseY2) && m.x != 0 && m.x != rc.getMapWidth() - 1 && m.y != 0 && m.y != rc.getMapHeight() - 1) {
            return true;
        }
        return false;
    }

    static boolean withinWalls(MapLocation m){
        return (m.x >= baseX1 && m.x <= baseX2 && m.y >= baseY1 && m.y <= baseY2);
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
        }else if(rc.onTheMap(hLeft) && rc.canSenseLocation(hLeft) && !rc.isLocationOccupied(hLeft) && !at.equals(hRight)){
             moveTo(hLeft);
        } else if(rc.onTheMap(hRight) && rc.canSenseLocation(hRight) && !rc.isLocationOccupied(hRight) && !at.equals(hLeft)){
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
            if(rc.getDirtCarrying() > 8 || (rc.getRoundNum() > 1000 && rc.getDirtCarrying() > 0))
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
            if(rc.getDirtCarrying() > 8 || (rc.getRoundNum() > 1000 && rc.getDirtCarrying() > 0))
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
        if(rc.getRoundNum() > 1000 && (task.equals("hover") || task.equals("scout"))){
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

            if(initialRun && souploc != null && !rc.isCurrentlyHoldingUnit()){
                pickUpMiner(loc);
                moveToDrone(HQ);
            }
            if(rc.isCurrentlyHoldingUnit()){
                if(souploc == null){
                    souploc = getClosestSoup(loc);
                }
                if(loc.isAdjacentTo(souploc)){
                    if(rc.canDropUnit(loc.directionTo(souploc)) && !rc.senseFlooding(souploc)){
                        rc.dropUnit(loc.directionTo(souploc));
                        souploc = null;
                    }else if(rc.canSenseLocation(loc.add(loc.directionTo(souploc).rotateLeft())) && rc.canDropUnit(loc.directionTo(souploc).rotateLeft()) && !rc.senseFlooding(loc.add(loc.directionTo(souploc).rotateLeft()))){
                        rc.dropUnit(loc.directionTo(souploc).rotateLeft());
                        souploc = null;
                    }else if(rc.canSenseLocation(loc.add(loc.directionTo(souploc).rotateRight())) && rc.canDropUnit(loc.directionTo(souploc).rotateRight()) && !rc.senseFlooding(loc.add(loc.directionTo(souploc).rotateRight()))){
                        rc.dropUnit(loc.directionTo(souploc).rotateRight());
                        souploc = null;
                    }
                }
                moveToDrone(souploc);
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
                moveToDroneHover(HQ);
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

            if(rc.isCurrentlyHoldingUnit() && carryingteammate){
                for(Direction g : directions){
                    MapLocation check= loc.add(g);
                    if(rc.onTheMap(check) && check.distanceSquaredTo(EnemyHQ) < 9 && !rc.senseFlooding(check) && rc.canDropUnit(g)){
                        rc.dropUnit(g);
                    }
                }
                moveToCrunch(EnemyHQ);
            }
            if(rc.isCurrentlyHoldingUnit() && !carryingteammate){
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
            if(rc.getRoundNum() > 150){
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
                    if (at.distanceSquaredTo(HQ) > 36){
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
            else{
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
        if (!isHQFull(at) && rc.getRoundNum() > 800) {
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
                if (rc.canDropUnit(g) && rc.onTheMap(at.add(g))&& at.add(g).distanceSquaredTo(HQ) > 8) {
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
                if (rc.canDropUnit(g) && rc.onTheMap(at.add(g))&& at.add(g).isAdjacentTo(HQ)) {
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
                if(!nextToSoup(i.getLocation()) && i.getSoupCarrying() < 90) {
                    minors.add(i);
                    if (rc.canPickUpUnit(i.getID())) {
                        heldUnit = i;
                        rc.pickUpUnit(i.getID());
                    }
                }
            }
        }
        if(!minors.isEmpty()) {
            moveToDrone(closestEnemyRobot(at, minors));
        }
    }
    static boolean nextToSoup(MapLocation at) throws GameActionException{
        if(rc.senseSoup(at) > 0){
            return true;
        }
        for(Direction d: directions){
            if(rc.canSenseLocation(at.add(d)) && rc.senseNearbySoup(at, 25).length > 0) {
                return true;
            }
        }
        return false;
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
            if(rc.canSenseLocation(m)) {
                totS += rc.senseSoup(m);
            }
        }
        if(totS > 200 && souploc == null){
            MapLocation h = getClosestSoup(at);
            if(h.distanceSquaredTo(HQ) > 144 && !rc.senseFlooding(h)) {
                souploc = h;
            }
        }
        if(souploc != null && rc.canSenseLocation(souploc)){
            if(rc.senseSoup(souploc) == 0 || rc.senseFlooding(souploc)){
                soup.remove(souploc);
                if(!soup.isEmpty()){
                    souploc = getClosestSoup(at);
                }else{
                    souploc = null;
                }
            }
        }

        if( pos1 != null && rc.canSenseLocation(pos1) && !rc.isLocationOccupied(pos1)){
            pos1 = null;
        }
        if(pos2 != null && rc.canSenseLocation(pos2) && !rc.isLocationOccupied(pos2)){
            pos2 = null;
        }
        if(pos3 != null && rc.canSenseLocation(pos3) && !rc.isLocationOccupied(pos3)){
            pos3 = null;
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

        if(initialRun){
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
        if (scoutDest == null) {
                scoutDest = pos1;
                scouted.add(pos1);
        }
        if (pos1 == null || at.isWithinDistanceSquared(pos1, 26)) {
                scoutDest = pos3;
                scouted.add(pos3);
        }
        if (pos3 == null || at.isWithinDistanceSquared(pos3, 26) && (pos1 == null || scouted.contains(pos1))) {
            scoutDest = mapCenter;
            initialRun = true;
            scouted.add(scoutDest);
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
    static int inting=0;
    static void moveTo(MapLocation dest) throws GameActionException{

        if (!rc.onTheMap(dest)){
            return;
        }
        if (!dest.equals(prevdest)){
            prevdest=dest;
            prevLocations.clear();
            inting=0;
        }

        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);
        rc.setIndicatorLine(rc.getLocation(), dest, 0, 0, 0);
        //See if general direction is valid

        prevLocations.add(loc);

        if(canMoveTo(loc, moveDirection) && canStepOut(loc.add(moveDirection)) && wetfloor(loc.add(moveDirection))){
            path = moveDirection.opposite();
            tryMove(moveDirection);
        }else if(canMoveTo(loc, moveDirection.rotateLeft()) && canStepOut(loc.add(moveDirection.rotateLeft())) && wetfloor(loc.add(moveDirection.rotateLeft()))){
            path = moveDirection.rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight()) && canStepOut(loc.add(moveDirection.rotateRight())) && wetfloor(loc.add(moveDirection.rotateRight()))) {
            path = moveDirection.rotateRight().opposite();
            tryMove(moveDirection.rotateRight());
        }else if(canMoveTo(loc, moveDirection.rotateLeft().rotateLeft()) && canStepOut(loc.add(moveDirection.rotateLeft().rotateLeft())) && wetfloor(loc.add(moveDirection.rotateLeft().rotateLeft()))) {
            path = moveDirection.rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight().rotateRight()) && canStepOut(loc.add(moveDirection.rotateRight().rotateRight())) && wetfloor(loc.add(moveDirection.rotateRight().rotateRight()))) {
            path = moveDirection.rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight());
        }else if(canMoveTo(loc, moveDirection.rotateLeft().rotateLeft().rotateLeft()) && canStepOut(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft())) && wetfloor(loc.add(moveDirection.rotateLeft().rotateLeft().rotateLeft()))) {
            path = moveDirection.rotateLeft().rotateLeft().rotateLeft().opposite();
            tryMove(moveDirection.rotateLeft().rotateLeft().rotateLeft());
        }else if(canMoveTo(loc, moveDirection.rotateRight().rotateRight().rotateRight()) && canStepOut(loc.add(moveDirection.rotateRight().rotateRight().rotateRight())) && wetfloor(loc.add(moveDirection.rotateRight().rotateRight().rotateRight()))) {
            path = moveDirection.rotateRight().rotateRight().rotateRight().opposite();
            tryMove(moveDirection.rotateRight().rotateRight().rotateRight());
        }else if(canMoveTo(loc, moveDirection.opposite()) && canStepOut(loc.add(moveDirection.opposite()))) {
            path = moveDirection;
            tryMove(moveDirection.opposite());
        } else if(!(task.equals("first") || !task.equals("terraform"))){
            if(inting<5) {
                prevLocations.remove(loc.add(path));
            }
            tryMove(path);
            inting++;
        }else if(task.equals("first")){
            tryMove(loc.directionTo(HQ));
            tryMove(loc.directionTo(HQ).rotateRight());
            tryMove(loc.directionTo(HQ).rotateLeft());

        }
    }



    static void bugMove(MapLocation dest) throws GameActionException{
        MapLocation at = rc.getLocation();
        Direction moveDirection = at.directionTo(dest);
        rc.setIndicatorLine(at, dest, 0,0,0);
        if(toGo == null){
            toGo = dest;
        }
        if(!dest.equals(toGo)){
            toGo = dest;
            closest = 1000000;
        }

        if(at.distanceSquaredTo(dest) < closest){
            closest = at.distanceSquaredTo(dest);
        }
        if(canMoveCloser(dest)){
            findClosestMove(dest);
        }else {
            Direction[] moves = {moveDirection.rotateRight().rotateRight(), moveDirection.rotateRight().rotateRight().rotateRight(), moveDirection.rotateRight().rotateRight().rotateRight().rotateRight(), moveDirection.rotateRight().rotateRight().rotateRight().rotateRight().rotateRight(), moveDirection.rotateRight().rotateRight().rotateRight().rotateRight().rotateRight().rotateRight()};
            for (Direction d : moves) {
                if (canMoveTo(at, d)) {
                    tryMove(d);
                }
            }
        }


    }

    static boolean canMoveCloser(MapLocation dest) throws GameActionException{
        MapLocation at = rc.getLocation();
        int closer = closest;
        for(Direction d : directions){
            if(canMoveTo(at, d) && at.add(d).distanceSquaredTo(dest) < closest && canStepOut(at.add(d)) && wetfloor(at.add(d))){
                return true;
            }
        }
        return false;
    }

    static void findClosestMove(MapLocation dest) throws GameActionException{
        MapLocation at = rc.getLocation();
        int closer = closest;
        Direction ret = Direction.CENTER;
        for(Direction d : directions){
            int n = at.add(d).distanceSquaredTo(dest);
            if(canMoveTo(at, d) && n < closer && canStepOut(at.add(d)) && wetfloor(at.add(d))){
                closer = n;
                ret = d;
            }
        }
        tryMove(ret);
    }




    static boolean canStepOut(MapLocation m){
        if(task.equals("first") && m.distanceSquaredTo(HQ) > 30){
            return false;
        }
        return true;
    }

    static boolean wetfloor(MapLocation m){
        if(task.equals("terraform") && m.distanceSquaredTo(HQ) < 3){
            return false;
        }
        return true;
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
        rc.setIndicatorLine(loc, dest, 0, 0, 0);
//        if(loc.distanceSquaredTo(HQ) < 64) {
//            nonoDirections = new Direction[]{path};
//        }else {
//            nonoDirections = new Direction[]{Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST, path};
//        }
//        ArrayList<Direction> ew = new ArrayList<>();
//        for( Direction d : nonoDirections){
//            ew.add(d);
//        }
        prevLocations.add(loc);
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
        if(pos1!= null && move.distanceSquaredTo(pos1) < 16){
            return true;
        }
        if(pos2!= null && move.distanceSquaredTo(pos2) < 16){
            return true;
        }
        if(pos3!= null && move.distanceSquaredTo(pos3) < 16){
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
        message[1] = SUPERSECRET;
        message[2] = code;
        message[3] = x;
        message[4] = y;
        if (rc.canSubmitTransaction(message, cost))
            rc.submitTransaction(message, cost);
    }

    static MapLocation getHQLocation() throws GameActionException {
        //returns the location of HQ
        if (rc.getRoundNum()>11) {
            MapLocation location = null;
            Transaction[] block = rc.getBlock(11);
            if (block.length != 0) {
                for (int i = 0; i < block.length; i++) {
                    int[] message = block[i].getMessage();
                    if (message[1] == SUPERSECRET && message[2] == 1) {
                        location = new MapLocation(message[3], message[4]);
                        return location;
                    }
                }
            }

            return null;
        }
        return null;
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
                        if(message[1] == SUPERSECRET && message[2] == 2) {
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
                        if(message[1] == SUPERSECRET && message[2] == 3) {
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
        if (rc.getTeamSoup()>=cost && !broadcastQueue.isEmpty() && rc.getRoundNum()>50) {
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
            bitSet.set(9 * 16, true);

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
                    bitSet.get(9 * 16)


            ) {
                out.add(bitSet);
            }
        }
        return out;

    }

    static void receiveBroadcast(int round) throws  GameActionException{
        Transaction[] transactions=rc.getBlock(round);
        if(transactions.length==0) return;
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
                if(rc.getType()==RobotType.HQ){
                    broadcastQueue.add(new Information(type,x,y));
                }

                MapLocation next= new MapLocation(x,y);
//                switch (type){//only edit this section
//                    case 0:
//                        if(x==0 && y==0 && rc.getType()==RobotType.DELIVERY_DRONE) { //never use this
//                            task="crunch";
//                        }
//                        else if ( x==1 && rc.getType()==RobotType.DELIVERY_DRONE && task.equals("")) {
//                            if (y==0) {
//                                task = "scout";
//                            }
//                            else if (y==1){
//                                task="hover";
//                            }
//                            else if (y==2){
//                                task="killEnemy";
//                            }
//                            else if (y==3){
//                                task="defend";
//                            }
//                        }
//                        else if ( x==2 && rc.getType()==RobotType.LANDSCAPER && task.equals("")) {
//                            if (y==0) {
//                                task = "castle";
//                            }
//                            else if (y==1){
//                                task="terraform";
//                            }
//                        }
//                        else if (x==3 && y==3 && rc.getType()==RobotType.MINER) {
//                            schoolsBuilt++;
//                        }
//                        else if (x==4 && y==4 && rc.getType()==RobotType.MINER) {
//                            factoriesBuilt++;
//                        }
//
//                            break;
//
//                    case 1:
//                        EnemyHQ=next;
//                        break;
//                    case 2:
//                        if(!soup.contains(next)) {
//                            soup.add(next);
//                        }
//                        break;
//                    case 3:
//                        if(!offensiveEnemyBuildings.contains(next)) {
//                            offensiveEnemyBuildings.add(next);
//                        }
//                        break;
//                    case 4:
//
//                        if(!refineries.contains(next)) {
//                            refineries.add(next);
//                        }
//                        break;
//                    case 5:
//                        if(!oppNet.contains(next)) {
//                            oppNet.add(next);
//                        }
//                        break;
//                }


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
    static void checkStatus(int round) throws GameActionException{
        int check=round - (round%STATUSFREQ);
//        if (check==0){
//            if (rc.getType()==RobotType.MINER){
//                task = "castle";
//            }
//            else if (rc.getType()==RobotType.DELIVERY_DRONE){
//                task= "scout";
//            }
//        }
//        else{
            receiveBroadcast(check);
//        }
    }
    //__________________________________________________________________________________________________________________
}

