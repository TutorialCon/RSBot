package org.rsbot.script.background;

import org.rsbot.script.BackgroundScript;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Web;
import org.rsbot.script.util.io.WebQueue;
import org.rsbot.script.wrappers.RSTile;

import java.util.HashMap;

@ScriptManifest(name = "Web Data Collector", authors = {"Timer"})
public class WebData extends BackgroundScript {
	private RSTile lastMapBase = null;
	private int lastLevel = -1;
	public final HashMap<RSTile, Integer> collectionMap = new HashMap<RSTile, Integer>();
	private static final Object botCollectionLock = new Object();

	@Override
	public boolean activateCondition() {
		final RSTile curr_base = game.getMapBase();
		final int curr_plane = game.getPlane();
		return Web.isLoaded() && game.isLoggedIn() && ((lastMapBase == null || !lastMapBase.equals(curr_base)) || (lastLevel == -1 || lastLevel != curr_plane));
	}

	@Override
	public int loop() {
		try {
			sleep(5000);
			final RSTile currentMapBase = game.getMapBase();
			final int currentLevel = game.getPlane();
			collectionMap.clear();
			if (!currentMapBase.equals(game.getMapBase())) {
				return -1;
			}
			lastMapBase = currentMapBase;
			lastLevel = currentLevel;
			final int tileKeys[][] = walking.getCollisionFlags(currentLevel).clone();
			for (int queryX = 3; queryX < 102; queryX++) {
				for (int queryY = 3; queryY < 102; queryY++) {
					final RSTile analysisTile = new RSTile(currentMapBase.getX() + queryX, currentMapBase.getY() + queryY, currentLevel);
					final int xBase = currentMapBase.getX(), yBase = currentMapBase.getY();
					final int localX = analysisTile.getX() - xBase, localY = analysisTile.getY() - yBase;
					final RSTile collisionOffset = walking.getCollisionOffset(currentLevel);
					final int xOffset = collisionOffset.getX();
					final int yOffset = collisionOffset.getY();
					final int keyIndex_x = localX - xOffset, keyIndex_y = localY - yOffset;
					final int key = tileKeys[keyIndex_x][keyIndex_y];
					synchronized (botCollectionLock) {
						if (!Web.rs_map.containsKey(analysisTile) && (!RSTile.Walkable(key) || RSTile.Questionable(key))) {
							collectionMap.put(analysisTile, key);
						} else {
							if (Web.rs_map.containsKey(analysisTile) && Web.rs_map.get(analysisTile) != key) {
								WebQueue.Remove(analysisTile);
								lastMapBase = null;
								lastLevel = -1;
							}
						}
					}
				}
			}
			WebQueue.Add(collectionMap);
			return -1;
		} catch (final Exception ignored) {
		}
		return -1;
	}

	@Override
	public int iterationSleep() {
		return 2000;
	}
}