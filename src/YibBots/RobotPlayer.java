package YibBots;
import battlecode.common.*;
import java.util.*;

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

    static HashMap<Integer, Direction> directionHashMap = new HashMap<Integer, Direction>();

    static MapLocation initialLoc;

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

        initialLoc = rc.getLocation();

        //fill up directionHashMap 1:Direction.NORTH, etc
        for (int i = 0; i < directions.length; i++){
            directionHashMap.put(i+1, directions[i]);
        }
        //System.out.println(directionHashMap);

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

    static void runHQ() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.MINER, dir);
    }

    static void runMiner() throws GameActionException {
        MapLocation curr = rc.getLocation();
        //right now findSoup() goes to the left of soupmine
        //MINE SOUP
        if (rc.canMineSoup(Direction.EAST) && rc.getSoupCarrying() < 98){
            tryMine(Direction.EAST);
            System.out.println("MINING SOUP");
        }
        //MOVE BACK TO HQ (todo: implement refineries)
        else if (rc.getSoupCarrying() > 90 && !curr.equals(initialLoc)){
            moveTo(initialLoc);
            System.out.println("MOVING BACK TO HQ");
        }
        //DEPOSIT SOUP
        else if(curr.equals(initialLoc)){
            for (Direction dir : directions){
                if(rc.canDepositSoup(dir)){
                    rc.depositSoup(dir, rc.getSoupCarrying());
                    System.out.println("DEPOSIT SOUP");
                }
            }
        }
        //FIND SOUP
        else {
            findSoup(curr);
            System.out.println("FIND SOUP");
        }
        rc.move(Direction.EAST);
        // tryBlockchain();
        // tryMove(randomDirection());
        // if (tryMove(randomDirection()))
        //     System.out.println("I moved!");
        // // tryBuild(randomSpawnedByMiner(), randomDirection());
        // for (Direction dir : directions)
        //     tryBuild(RobotType.FULFILLMENT_CENTER, dir);
        // for (Direction dir : directions)
        //     if (tryRefine(dir))
        //         System.out.println("I refined soup! " + rc.getTeamSoup());
        // for (Direction dir : directions)
        //     if (tryMine(dir))
        //         System.out.println("I mined soup! " + rc.getSoupCarrying());
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

    static void findSoup(MapLocation loc) throws GameActionException{
        Direction d = randomDirection();
        int selfX = loc.x;
        int selfY = loc.y;
        for(int x = -5; x <6; x++){
            for (int y = -5; y < 6; y++){
                MapLocation check = new MapLocation(selfX + x, selfY + y);
                if (rc.canSenseLocation(check)){
                    if(rc.senseSoup(check) > 0){
                        //plusOrMinusOne makes Miner stop either left or right of soup
                        //double rando = Math.random();
                        //int plusOrMinusOne = (int)Math.signum(rando - 0.5);
                        moveTo(new MapLocation(check.x - 1, check.y));
                        //upload location of soup to blockchain?
                    }
                }
            }
        }
        tryMove(d);
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    //Keeps building em Landscapers
    static void runDesignSchool() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.LANDSCAPER, dir);
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {
        //tryBlockchain();
        if(tryMove(randomDirection())){
            System.out.println("I, Landscaper, moved randomly");
        }
        for (Direction dir : directions){
            if (rc.canDigDirt(dir)){
                rc.digDirt(dir);
                System.out.println("I, Landscaper, dug");
            }
        }
        System.out.println("I, Landscaper, am carrying " + rc.getDirtCarrying() + " dirt");
        if(rc.getDirtCarrying() > 10){
            for (Direction dir : directions){
                if (rc.canDepositDirt(dir)){
                    rc.depositDirt(dir);
                    System.out.println("I, Landscaper, deposited dirt");
                }
            }
        }
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
            return true;
        } else return false;
    }

    static void moveTo(MapLocation dest) throws GameActionException{
        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);

        //See if general direction is valid
        if(tryMove(moveDirection)){

        }
        else{
            tryMove();
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
