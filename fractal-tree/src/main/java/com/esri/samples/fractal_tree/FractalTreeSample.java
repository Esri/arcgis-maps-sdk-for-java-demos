package com.esri.samples.fractal_tree;

import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import java.util.Random;


public class FractalTreeSample extends Application {

  private MapView mapView;
  private GraphicsOverlay graphicsOverlay;
  private SimpleLineSymbol lineSymbol;
  private SimpleMarkerSymbol decorationSymbol;
  private SimpleFillSymbol fillSymbol;
  private double branchCoefficient = 0.75;

  @Override
  public void start(Stage stage) {

    try {
      // create stack pane and application scene
      StackPane stackPane = new StackPane();
      Scene scene = new Scene(stackPane);

      // set title, size, and add scene to stage
      stage.setTitle("Display Map Sample");
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setScene(scene);
      stage.show();

      // create a map without a basemap
      ArcGISMap map = new ArcGISMap(SpatialReferences.getWebMercator());

      // create a map view and set the map to it
      mapView = new MapView();
      mapView.setMap(map);

      // a graphics overlay for drawing the tree
      graphicsOverlay = new GraphicsOverlay();
      mapView.getGraphicsOverlays().add(graphicsOverlay);

      // symbols for rendering tree parts
      lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF00FF00, 5);
      decorationSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 10);
      fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFFFFFFF, null);

      // tree drawing starts here!
      makeFractalTree(new Point(0,0), 1000000,10, 30);

      // add the map view to the stack pane
      stackPane.getChildren().addAll(mapView);
    } catch (Exception e) {
      // on any error, display the stack trace.
      e.printStackTrace();
    }
  }

  /**
   * Renderers a tree from a given starting point.
   * @param startPoint position of base of tree trunk
   * @param height height of tree trunk
   * @param maxDepth number of recursions in tree structuire
   * @param branchAngle the angle of the tree branches
   */
  private void makeFractalTree(Point startPoint, double height, int maxDepth, double branchAngle) {
    drawBranch(startPoint, height, 0, 1, maxDepth);
  }

  /**
   * Method to draw a tree branch which is called recursively to draw further branches connected to this one
   * @param pt start point of branch
   * @param length length of the branch
   * @param angle the angle of the brance
   * @param depth the current depth of the tree (number of recursions at this point in the tree)
   * @param maxDepth the total depth of the tree (max recursions)
   */
  private void drawBranch (Point pt, double length, double angle, int depth, int maxDepth) {
    // end point of next branch
    Point endPoint = makePointAtAngle(pt, length, angle);

    // make the line
    PointCollection points = new PointCollection(SpatialReferences.getWebMercator());
    points.add(pt);
    points.add(endPoint);

    Polyline line = new Polyline(points);

    // draw the line
    Graphic graphic = new Graphic(line, lineSymbol);
    graphicsOverlay.getGraphics().add(graphic);

    // decoration
    Random random = new Random();
    Color color = Color.rgb(random.nextInt(255),random.nextInt(255),random.nextInt(255));
    int argb = ColorUtil.colorToArgb(color);
    SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, argb, 15);
    Graphic decoration = new Graphic(endPoint, markerSymbol);
    graphicsOverlay.getGraphics().add(decoration);

    // if we can still grow the tree, draw more
    if (depth < maxDepth) {
      // draw left branch
      drawBranch(endPoint, length* branchCoefficient, angle - 45, depth+ 1, maxDepth);

      // draw right branch
      drawBranch(endPoint, length* branchCoefficient, angle + 35, depth+ 1, maxDepth);
    }

  }

  /**
   * Method which creates a point from in input point at a given distance and bearing angle
   *
   * @param startPosition the input point
   * @param length distance of new point from input point
   * @param angle the bearing angle the new point will be from the input point
   * @return Point
   */
  private Point makePointAtAngle(Point startPosition, double length, double angle) {
    Point point;
    double x = (length * Math.sin(Math.toRadians(angle))) + startPosition.getX();
    double y = (length * Math.cos(Math.toRadians(angle))) + startPosition.getY();
    point = new Point(x,y);

    return point;
  }

  /**
   * Stops and releases all resources used in application.
   */
  @Override
  public void stop() {

    if (mapView != null) {
      mapView.dispose();
    }
  }

  /**
   * Opens and runs application.
   *
   * @param args arguments passed to this application
   */
  public static void main(String[] args) {
    Application.launch(args);
  }
}