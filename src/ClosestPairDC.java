import ProGAL.geom2d.Circle;
import ProGAL.geom2d.Line;
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
   public static J2DScene scene;

   public static void main(String[] args) {
      ArrayList<Point> points;
      // Runs 10.000 iterations with different points each time. If an iteration returns the wrong closest pair
      // according to the brute force algorithm, the points are printed in the console and ready to be used in
      // debugging.
      for (int i = 0; i < 10000; i++) {

         points = createItemList(100);
         ClosestPairDC cp = new ClosestPairDC(points);
         ArrayList<Point> closest = cp.findClosest();
         ArrayList<Point> p = cp.bruteForce(points);
         double dist = p.get(0).getSquaredDistance(p.get(1));
         if (closest.get(0).getSquaredDistance(closest.get(1)) != dist) {
            System.out.println(Math.sqrt(closest.get(0).getSquaredDistance(closest.get(1))));
            System.out.println(Math.sqrt(dist));
            for (Point pt : points) {
               System.out.println("points.add(new Point(" + pt.x() + ", " + pt.y() + "));");
            }
            break;
         }
         assert (closest.get(0).getSquaredDistance(closest.get(1)) == dist);
      }
      System.out.println("IT WORKS");
      /*
      scene = J2DScene.createJ2DSceneInFrame();
      points = createItemList(10000);
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
      System.out.println(cp.getDist());
      ArrayList<Point> p = cp.bruteForce(points);
      double dist = Math.sqrt(p.get(0).getSquaredDistance(p.get(1)));
      System.out.println(dist);
      scene.addShape(new Circle(p.get(0), 0.04), Color.GREEN, 0, true);
      scene.addShape(new Circle(p.get(1), 0.04), Color.GREEN, 0, true);
      */
   }

   /**
    * Constructor. Sorts the points in x and in y.
    * @param points
    */
   public ClosestPairDC(ArrayList<Point> points) {
      this.points = points;
      this.x = new ArrayList<>(points);
      Collections.sort(x, new sortByX());
      this.y = new ArrayList<>(points);
      Collections.sort(y, new sortByY());
   }

   /**
    * Finds the closest pair of the current points.
    * @return The closest pair in an arraylist.
    */
   public ArrayList<Point> findClosest() {
      this.closest = _findClosest(this.x, this.y);
      this.dist = dist(closest.get(0), closest.get(1));
      return closest;
   }

   /**
    * Recursive helper function for finding the closest pair.
    * @param x ArrayList of points sorted in x.
    * @param y ArrayList of points sorted in y.
    * @return The closest pair of points determined by mergePlanes function.
    */
   private ArrayList<Point> _findClosest(ArrayList<Point> x, ArrayList<Point> y) {
      int n = x.size();
      ArrayList<Point> pLeft, pRight, xL, xR, pL, pR, yL, yR;

      // Base case
      if (n <= 3) {
         return bruteForce(x);
      }
      else {
         yL = new ArrayList<>();
         yR = new ArrayList<>();
         pL = new ArrayList<>(x.subList(0, n / 2 + 1));
         xL = new ArrayList<>(x.subList(0, n / 2 + 1));
         pR = new ArrayList<>(x.subList(n / 2, n));
         xR = new ArrayList<>(x.subList(n / 2, n));
         //scene.addShape(new Line(pL.get(pL.size() - 1), new Point(pL.get(pL.size() - 1).x(), 0)));
         /*
         Generates the sorted Y_l and Y_r arrays using binary search.
          */
         for (int i = 0; i < y.size(); i++) {
            Point p = y.get(i);
            if (Collections.binarySearch(pL, p, new contains()) >= 0) {
               yL.add(p);
            }
            if (Collections.binarySearch(pR, p, new contains()) >= 0) {
               yR.add(p);
            }
         }
         pLeft = _findClosest(xL, yL);
         pRight = _findClosest(xR, yR);
         return mergePlanes(pLeft, pRight, pL, y);
      }
   }

   /**
    * Merges two planes and finds the closest pair.
    * @param left The closest pair of points in the left plane.
    * @param right The closest pair of points in the right plane
    * @param pLeft All the points in the left plane.
    * @param y All the points in both planes sorted by y-coordinate.
    * @return The closest pair in an arraylist.
    */
   private ArrayList<Point> mergePlanes(ArrayList<Point> left, ArrayList<Point> right, ArrayList<Point> pLeft, ArrayList<Point> y) {
      double dLeft = dist(left.get(0), left.get(1));
      double dRight = dist(right.get(0), right.get(1));
      ArrayList<Point> closest = new ArrayList<>();
      double delta;

      /*
      Finds the closest pair of the two planes.
       */
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

      /*
      Finds the mid point of the two planes and generates the Y' array while maintaining the sorted order.
       */
      double mid = pLeft.get(pLeft.size() - 1).x();
      for (Point p : y) {
         double x = p.x();
         if (x > mid - delta && x < mid + delta) {
            yPrime.add(p);
         }
      }
      /*
      Looks for pairs that are closer across the line between the two planes.
       */
      double dist = delta;
      for (int i = 0; i < yPrime.size(); i++) {
         for (int j = i + 1; j < yPrime.size(); j++) {
            Point p1 = yPrime.get(i);
            Point p2 = yPrime.get(j);
            if(Math.abs(p1.y() - p2.y()) > dist ) break;
            double tmpDist = dist(p1, p2);
            if (tmpDist < dist) {
               dist = tmpDist;
               closest.set(0, p1);
               closest.set(1, p2);
            }
         }
      }

      return closest;
   }

   /**
    * Find the closest pair of the input points in a brute force fashion
    * @param points ArrayList<Point> of ProGAL points
    * @return The closest pair in an arraylist
    */
   public ArrayList<Point> bruteForce(ArrayList<Point> points) {
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

   /**
    * Returns the distance of the current closest pair.
    * @return The distance between the closest pair.
    */
   public double getDist() {
      return dist;
   }

   /**
    * Returns the closest pair of the points in an ArrayList.
    * @return ArrayList of the closest pair.
    */
   public ArrayList<Point> getClosest() {
      return closest;
   }

   /**
    * Comparator class used for sorting an array of points in x.
    */
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

   /**
    * Comparator class used for sorting an array of points in y.
    */
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

   /**
    * Comparator class used for comparing points that are sorted in x.
    */
   class contains implements Comparator<Point> {
      @Override
      public int compare(Point p1, Point p2) {
         if (p1.x() == p2.x()) {
            if (p1.y() == p2.y()) {
               return 0;
            } else {
               return p1.y() < p2.y() ? -1 : 1;
            }
         } else {
            return p1.x() < p2.x() ? -1 : 1;
         }
      }
   }

   /**
    * Generates n random points.
    * @param n The number of points.
    * @return ArrayList of the randomly generated points.
    */
   static private ArrayList<Point> createItemList(int n) {
      ArrayList<Point> list = new ArrayList<>();
      int i;
      for(i=0; i<n; i++) {
         list.add(new Point(Math.random()*12 - 6, Math.random()*12- 6));
      }
      return list;
   }

   /**
    * Private function for calculating the distance between two points in the plane.
    * @param p1 First point.
    * @param p2 Second point.
    * @return The distance.
    */
   private double dist(Point p1, Point p2) {
      return Math.sqrt(Math.pow(p2.x() - p1.x(), 2) + Math.pow(p2.y() - p1.y(), 2));
   }
}
