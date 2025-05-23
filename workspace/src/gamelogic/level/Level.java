package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData() {
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition * tileSize, yPosition * tileSize, this)); // TODO: objects vs
																									// tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if (flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}

	// Pre condition: col and row are both greater than 0; col <
	// map.getTiles().length; row < map.getTiles()[col].length; numSquaresToFill >
	// 0; placedThisRound is empty
	// Post condition: gas is made at the given location
	private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
		Gas g = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
		map.addTile(col, row, g);
		placedThisRound.add(g);
		numSquaresToFill--;

		while (placedThisRound.size() > 0 && numSquaresToFill > 0) {
			int currCol = placedThisRound.get(0).getCol();
			int currRow = placedThisRound.get(0).getRow();

			// Expand up
			if ((currRow - 1 >= 0) && !map.getTiles()[currCol][currRow - 1].isSolid()
					&& !(map.getTiles()[currCol][currRow - 1] instanceof Gas)) {
				Gas gas = new Gas(currCol, currRow - 1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol, currRow - 1, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			// Check if we have tiles left
			if (numSquaresToFill <= 0) {
				return;
			}

			// Expand up and right
			if ((currRow - 1 >= 0
					&& currCol + 1 < map.getTiles().length) && !map.getTiles()[currCol + 1][currRow - 1].isSolid()
					&& !(map.getTiles()[currCol + 1][currRow - 1] instanceof Gas)) {
				Gas gas = new Gas(currCol + 1, currRow - 1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol + 1, currRow - 1, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			// Check if we have tiles left
			if (numSquaresToFill <= 0) {
				return;
			}

			// Expand up and left
			if ((currRow - 1 >= 0
					&& currCol - 1 >= 0) && !map.getTiles()[currCol - 1][currRow - 1].isSolid()
					&& !(map.getTiles()[currCol - 1][currRow - 1] instanceof Gas)) {
				Gas gas = new Gas(currCol - 1, currRow - 1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol - 1, currRow - 1, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			// Check if we have tiles left
			if (numSquaresToFill <= 0) {
				return;
			}

			// Expand right
			if ((currCol + 1 < map.getTiles().length) && !map.getTiles()[currCol + 1][currRow].isSolid()
					&& !(map.getTiles()[currCol + 1][currRow] instanceof Gas)) {
				Gas gas = new Gas(currCol + 1, currRow, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol + 1, currRow, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			// Check if we have tiles left
			if (numSquaresToFill <= 0) {
				return;
			}

			// Expand left
			if ((currCol - 1 >= 0) && !map.getTiles()[currCol - 1][currRow].isSolid()
					&& !(map.getTiles()[currCol - 1][currRow] instanceof Gas)) {
				Gas gas = new Gas(currCol - 1, currRow, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol - 1, currRow, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			// Check if we have tiles left
			if (numSquaresToFill <= 0) {
				return;
			}

			// Expand down
			if ((currRow + 1 < map.getTiles()[currCol].length) && !map.getTiles()[currCol][currRow + 1].isSolid()
					&& !(map.getTiles()[currCol][currRow + 1] instanceof Gas)) {
				Gas gas = new Gas(currCol, currRow + 1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol, currRow + 1, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			// Check if we have tiles left
			if (numSquaresToFill <= 0) {
				return;
			}

			// Expand down + right
			if ((currRow + 1 < map.getTiles()[currCol].length && currCol + 1 < map.getTiles().length)
					&& !map.getTiles()[currCol + 1][currRow + 1].isSolid()
					&& !(map.getTiles()[currCol + 1][currRow + 1] instanceof Gas)) {
				Gas gas = new Gas(currCol + 1, currRow + 1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol + 1, currRow + 1, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			// Check if we have tiles left
			if (numSquaresToFill <= 0) {
				return;
			}

			// Expand down + left
			if ((currRow + 1 < map.getTiles()[currCol].length && currCol - 1 >= 0)
					&& !map.getTiles()[currCol - 1][currRow + 1].isSolid()
					&& !(map.getTiles()[currCol - 1][currRow + 1] instanceof Gas)) {
				Gas gas = new Gas(currCol - 1, currRow + 1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(currCol - 1, currRow + 1, gas);
				placedThisRound.add(gas);
				numSquaresToFill--;
			}

			placedThisRound.remove(0);
			System.out.println("Tiles left: " + numSquaresToFill);

		}

	}

	// #############################################################################################################
	// Your code goes here!
	// Please make sure you read the rubric/directions carefully and implement the
	// solution recursively!
	// pre condition: fullness is a number between 0 and 4
	// post condition: a water block is created at the given coordinates with the
	// given fullness level
	private void water(int col, int row, Map map, int fullness) {

		String imageName = "";
		// The fullness of the next block being spread horizontally
		int nextBlock = 0;
		// Whether or not water is falling
		boolean isFalling = false;

		// If col or row are out of bounds then return
		if (col < 0 || col >= map.getTiles().length || row < 0 || row >= map.getTiles()[col].length) {
			return;
		}

		// If falling water is on the bottom row then return
		if (row == map.getTiles()[col].length - 1 && fullness == 0) {
			return;
		}

		// If the row under the current one isn't off the map then remember its
		// posistion
		if (row + 1 < map.getTiles()[col].length) {
			Tile nextTile = map.getTiles()[col][row + 1];

			// If the next tile isn't water and isn't solid then water is falling
			if (!(nextTile instanceof Water) && !nextTile.isSolid()) {
				isFalling = true;
			}
		}

		// Assign the image of the water depending on fullness
		if (fullness == 3) {
			imageName = "Full_water";
		} else if (fullness == 2) {
			imageName = "Half_water";
		} else if (fullness == 1) {
			imageName = "Quarter_water";
		} else if (fullness == 0) {
			imageName = "Falling_water";
		}

		// If water already exists on a given tile, don't spawn more water on top of it
		if (!(map.getTiles()[col][row] instanceof Water) || fullness == 3) {
			Water w = new Water(col, row, tileSize, tileset.getImage(imageName), this, fullness);
			map.addTile(col, row, w);
		}

		// check if we can go down
		if (isFalling) {
			fullness = 0;
			water(col, row + 1, map, fullness);
			return;
			// If we're not falling and the block is falling water then land into a full
			// block
		} else if (!isFalling && fullness == 0) {
			fullness = 3;
			water(col, row, map, fullness);
			return;
		}

		// if we can’t go down go left and right.
		// right
		// If water isn't falling then it must be spreading
		if (fullness > 0) {

			// If the water is a quarter block then the next water block should be too
			if (fullness == 1) {
				nextBlock = 1;
				// If it isn't, then the next block should be 1 block less than the previous
			} else {
				nextBlock = fullness - 1;
			}

			if (col + 1 < map.getTiles().length && !(map.getTiles()[col + 1][row] instanceof Water)
					&& !map.getTiles()[col + 1][row].isSolid()) {
				water(col + 1, row, map, nextBlock);
			}
			// left
			if (col - 1 >= 0 && !(map.getTiles()[col - 1][row] instanceof Water)
					&& !map.getTiles()[col - 1][row].isSolid()) {
				water(col - 1, row, map, nextBlock);
			}
		}
	}

	public void draw(Graphics g) {
		g.translate((int) -camera.getX(), (int) -camera.getY());
		// Draw the map
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				Tile tile = map.getTiles()[x][y];
				if (tile == null)
					continue;
				if (tile instanceof Gas) {

					int adjacencyCount = 0;
					for (int i = -1; i < 2; i++) {
						for (int j = -1; j < 2; j++) {
							if (j != 0 || i != 0) {
								if ((x + i) >= 0 && (x + i) < map.getTiles().length && (y + j) >= 0
										&& (y + j) < map.getTiles()[x].length) {
									if (map.getTiles()[x + i][y + j] instanceof Gas) {
										adjacencyCount++;
									}
								}
							}
						}
					}
					if (adjacencyCount == 8) {
						((Gas) (tile)).setIntensity(2);
						tile.setImage(tileset.getImage("GasThree"));
					} else if (adjacencyCount > 5) {
						((Gas) (tile)).setIntensity(1);
						tile.setImage(tileset.getImage("GasTwo"));
					} else {
						((Gas) (tile)).setIntensity(0);
						tile.setImage(tileset.getImage("GasOne"));
					}
				}
				if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
					tile.draw(g);
			}
		}

		// Draw the enemies
		for (int i = 0; i < enemies.length; i++) {
			enemies[i].draw(g);
		}

		// Draw the player
		player.draw(g);

		// used for debugging
		if (Camera.SHOW_CAMERA)
			camera.draw(g);
		g.translate((int) +camera.getX(), (int) +camera.getY());
	}

	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}