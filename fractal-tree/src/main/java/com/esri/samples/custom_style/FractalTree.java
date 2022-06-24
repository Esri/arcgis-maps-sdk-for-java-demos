package com.esri.samples.display_map;

import com.esri.arcgisruntime.arcgisservices.ArcGISFeatureServiceInfo;
import com.esri.arcgisruntime.arcgisservices.IdInfo;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.*;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.internal.jni.CoreRequest;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.Loadable;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.ogc.kml.*;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.UserCredential;
import com.esri.arcgisruntime.symbology.*;
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.Random;


public class FractalTree extends Application {

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


      // create a map with the standard imagery basemap style
      //ArcGISMap map = new ArcGISMap(SpatialReferences.getWebMercator());
      ArcGISMap map = new ArcGISMap(Basemap.createLightGrayCanvas());


      // create a map view and set the map to it
      mapView = new MapView();
      mapView.setMap(map);

      graphicsOverlay = new GraphicsOverlay();
      mapView.getGraphicsOverlays().add(graphicsOverlay);

      //makePointAtAngle(new Point(0,0), 100, 0);
      lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF00FF00, 5);
      decorationSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 10);

      fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFFFFFFF, null);
      //makeFractalTree(new Point(0,0), 1000000,2, 30);


      //drawSnowflake(4);

      drawArc();


      // add the map view to the stack pane
      stackPane.getChildren().addAll(mapView);
    } catch (Exception e) {
      // on any error, display the stack trace.
      e.printStackTrace();
    }
  }

  private void drawArc() {

    // set up spatial references
    SpatialReference wgs84 = SpatialReferences.getWgs84();
    SpatialReference webMercator = SpatialReferences.getWebMercator();

    // make a point in lat/long (WGS84)
    Point centrePoint = new Point(-2.5,56, wgs84);

    // project it to a mercator projection where the unit of measure is metres
    Point mercatorPoint = (Point) GeometryEngine.project(centrePoint, webMercator);

    // create an arc segment which is 50Kilometers radius, starting at 90 degrees, the arc angle is 45 degrees clockwise
    EllipticArcSegment arcSegment = EllipticArcSegment.createCircularEllipticArc(mercatorPoint, 50000,Math.toRadians(90), Math.toRadians(-45), webMercator);

    // make a part with the segment
    Part part = new Part(webMercator);
    part.add(arcSegment);

    // create the line from the part
    Polyline line = new Polyline(part);

    // add it to a graphic and graphics overlay to allow us to visualise it
    Graphic arcGraphic = new Graphic(line, lineSymbol);
    graphicsOverlay.getGraphics().add(arcGraphic);
  }

  private void drawSnowflake(int depth) {
    Polygon polygon = makeTriangle(new Point(0,0), 1000000, 30);
    Graphic graphic = new Graphic(polygon, fillSymbol);

    graphicsOverlay.getGraphics().add(graphic);
  }

  private Polygon makeTriangle(Point startPosition, double size, double angle) {
    Polygon polygon;

    Point pt2 = makePointAtAngle(startPosition, size, angle);
    Point pt3 = makePointAtAngle(pt2, size, angle + 120);

    PointCollection points = new PointCollection(SpatialReferences.getWebMercator());
    points.add(startPosition);
    points.add(pt2);
    points.add(pt3);

    polygon = new Polygon(points);

    return polygon;


  }

  private void makeFractalTree(Point startPoint, double height, int maxDepth, double branchAngle) {

    drawBranch(startPoint, height, 0, 1, maxDepth);
  }

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