package Richardplayer;
import battlecode.common.*;

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

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
        if (rc.getRoundNum() < 15) 
            tryBuild(RobotType.MINER, Direction.SOUTH);
        //post the HQ location to blockchain
        if (rc.getRoundNum() == 1)
        	postLocation(1, rc.getLocation().x, rc.getLocation().y, 2);
        if(rc.getRoundNum() == 2) {
        	if(getHQLocation() == null)
        		postLocation(10, rc.getLocation().x, rc.getLocation().y, 5);
        }
        updateEnemyHQLocation();
    }

    static void runMiner() throws GameActionException {
        tryBlockchain();
        tryMove(randomDirection());
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : directions)
            tryBuild(RobotType.FULFILLMENT_CENTER, dir);
        for (Direction dir : directions)
            if (tryRefine(dir))
                System.out.println("I refined soup! " + rc.getTeamSoup());
        for (Direction dir : directions)
            if (tryMine(dir))
                System.out.println("I mined soup! " + rc.getSoupCarrying());
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
            return true;
        } else return false;
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

    static void postLocation(int code, int x, int y, int cost) throws GameActionException {
    	/* Code to be placed in array[2]
    	 * 1 : HQ
    	 * 2 : Soup
    	 * 3 : Enemy HQ
    	*/ 
    	int[] message = new int[7];
        message[1] = 1231241;
    	message[2] = 1;
        message[3] = x;
        message[4] = y;
        if (rc.canSubmitTransaction(message, cost))
        	rc.submitTransaction(message, cost);
    }
    
    static MapLocation getHQLocation() throws GameActionException {
    	//returns the location of HQ
    	MapLocation location = null;
    	for(int k = 1; k < rc.getRoundNum(); k++) {
    		Transaction[] block = rc.getBlock(k);
    		for(int i = 0; i < 7; i++) {
    			int[] message = block[i].getMessage();
    			if(message[1] == 1231241 && message[2] == 1) {
    				location = new MapLocation(message[3], message[4]);
    				return location;
    			}
    		}
    	}
    	return location;
    }
    
    static MapLocation getSoupLocation() throws GameActionException {
    	/* Code to be placed in array[2]
    	 * 1 : HQ
    	 * 2 : Soup
    	 * 3 : Enemy HQ
    	*/ 
    	MapLocation location = null;
    	for(int k = 1; k < 60; k++) {
    		Transaction[] block = rc.getBlock(k);
    		for(int i = 0; i < 7; i++) {
    			int[] message = block[i].getMessage();
    			if(message[1] == 1231241 && message[2] == 2) {
    				location = new MapLocation(message[3], message[4]);
    				return location;
    			}
    		}
    	}
    	return location;
    }
    
    static MapLocation getEnemyHQLocation() throws GameActionException {
    	//returns the enemy HQ location as a MapLocation
    	MapLocation location = null;
    	for(int k = rc.getRoundNum(); k > rc.getRoundNum()-60; k--) {
    		if(k > 0) {
    			Transaction[] block = rc.getBlock(k);
    			for(int i = 0; i < 7; i++) {
    				int[] message = block[i].getMessage();
    				if(message[1] == 1231241 && message[2] == 3) {
    					location = new MapLocation(message[3], message[4]);
    					return location;
    				}
    			}
    		}
    	}
    	return location;
    }
    
    static void updateEnemyHQLocation() throws GameActionException {
    	//looks for enemy hq location in block chain and moves it to a more recent round
    	for(int k = rc.getRoundNum()-60; k > rc.getRoundNum()-100; k--) {
    		if(k > 0) {
    			Transaction[] block = rc.getBlock(k);
    			for(int i = 0; i < 7; i++) {
    				int[] message = block[i].getMessage();
    				if(message[1] == 1231241 && message[2] == 3) {
    					postLocation(3, message[3], message[4], 2);
    				}
    			}
    		}
    	}
    }
    
    static void tryBlockchain() throws GameActionException {
        //delete
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