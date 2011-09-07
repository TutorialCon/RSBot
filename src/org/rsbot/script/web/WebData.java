package org.rsbot.script.web;

import java.util.HashMap;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSTile;

public class WebData {
	private RSTile lastMapBase = null;
	private int lastLevel = -1;
        MethodContext ctx;
	private static final Object botCollectionLock = new Object();
        private final HashMap<RSTile, Integer> tileflags = new HashMap<RSTile, Integer>();

        public WebData(final MethodContext ctx){
            this.ctx = ctx;
        }
        
        public HashMap<RSTile, Integer> getTileFlags(){
            return tileflags;
        }
        
        private boolean isGameLoading(){
            return ctx.client.getLoginIndex() == 12;
        }
        
	public boolean updateAvailable() {
		final RSTile curr_base = ctx.game.getMapBase();
		final int curr_plane = ctx.game.getPlane();
		return ctx.game.isLoggedIn() && !isGameLoading() && ((lastMapBase == null || !lastMapBase.equals(curr_base)) || (lastLevel == -1 || lastLevel != curr_plane));
	}
        
        public void update(){
            update(false);
        }

	public void update(boolean forced) {
            if(forced || updateAvailable())
		try {
			final RSTile currentMapBase = ctx.game.getMapBase();
			final int currentLevel = ctx.game.getPlane();
			lastMapBase = currentMapBase;
			lastLevel = currentLevel;
			final int tileKeys[][] = ctx.walking.getCollisionFlags(currentLevel).clone();
			final RSTile collisionOffset = ctx.walking.getCollisionOffset(currentLevel);
			final int xOffset = collisionOffset.getX();
			final int yOffset = collisionOffset.getY();
			final int xBase = currentMapBase.getX(), yBase = currentMapBase.getY();
			for (int queryX = 3; queryX < 102; queryX++) {
				for (int queryY = 3; queryY < 102; queryY++) {
					final RSTile analysisTile = new RSTile(currentMapBase.getX() + queryX, currentMapBase.getY() + queryY, currentLevel);
					final int localX = analysisTile.getX() - xBase, localY = analysisTile.getY() - yBase;
					final int keyIndex_x = localX - xOffset, keyIndex_y = localY - yOffset;
					final int key = tileKeys[keyIndex_x][keyIndex_y];
					synchronized (botCollectionLock) {
						if (!tileflags.containsKey(analysisTile) && (!RSTile.Walkable(key) || RSTile.Questionable(key))) {
							tileflags.put(analysisTile, key);
						} else {
							if (tileflags.containsKey(analysisTile) && tileflags.get(analysisTile) != key) {
								tileflags.remove(analysisTile);
								lastMapBase = null;
								lastLevel = -1;
							}
						}
					}
				}
			}
		} catch (final Exception ignored) {
		}
	}
}