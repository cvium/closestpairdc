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
      points.add(new Point(2, 7));
      points.add(new Point(4, 13));
      points.add(new Point(5, 7));
      points.add(new Point(10, 5));
      points.add(new Point(13, 9));
      points.add(new Point(15, 5));
      points.add(new Point(17, 7));
      points.add(new Point(19, 10));
      points.add(new Point(22, 7));
      points.add(new Point(25, 10));
      points.add(new Point(29, 14));
      points.add(new Point(30, 2));
      //points = createItemList(50000);
      ClosestPairDC cp = new ClosestPairDC(points);

      for (Point point : points) {
         scene.addShape(new Circle(point, 0.4), Color.BLUE, 0, true);
      }
      long start = System.currentTimeMillis();
      ArrayList<Point> closest = cp.findClosest();
      scene.addShape(new Circle(closest.get(0), 0.4), Color.RED, 0, true);
      scene.addShape(new Circle(closest.get(1), 0.4), Color.RED, 0, true);
      long end = System.currentTimeMillis();

      System.out.println(end - start);
      System.out.println(cp.getDist());
   }

   public ClosestPairDC(ArrayList<Point> points) {
      this.points = points;
      this.x = new ArrayList<>(points);
      Collections.sort(x, new sortByX());
      this.y = new ArrayList<>(points);
      Collections.sort(y, new sortByY());
   }

   public ArrayList<Point> findClosest() {
      this.closest = _findClosest(this.x, this.y);
      this.dist = dist(closest.get(0), closest.get(1));
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
         return mergePlanes(pLeft, pRight, pL, pR);
      }
   }

   private ArrayList<Point> mergePlanes(ArrayList<Point> left, ArrayList<Point> right, ArrayList<Point> pLeft, ArrayList<Point> pRight) {
      double dLeft = dist(left.get(0), left.get(1));
      double dRight = dist(right.get(0), right.get(1));
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
/*      for (int i = 0; i < points.size(); i++) {
         Point p = points.get(i);
         if (left.contains(p) || right.contains(p)) {
            yPrime.add(p);
         }
      }*/
      Point mid = pLeft.get(pLeft.size() - 1);
      double midX = mid.x();
      for (int i = 0; i < this.y.size(); i++) {
         Point p = y.get(i);
         if (p.x() < midX + delta) {
            yPrime.add(p);
         }
      }
      /*for (int i = pLeft.size() - 1; i > 0; i--) {
         for (int j = 0; j < pRight.size(); j++) {
            Point pL = pLeft.get(i);
            Point pR = pRight.get(j);
            double xDist = Math.abs(pL.x() - pR.x());
            double yDist = Math.abs(pL.y() - pR.y());
            if (xDist < delta && yDist < delta) {
               yPrime.add(pRight.get(j));
            }
         }
         if (Math.abs(pLeft.get(i).x() - pRight.get(0).x()) < delta) {
            yPrime.add(pLeft.get(i));
         }
      }*/
      double dist = delta;
      for (int i = 0; i < yPrime.size(); i++) {
         for (int j = i + 1; j < yPrime.size(); j++) {
            double tmpDist = dist(yPrime.get(i), yPrime.get(j));
            if (tmpDist < dist) {
               dist = tmpDist;
               closest.set(0, yPrime.get(i));
               closest.set(1, yPrime.get(j));
            }
            if (j - i == 7) {
               break;
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
            double dist1 = dist(points.get(i), points.get(j));
            double dist2 = dist(closest.get(0), closest.get(1));
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

   private double dist(Point p1, Point p2) {
      return Math.sqrt(Math.pow(p2.x() - p1.x(), 2) + Math.pow(p2.y() - p1.y(), 2));
   }
}
