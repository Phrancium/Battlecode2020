package Frankplayer1;
import battlecode.common.*;

import javax.naming.MalformedLinkException;
import java.util.HashMap;
import java.util.Map;

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
    static Map<Direction, Integer> dIndex = new HashMap<Direction, Integer>();
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    static Direction lastT;
    static MapLocation destination;

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
        lastT = directions[3];
        destination = null;


        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
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

    static void runHQ() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.MINER, dir);
        RobotInfo[] r = rc.senseNearbyRobots();
        for(RobotInfo s : r){
            if(s.getTeam() != rc.getTeam() && rc.canShootUnit(s.getID())){
                rc.shootUnit(s.getID());
            }
        }
    }

    static void runMiner() throws GameActionException {
        MapLocation curr = rc.getLocation();
        if(canMine() && rc.getSoupCarrying() < 98) {
            mineSoup();
        }else if(rc.getSoupCarrying() > 98){
            goHome(curr);
        }
        if(!(destination == null)){
            if(destination.x == curr.x && destination.y == curr.y){
                destination = null;
            }else{
                moveTo(curr, destination);
            }
        }else {
            findSoup(curr);
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
    }
    /**
     * Scouting method run at beginning to find soup
     *
     * @throws GameActionException
     */
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


    static void findEnemyHQ(MapLocation at) throws GameActionException{
        int hqX = 0;
        int hqY = 0;
        int mapW = rc.getMapWidth();
        int mapH = rc.getMapHeight();
        MapLocation dest1 = new MapLocation(hqX, mapH-hqY);
        MapLocation dest2 = new MapLocation(mapW-hqX, mapH-hqY);

        RobotInfo[] rob = rc.senseNearbyRobots();
        for(RobotInfo d : rob){
            if(d.getType().name() == "HQ"){

            }
        }
        if(at.x < dest1.x){
            moveTo(at, dest1);
        }else{
            moveTo(at, dest2);
        }
    }


    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {

    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {

    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
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
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            lastT = oppositeD(dir);
            return true;
        } else return false;
    }

    static Direction oppositeD(Direction d){
        switch (d) {
            case NORTH:
                return Direction.SOUTH;
            case NORTHEAST:
                return Direction.SOUTHWEST;
            case EAST:
                return Direction.WEST;
            case SOUTHEAST:
                return Direction.NORTHWEST;
            case SOUTH:
                return Direction.NORTH;
            case SOUTHWEST:
                return Direction.NORTHEAST;
            case WEST:
                return Direction.EAST;
            case NORTHWEST:
                return Direction.SOUTHEAST;
        }
        return null;
    }
    /**
     * Scouting method run at beginning to find soup
     *
     * @throws GameActionException
     */
    static void findSoup(MapLocation m) throws GameActionException {
        Direction d = randomDirection();
        int selfX = m.x;
        int selfY = m.y;
        for(int x = -6; x <7; x++){
            for (int y = -6; y < 7; y++){
                MapLocation check = new MapLocation(selfX + x, selfY + y);
                if (rc.canSenseLocation(check)){
                    if(rc.senseSoup(check) > 0){
                        moveTo(m, new MapLocation(check.x-1, check.y));
                        //upload location of soup to blockchain?
                    }
                }
            }
        }
        tryMove(d);
    }
    /**
     * move towards a certain MapLocation
     *
     * @param at robot's current location
     * @param to where robot wants to move to
     * @throws GameActionException
     */

    static void moveTo(MapLocation at, MapLocation to) throws GameActionException {
        int xDiff = at.x - to.x;
        int yDiff = at.y - to.y;
//        Direction goal = at.directionTo(to);
//        int start = dIndex.get(goal);
        destination = to;
        if (Math.abs(xDiff) > Math.abs(yDiff)) {
            if (xDiff > 0) {
                if (yDiff > 0) {
                    if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.WEST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.NORTH)) {
                    } else if (tryMove(Direction.EAST)) {
                    }

                } else if (yDiff < 0) {
                    if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.WEST)) {
                    } else if (tryMove(Direction.NORTH)) {
                    } else if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.EAST)) {
                    }
                } else {
                    if (tryMove(Direction.WEST)) {
                    } else if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.NORTH)) {
                    }
                }
            } else {
                if (yDiff > 0) {
                    if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.EAST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.EAST)) {
                    }
                } else if (yDiff < 0) {
                    if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.EAST)) {
                    } else if (tryMove(Direction.NORTH)) {
                    } else if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.WEST)) {
                    }
                } else {
                    if (tryMove(Direction.EAST)) {
                    } else if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.NORTH)) {
                    }
                }
            }
        } else {
            if (yDiff > 0) {
                if (xDiff > 0) {
                    if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.WEST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.NORTH)) {
                    } else if (tryMove(Direction.EAST)) {
                    }
                } else if (xDiff < 0) {
                    if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.EAST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.EAST)) {
                    }
                } else {
                    if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.EAST)) {
                    } else if (tryMove(Direction.WEST)) {
                    }
                }
            } else {
                if (xDiff > 0) {
                    if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.WEST)) {
                    } else if (tryMove(Direction.NORTH)) {
                    } else if (tryMove(Direction.SOUTHWEST)) {
                    } else if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.EAST)) {
                    }
                } else if (xDiff < 0) {
                    if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.EAST)) {
                    } else if (tryMove(Direction.NORTH)) {
                    } else if (tryMove(Direction.SOUTHEAST)) {
                    } else if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.SOUTH)) {
                    } else if (tryMove(Direction.WEST)) {
                    }
                } else {
                    if (tryMove(Direction.NORTH)) {
                    } else if (tryMove(Direction.NORTHWEST)) {
                    } else if (tryMove(Direction.NORTHEAST)) {
                    } else if (tryMove(Direction.EAST)) {
                    } else if (tryMove(Direction.WEST)) {
                    }
                }
            }
        }
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


    static void tryBlockchain() throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }
}
