# Wumpus-World
This is an AI intro class project at UC Irvine -- the implemention of a Knowledge-based Wumpus World AI agent using Java.
## Introduction
> The Wumpus World is a cave consisting of rooms connected by passageways.
   Lurking somewhere in the cave is the terrible Wumpus, a beast that eats
   anyone who enters its room. The Wumpus can be shot by an agent, but the
   agent has only one arrow. Some rooms contain bottomless pits that will
   trap anyone who wanders into these rooms (except for the Wumpus, which
   is too big to fall in). The only mitigating feature of this bleak
   environment is the possibility of finding a heap of gold.
>
### Performance Measure
> The performance measure of an agent is an integer score calculated based
   on the following:
> - Start at 0 points.
> - -1 point for each action taken.
> - -10 for using the arrow (additional to the -1 point).
> - -1000 points for falling into a pit or being eaten by the Wumpus.
> - +1000 for climbing out of the cave with gold.
> 
> The gaming ends either when the agent dies, when the agent climbs out of
  the cave, or when the agent’s score goes below -1000.
>
### Environment
> The environment can be classified as partially observable,
  deterministic, sequential, static, discrete, and single agent.
> - An NxM grid of rooms, where 4<=N, M <=7.
> - The agent always starts in the bottom left square (1,1), facing to
the right.
> - The locations of the gold and the Wumpus are chosen randomly, with a uniform distribution, from the squares other than the start square.
> - Each square other than the start can be a pit, with a 20% probability.
> - The agent dies a miserable death if it enters a square containing a pit or a live Wumpus.
> 
### Acutators
> - The agent can move FORWARD, TURN_LEFT by 90 degrees, or TURN_RIGHT
> - The action GRAB can be used to pick up the gold if it is in the same square as the agent.
> - The action SHOOT can be used to fire an arrow in a straight line in the direction the agent is facing. The arrow continues until it either hits and kills the Wumpus or hits a wall. The agent has only one arrow, so only the first shoot action has any effect.
> - The action CLIMB can be used to climb out of the cave, but only from square (1,1).
> 
### Sensors
> - In the square containing the Wumpus and in the directly (not diagonally) adjacent squares, the agent will perceive a STENCH.
> - In the squares directly adjacent to a pit, the agent will perceive a BREEZE.
> - In the square where the gold is, the agent will perceive a GLITTER.
> - When an agent walks into a wall, it will perceive a BUMP.
> - When the Wumpus is killed, it emits a woeful SCREAM that can be perceived anywhere in the cave. This percept will only be sensed on the turn immediately after the wumpus’s death.
> 
## Implementation
The general idea is to use KB (Knowledge Base) to filter out potential dangerous tiles with different conditions, and only go to tiles that are safe.
I had this class `Tile` to record all sort of information about each room, so we can build KB in further implementation with these attributes.
```
public class Tile {
    boolean notPit		= false;
    boolean notWumpus	= false;
    boolean visited		= false;
    boolean isBrick		= false;
}
```
Note in this project, there is an N*M grid of rooms, where 4 <= N, M <= 7.
```
for(int i = 0; i < 4; i++) {
    for (int j = 0; j < 7; j++) {
    		myBoard[i][j] = new Tile();
    }
}
```
Also, these variables help us record current location (x, y) and facing direction (0, 1, 2, 3 for four different directions):
```
int x 	= 0;	// current x
int y 	= 0;	// current y
int dir = 0;	// current direction
```
What action to return?
1. If `bump` is perceived, we walked into a wall. And so we set a border (`isBrick = true`) for some row/column of tiles.
2. If `glitter` is perceived, grab the gold and go back to tile(0, 0) to climb out of cave.
3. If `stench` is perceived, shoot at all 4 directions to make sure that Wumpus is killed.
4. If `scream` is perceived, we die. But we won't allow this to happen in our strategy.
5. Based on circumstance on the current tile (is there `stench` or `breeze`?), we set the attributes of adjacent tiles.
6. Finally, we go to those tiles that are only 100% safe and repeat the above.
7. If adjecent tiles are all potentially dangerous, we go back home.

This makes sure we won't be killed by Wumpus or fall into a pit, both of which take 1000pt off of the final score. And so the worst case here is that we climb out without gold, which usually scores around -30pt. Therefore in most cases with this safe strategy, the average score will be higher than to risk if you run your agent multiple times.
