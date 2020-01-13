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

    static HashMap<Integer, Direction> dirHash = new HashMap<Integer, Direction>();

    static MapLocation initialLoc;

    static MapLocation souploc;

    static Direction path;

    static String task;

    static boolean moveOnce = false;

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

        souploc = null;

        path = Direction.CENTER;

        task = "idle";

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

    static void runHQ() throws GameActionException {
        //if (rc.getRobotCount() < 5) {
    	if(rc.getRoundNum() == 1) {
    		postLocation(1, rc.getLocation().x, rc.getLocation().y, 2);
    	}
    	if (rc.getRoundNum() < 20) 
    		for (Direction dir : directions)
    			tryBuild(RobotType.MINER, dir);
    	updateEnemyHQLocation();
        //}
    }

    static void runMiner() throws GameActionException {
        MapLocation curr = rc.getLocation();
        scanForSoup(curr);
        souploc = getSoupLocation();
        //build design school
        if (rc.getRobotCount() == 4) {
        	for (Direction dir : directions)
        		tryBuild(RobotType.DESIGN_SCHOOL,dir);
        }
        //MINE SOUP
        if (souploc != null && rc.getSoupCarrying() < 100){
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
            findSoup(curr);
        }
        
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
        scanForSoup(loc);
//        if(getSoupLocation() != null)
//        	moveTo(getSoupLocation());
//        else
        	tryMove(d);
    }

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
//                    	moveTo(new MapLocation(check.x - 1, check.y));
                        break scan;
                        //upload location of soup to blockchain?
                    }
                }
            }
        }
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
        MapLocation HQ = getEnemyHQLocation();
        if(HQ == null)
            findEnemyHQ(rc.getLocation());
        MapLocation current = rc.getLocation();
        //alternate moving with picking up dirt
        if(current.distanceSquaredTo(HQ)>2) {
            zergRush(HQ);
        }
        //if HQ is within range
        else if(rc.getDirtCarrying() > 0) {
            Direction dir = current.directionTo(HQ);
            rc.depositDirt(dir);
        }
        else {
            if(rc.canDigDirt(current.directionTo(HQ).opposite())) {
                rc.digDirt(current.directionTo(HQ).opposite());
            }
        }

    }

    static void zergRush(MapLocation dest) throws GameActionException{
        //Find general direction of destination
        MapLocation loc = rc.getLocation();
        Direction moveDirection = loc.directionTo(dest);

        //dig through barriers
        if(rc.senseElevation(rc.adjacentLocation(moveDirection)) > 3 && rc.senseElevation(rc.adjacentLocation(moveDirection.rotateLeft())) > 3 && rc.senseElevation(rc.adjacentLocation(moveDirection.rotateRight())) > 3){
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
    				if(message[1] == 1231241 && message[2] == 1) {
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
    					if(message[1] == 1231241 && message[2] == 2) {
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
    					if(message[1] == 1231241 && message[2] == 3) {
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
    	for(int k = rc.getRoundNum()-100; k < rc.getRoundNum()-60; k++) {
    		if(k > 0) {
    			Transaction[] block = rc.getBlock(k);
    			if(block.length != 0) {	
    				for(int i = 0; i < block.length; i++) {
    					int[] message = block[i].getMessage();
    					if(message[1] == 1231241 && message[2] == 3) {
    						postLocation(3, message[3], message[4], 2);
    					}	
    				}
    			}
    		}
    	}
    }
    /**robot mines soup **/

    static void mineSoup() throws GameActionException{
        for (Direction l : directions) {
            if (rc.canMineSoup(l)) {
                rc.mineSoup(l);
            }
        }
        moveTo(souploc);
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

    //scouting route to find enemy HQ
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
            postLocation(3, hqX, mapH - hqY, 1);
        }

        RobotInfo[] rob = rc.senseNearbyRobots();
        //scan for enemy HQ
        for(RobotInfo d : rob){
            if(d.getType().name() == "HQ" && d.getTeam() != rc.getTeam()){
                postLocation(3, d.location.x, d.location.y, 1);
            }
        }
        if(hqX < mapW/2) {
            if (at.x < dest1.x - 2) {
                zergRush(dest1);
            } else {
                zergRush(dest2);
            }
        }else{
            if (at.x > dest1.x + 2) {
                zergRush(dest1);
            } else {
                zergRush(dest2);
            }
        }
    }
}

