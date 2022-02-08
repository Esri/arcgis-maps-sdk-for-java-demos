/*
 * Copyright 2021 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.*;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.raster.HillshadeRenderer;
import com.esri.arcgisruntime.raster.Raster;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class ArranLidarProjectSample extends Application {

  private SceneView sceneView;

  @Override
  public void start(Stage stage) {

    try {
      // create stack pane and JavaFX app scene
      StackPane stackPane = new StackPane();
      Scene fxScene = new Scene(stackPane);

      // set title, size, and add JavaFX scene to stage
      stage.setTitle("Arran Lidar Project");
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setScene(fxScene);
      stage.show();

      // create a progress indicator which displays whilst the scene view's draw status is in progress
      var progressIndicator = new ProgressIndicator();

      // create a scene and scene view
      var arcGISScene = new ArcGISScene();
      sceneView = new SceneView();

      // get the GeoTIFFs that contain 1m lidar digital terrain model (data copyright Scottish Government and SEPA (2014)).
      List<String> geoTiffFiles = new ArrayList<>(Arrays.asList(
        new File("./data/arran-lidar-data/NS02_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NS03_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NS04_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NR82_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NR83_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NR84_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NR93_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NR92_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NR94_1M_DTM_PHASE2.tif").getAbsolutePath(),
        new File("./data/arran-lidar-data/NR95_1M_DTM_PHASE2.tif").getAbsolutePath()
      ));

      // create a new hillshade renderer that is representative of sunset conditions early November 2021 over Scotland
      var hillshadeRenderer = new HillshadeRenderer(10, 225, 1);

      // check the array contains 10 GeoTIFFS as expected
      if (geoTiffFiles.size() == 10) {

        // add a done loading listener to the scene, and check that it has loaded successfully
        arcGISScene.addDoneLoadingListener(() -> {
          if (arcGISScene.getLoadStatus() == LoadStatus.LOADED) {

            // loop through the GeoTIFFs
            for (String geoTiffFile : geoTiffFiles) {

              // create a raster from every GeoTIFF
              var raster = new Raster(geoTiffFile);
              // create a raster layer from the raster
              var rasterLayer = new RasterLayer(raster);
              // set a hillshade renderer to the raster layer
              rasterLayer.setRasterRenderer(hillshadeRenderer);
              // add the raster layer to the scene's operational layers
              arcGISScene.getOperationalLayers().add(rasterLayer);

            }

            // create an elevation source from the GeoTIFF (raster) collection
            var rasterElevationSource = new RasterElevationSource(geoTiffFiles);

            // create a surface, get its elevation sources, and add the raster elevation source to the collection
            var surface = new Surface();
            surface.getElevationSources().add(rasterElevationSource);
            // set the surface to the scene
            arcGISScene.setBaseSurface(surface);

            // display an error if the scene failed to load
          } else if (arcGISScene.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
            new Alert(Alert.AlertType.ERROR, "Scene failed to load").show();
            // print the load error stack trace
            arcGISScene.getLoadError().printStackTrace();
          }
        });

      } else {
        new Alert(Alert.AlertType.WARNING, "Missing GeoTIFF data").show();
      }

      sceneView.addDrawStatusChangedListener(e -> progressIndicator.setVisible(e.getDrawStatus() == DrawStatus.IN_PROGRESS));

      // add the scene view to the stack pane
      stackPane.getChildren().addAll(sceneView, progressIndicator);
      // create a new viewpoint and set it to the scene view
      Camera camera = new Camera(55.615, -5.212, 768, 346.0, 70.5, 0.0);
      sceneView.setViewpointCamera(camera);
      // finally set the scene to the scene view to load the scene
      sceneView.setArcGISScene(arcGISScene);

      } catch (Exception e){
        // on any error, display the stack trace.
        e.printStackTrace();
      }


  }

  /**
   * Stops and releases all resources used in application.
   */
  @Override
  public void stop() {

    if (sceneView != null) {
      sceneView.dispose();
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
