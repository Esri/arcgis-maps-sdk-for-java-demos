
/**
 * Copyright 2022 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.esri.samples.custom_style;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.OpenStreetMapLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class CustomStyleSample extends Application {

  private MapView mapView;
  private ArcGISMap map;
  private SymbolStyle symbolStyle;
  private Geodatabase geodatabase;

  public static void main(String[] args) {

    Application.launch(args);
  }

  @Override
  public void start(Stage stage) {

    // set the title and size of the stage and show it
    stage.setTitle("Custom Style App");
    stage.setWidth(800);
    stage.setHeight(700);
    stage.show();

    // create a JavaFX scene with a stack pane as the root node and add it to the scene
    var stackPane = new StackPane();
    var scene = new Scene(stackPane);
    stage.setScene(scene);

    // create a MapView to display the map and add it to the stack pane
    mapView = new MapView();
    stackPane.getChildren().add(mapView);

    // create an ArcGISMap with an Open Street Map Basemap
    var openStreetMapLayer = new OpenStreetMapLayer();
    map = new ArcGISMap();
    var basemap = new Basemap();
    basemap.getBaseLayers().add(openStreetMapLayer);
    map.setBasemap(basemap);

    // load the style and make a renderer
    var renderer = makeRendererFromStyle();

    // add the data from mobile geodatabase using the renderer
    addMobileGDBData(renderer);

    // display the map by setting the map on the map view
    mapView.setMap(map);

    // set an initial viewpoint
    var point = new Point(-302209.076759, 7568136.965982);
    mapView.setViewpointCenterAsync(point, 5000);
  }

  /**
   * Method to load a style file and use the symbols to create a renderer
   *
   * @return renderer using symbols in style file
   */
  private UniqueValueRenderer makeRendererFromStyle() {
    // create the unique value renderer
    var uniqueValueRenderer = new UniqueValueRenderer();
    uniqueValueRenderer.getFieldNames().add("InsectGroup");

    // default symbol
    var simpleMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFFFF0000,10);
    uniqueValueRenderer.setDefaultSymbol(simpleMarkerSymbol);

    // load the style
    symbolStyle = new SymbolStyle("data/Insects.stylx");
    symbolStyle.loadAsync();
    symbolStyle.addDoneLoadingListener(() -> {
      if (symbolStyle.getLoadStatus() == LoadStatus.LOADED) {

        // create a list of all of the potential symbol names that correspond to the InsectGroup attribute
        var symbolNames = new ArrayList<>(Arrays.asList("Beetle", "Fly", "Moth", "Wasp", "Bee"));

        // loop through the symbol names to create unique values for each status within the unique value renderer
        for (String symbolName : symbolNames) {
          ListenableFuture<Symbol> searchResult = symbolStyle.getSymbolAsync(Collections.singletonList(symbolName));
          searchResult.addDoneListener(() -> {
            try {
              var symbol = searchResult.get();

              switch (symbolName) {
                case "Beetle":
                  var uniqueBeetleValue =
                      new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("Beetle"));
                  uniqueValueRenderer.getUniqueValues().add(uniqueBeetleValue);
                  break;
                case "Fly":
                  var uniqueFlyValue =
                      new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("Fly"));
                  uniqueValueRenderer.getUniqueValues().add(uniqueFlyValue);
                  break;
                case "Moth":
                  var uniqueMothValue =
                      new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("Moth"));
                  uniqueValueRenderer.getUniqueValues().add(uniqueMothValue);
                  break;
                case "Wasp":
                  var uniqueWaspValue =
                      new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("Wasp"));
                  uniqueValueRenderer.getUniqueValues().add(uniqueWaspValue);
                  break;
                case "Bee":
                  var uniqueBeeValue =
                      new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("Bee"));
                  uniqueValueRenderer.getUniqueValues().add(uniqueBeeValue);
                  break;
                default:
              }
            } catch (InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
          });
        }
      }
    });
    return uniqueValueRenderer;
  }

  /**
   * Method to load data from a mobile geodatabase and add this to a layer on the map
   *
   * @param renderer the renderer to display the insects
   */
  private void addMobileGDBData(Renderer renderer) {
    geodatabase = new Geodatabase("data/Insect Survey.geodatabase");
    geodatabase.loadAsync();
    geodatabase.addDoneLoadingListener(() -> {
      if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {

        // load the table for the InsectRecords
        var table = geodatabase.getGeodatabaseFeatureTable("InsectRecords");
        table.loadAsync();
        table.addDoneLoadingListener(() -> {
          var featureLayer = new FeatureLayer(table);
          featureLayer.setRenderer(renderer);

          // add the new layer to the map
          map.getOperationalLayers().add(featureLayer);
        });
      }
    });
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
}
