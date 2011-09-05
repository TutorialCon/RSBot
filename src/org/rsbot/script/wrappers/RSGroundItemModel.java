package org.rsbot.script.wrappers;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import org.rsbot.client.Model;
import org.rsbot.client.RSAnimable;
import org.rsbot.script.methods.MethodContext;

/**
 */
class RSGroundItemModel extends RSModel {
	private final RSAnimable animable;
        private final RSGroundItem item;

	RSGroundItemModel(final MethodContext ctx, final Model model, final RSAnimable animable, final RSGroundItem item) {
		super(ctx, model);
                this.item = item;
		this.animable = animable;
	}

	@Override
	protected void update() {

	}

	@Override
	protected int getLocalX() {
		return animable.getX();
	}

	@Override
	protected int getLocalY() {
		return animable.getY();
	}
        
        @Override
        public Point getPoint() {
            Point modelPoint = super.getPoint();
            modelPoint.y += getYAdjustment();
            return modelPoint;
        }
        
        /**
         * @return the difference between the point at the center of the tile
         * and the real height.
         */
        public int getYAdjustment() {
            Point tilePoint = methods.calc.tileToScreen(item.getLocation());
            Point realPoint = methods.calc.tileToScreen(item.getLocation(), item.getHeight());
            return realPoint.y - tilePoint.y;
        }
        
        @Override
        public Polygon[] getTriangles() {
		int[][] points = projectVertices();
                final int adjustment = getYAdjustment();
		ArrayList<Polygon> polys = new ArrayList<Polygon>(numFaces);
		for (int index = 0; index < numFaces; index++) {
			int index1 = indices1[index];
			int index2 = indices2[index];
			int index3 = indices3[index];

			int _xPoints[] = new int[3];
			int _yPoints[] = new int[3];

			_xPoints[0] = points[index1][0];
			_yPoints[0] = points[index1][1] + adjustment;
			_xPoints[1] = points[index2][0];
			_yPoints[1] = points[index2][1] + adjustment;
			_xPoints[2] = points[index3][0];
			_yPoints[2] = points[index3][1] + adjustment;

			if (points[index1][2] + points[index2][2] + points[index3][2] == 3) {
				polys.add(new Polygon(_xPoints, _yPoints, 3));
			}
		}
		return polys.toArray(new Polygon[polys.size()]);
	}
        
        @Override
        public void drawWireFrame(Graphics graphics) {
		int[][] screen = super.projectVertices();
                final int adjustment = getYAdjustment();

		// That was it for the projection part
		for (int index = 0; index < numFaces; index++) {
			int index1 = indices1[index];
			int index2 = indices2[index];
			int index3 = indices3[index];

			int point1X = screen[index1][0];
			int point1Y = screen[index1][1] + adjustment;
			int point2X = screen[index2][0];
			int point2Y = screen[index2][1] + adjustment;
			int point3X = screen[index3][0];
			int point3Y = screen[index3][1] + adjustment;

			if (screen[index1][2] + screen[index2][2] + screen[index3][2] == 3) {
				graphics.drawLine(point1X, point1Y, point2X, point2Y);
				graphics.drawLine(point2X, point2Y, point3X, point3Y);
				graphics.drawLine(point3X, point3Y, point1X, point1Y);
			}
		}
	}
}
