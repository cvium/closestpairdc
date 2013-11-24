import ProGAL.geom2d.Circle;
import ProGAL.geom2d.Point;
import ProGAL.geom2d.viewer.J2DScene;

import java.awt.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Claus
 * Date: 24-11-13
 * Time: 00:02
 * To change this template use File | Settings | File Templates.
 */
public class ClosestPairDC {
   private ArrayList<Point> points;
   private ArrayList<Point> closest;
   private double dist;
   private ArrayList<Point> x;
   private ArrayList<Point> y;

   public static void main(String[] args) {
      J2DScene scene = J2DScene.createJ2DSceneInFrame();

      ArrayList<Point> points = new ArrayList<>();
      /*points.add(new Point(0, 1));
      points.add(new Point(2, 1));
      points.add(new Point(3, 1));
      points.add(new Point(5, 1));*/
      points = createItemList(50000);
      ClosestPairDC cp = new ClosestPairDC(points);

      for (Point point : points) {
         scene.addShape(new Circle(point, 0.04), Color.BLUE, 0, true);
      }
      long start = System.currentTimeMillis();
      ArrayList<Point> closest = cp.findClosest();
      scene.addShape(new Circle(closest.get(0), 0.04), Color.RED, 0, true);
      scene.addShape(new Circle(closest.get(1), 0.04), Color.RED, 0, true);
      long end = System.currentTimeMillis();

      System.out.println(end - start);
   }

   public ClosestPairDC(ArrayList<Point> points) {
      this.points = points;
      this.x = points;
      Collections.sort(x, new sortByX());
      this.y = points;
      Collections.sort(y, new sortByY());
   }

   public ArrayList<Point> findClosest() {
      this.closest = _findClosest(this.x, this.y);
      this.dist = closest.get(0).getSquaredDistance(closest.get(1));
      return closest;
   }

   private ArrayList<Point> _findClosest(ArrayList<Point> x, ArrayList<Point> y) {
      int n = x.size();
      ArrayList<Point> pLeft, pRight, xL, xR, pL, pR, yL, yR;

      if (n <= 3) {
         return bruteForce(x);
      }
      else {
         yL = new ArrayList<>();
         yR = new ArrayList<>();
         pL = new ArrayList<>(x.subList(0, n / 2));
         xL = new ArrayList<>(x.subList(0, n / 2));
         pR = new ArrayList<>(x.subList(n / 2, n));
         xR = new ArrayList<>(x.subList(n / 2, n));
         for (int i = 0; i < n; i++) {
            if (pL.contains(y.get(i))) {
               yL.add(y.get(i));
            }
            else {
               yR.add(y.get(i));
            }
         }
         pLeft = _findClosest(xL, yL);
         pRight = _findClosest(xR, yR);
         return mergePlanes(pLeft, pRight);
      }
   }

   private ArrayList<Point> mergePlanes(ArrayList<Point> left, ArrayList<Point> right) {
      double dLeft = left.get(0).getSquaredDistance(left.get(1));
      double dRight = right.get(0).getSquaredDistance(right.get(1));
      ArrayList<Point> closest = new ArrayList<>();
      double delta;

      if (dLeft < dRight) {
         delta = dLeft;
         closest.add(left.get(0));
         closest.add(left.get(1));
      } else {
         delta = dRight;
         closest.add(right.get(0));
         closest.add(right.get(1));
      }
      ArrayList<Point> yPrime = new ArrayList<>();
      for (int i = 0; i < points.size(); i++) {
         Point p = points.get(i);
         if (left.contains(p) || right.contains(p)) {
            yPrime.add(p);
         }
      }
      double dist = delta;
      for (int i = 0; i < yPrime.size(); i++) {
         for (int j = i + 1; j < yPrime.size(); j++) {
            double tmpDist = yPrime.get(i).getSquaredDistance(yPrime.get(j));
            if (tmpDist < dist) {
               dist = tmpDist;
               closest.set(0, yPrime.get(i));
               closest.set(1, yPrime.get(j));
            }
         }
      }

      return closest;
   }

   private ArrayList<Point> bruteForce(ArrayList<Point> points) {
      ArrayList<Point> closest = new ArrayList<>();
      closest.add(points.get(0));
      closest.add(points.get(1));
      int size = points.size();
      for(int i=0; i<(size-1); i++) {
         for(int j=(i+1); j<size; j++) {
            double dist1 = points.get(i).getSquaredDistance(points.get(j));
            double dist2 = closest.get(0).getSquaredDistance(closest.get(1));
            if( dist1 < dist2 ) {
               closest.set(0, points.get(i));
               closest.set(1, points.get(j));
            }
         }
      }
      return closest;
   }

   public double getDist() {
      return dist;
   }

   public ArrayList<Point> getClosest() {
      return closest;
   }

   class sortByX implements Comparator<Point> {
      @Override
      public int compare(Point p1, Point p2) {
         if (p1.x() < p2.x()) {
            return -1;
         } else if (p1.x() == p2.x()) {
            return 0;
         } else {
            return 1;
         }
      }
   }
   class sortByY implements Comparator<Point> {
      @Override
      public int compare(Point p1, Point p2) {
         if (p1.y() < p2.y()) {
            return -1;
         } else if (p1.y() == p2.y()) {
            return 0;
         } else {
            return 1;
         }
      }
   }

   static private ArrayList<Point> createItemList(int n) {
      ArrayList<Point> list = new ArrayList<>();
      int i;
      for(i=0; i<n; i++) {
         list.add(new Point(Math.random()*64 - 32, Math.random()*64 - 32));
      }
      return list;
   }
}
