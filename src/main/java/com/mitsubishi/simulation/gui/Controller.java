package com.mitsubishi.simulation.gui;

import com.mitsubishi.simulation.controllers.parsers.OSMBoundingBoxParser;
import com.mitsubishi.simulation.input.matsimtransit.TransitWriter;
import com.mitsubishi.simulation.input.network.NetworkUtils;
import com.mitsubishi.simulation.input.nlni.NLNIRailwayTransitAdapter;
import com.mitsubishi.simulation.input.osm.OSMRelationTransitAdapter;
import com.mitsubishi.simulation.input.population.PersonGenerator;
import com.mitsubishi.simulation.input.population.RandomWithinBoundaryPersonGenerator;
import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitAdapter;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.api.experimental.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.MatsimConfigReader;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.openstreetmap.osmosis.core.Osmosis;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by tiden on 7/27/2015.
 */
public class Controller implements Initializable {
    public TextField publicTransportationDataSource;
    public ChoiceBox publicTransportationDataSourceType;
    public ChoiceBox publicTransportationStartHour;
    public ChoiceBox publicTransportationStartMinute;
    public ChoiceBox publicTransportationEndHour;
    public ChoiceBox publicTransportationEndMinute;
    public TextField publicTransportationEmitInterval;
    public TextField publicTransportationSpeed;
    public ChoiceBox networkDataSourceType;
    public TextField networkDataSource;
    public RadioButton networkDataFilterAllRoads;
    public RadioButton networkDataFilterMainRoads;
    public ChoiceBox populationGenerationLeaveHomeStartHour;
    public ChoiceBox populationGenerationLeaveHomeStartMinute;
    public ChoiceBox populationGenerationLeaveHomeEndHour;
    public ChoiceBox populationGenerationLeaveHomeEndMinute;
    public ChoiceBox populationGenerationLeaveWorkStartHour;
    public ChoiceBox populationGenerationLeaveWorkStartMinute;
    public ChoiceBox populationGenerationLeaveWorkEndHour;
    public ChoiceBox populationGenerationLeaveWorkEndMinute;
    public TextField outputDirectory;
    public TextField populationGenerationNumberOfAgents;

    private FileChooser fileChooser;
    private DirectoryChooser dirChooser;
    private Alert warningDialog;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Put the radio buttons into a group
        ToggleGroup group = new ToggleGroup();
        networkDataFilterAllRoads.setToggleGroup(group);
        networkDataFilterMainRoads.setToggleGroup(group);
        group.selectToggle(networkDataFilterMainRoads);
        // Init a file chooser
        fileChooser = new FileChooser();
        dirChooser = new DirectoryChooser();
        warningDialog = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
    }

    public void onPublicTransportDataSourceButtonAction(ActionEvent actionEvent) {
        File transitData = openFileChooser(actionEvent);
        if (transitData != null) {
            publicTransportationDataSource.setText(transitData.getAbsolutePath());
        }
    }

    public void onNetworkDataSourceButtonAction(ActionEvent actionEvent) {
        File networkData = openFileChooser(actionEvent);
        if (networkData != null) {
            networkDataSource.setText(networkData.getAbsolutePath());
        }
    }

    public void onOutputDirectoryButtonAction(ActionEvent actionEvent) {
        File outputDir = openDirChooser(actionEvent);
        if (outputDir != null) {
            outputDirectory.setText(outputDir.getAbsolutePath());
        }
    }

    public void onGenerateMATSIMInputButtonAction(ActionEvent actionEvent) {
        String outputDir = outputDirectory.getText();
        if ("".equals(outputDir)) {
            showWarningDialog("Please specify an output directory!");
            return;
        } else {
            File outputFile = Paths.get(outputDir).toFile();
            if ((outputFile.exists() && outputFile.isFile())) {
                showWarningDialog("The output directory " + outputDirectory.getText() + " is not a valid path");
                return;
            }
            if (!outputFile.exists()) {
                if (!outputFile.mkdirs()) {
                    showWarningDialog("The output directory " + outputDirectory.getText() + " is not a valid path");
                    return;
                }
            }
        }
        String ptSrc = publicTransportationDataSource.getText();
        if ("".equals(ptSrc) || !Paths.get(ptSrc).toFile().exists()) {
            showWarningDialog("The public transportation input data source " + ptSrc + " is not a valid path");
            return;
        }
        String nSrc = networkDataSource.getText();
        if ("".equals(nSrc) || !Paths.get(nSrc).toFile().exists()) {
            showWarningDialog("The network input data source " + nSrc + " is not a valid path");
            return;
        }
        generateInputFiles();
    }

    public void onStartSimulationButtonAction(ActionEvent actionEvent) {
        String configPath = outputDirectory.getText() + File.separator + "config.xml";
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            // Warn the user to first generate the inputs
//            new Alert(Alert.AlertType.WARNING, "Please generate the inputs first!", ButtonType.OK).showAndWait();
            showWarningDialog("Please generate the inputs first!");
        } else {
            new Controler(ConfigUtils.loadConfig(configPath)).run();
        }
    }

    private File openDirChooser(ActionEvent e) {
        Window window = ((Node) e.getSource()).getScene().getWindow();
        return dirChooser.showDialog(window);
    }

    private File openFileChooser(ActionEvent e) {
        Window window = ((Node) e.getSource()).getScene().getWindow();
        return fileChooser.showOpenDialog(window);
    }

    private void generateInputFiles() {
        String publicPTSourceType = publicTransportationDataSourceType.getValue().toString();
        String publicPTSource = publicTransportationDataSource.getText();
        String networkSourceType = networkDataSourceType.getValue().toString();
        String networkSource = networkDataSource.getText();
        String outputDir = outputDirectory.getText();

        // config
        Config config = ConfigUtils.createConfig();
        new MatsimConfigReader(config).parse(getClass().getResource("/config.xml"));
        config.getModule("controler").addParam("outputDirectory", outputDir + File.separator + "simulationOutput");
        config.getModule("network").addParam("inputNetworkFile", outputDir + File.separator + "network.xml");
        config.getModule("plans").addParam("inputPlansFile", outputDir + File.separator + "population.xml");
        config.getModule("transit").addParam("transitScheduleFile", outputDir + File.separator + "transitSchedule.xml");
        config.getModule("transit").addParam("vehiclesFile", outputDir + File.separator + "transitVehicles.xml");

        // write config
        new ConfigWriter(config).write(outputDir + File.separator + "config.xml");

        // create a scenario
        Scenario scenario = ScenarioUtils.createScenario(config);

        // convert the network
        // TODO there might be other network data sources
        List<String> osmArgs = new ArrayList<>();
        String wayTempOsm = outputDir + File.separator + "wayTemp.osm";
        osmArgs.add("--rx");
        osmArgs.add("file=" + networkSource);
        if (networkDataFilterMainRoads.isSelected()) {
            osmArgs.add("--tf");
            osmArgs.add("accept-ways");
            osmArgs.add("highway=motorway,motorway_link,trunk,trunk_link,primary,primary_link");
        }
        osmArgs.add("--tf");
        osmArgs.add("reject-relations");
        osmArgs.add("--used-node");
        osmArgs.add("--wx");
        osmArgs.add("file=" + wayTempOsm);
        Osmosis.run(osmArgs.toArray(new String[osmArgs.size()]));
        NetworkUtils.convertOSMToNetwork(
                scenario.getNetwork(),
                wayTempOsm,
                TransformationFactory.WGS84,
                TransformationFactory.CH1903_LV03,
                true
        );

        // set the transit adapter
        TransitAdapter transitAdapter;
        QuadTree.Rect boundary = new OSMBoundingBoxParser(networkSource).getBoundingBox();
        if ("NLNI".equals(publicPTSourceType)) {
            transitAdapter = new NLNIRailwayTransitAdapter(publicPTSource, scenario.getNetwork(), boundary);
        } else {
            transitAdapter = new OSMRelationTransitAdapter(publicPTSource, scenario.getNetwork());
        }
        List<Transit> transits = transitAdapter.getTransits();

        // set the speed of transits
        for (Transit transit : transits) {
            transit.setSpeed(getTransitSpeed());
        }

        // write the transit schedule file and the transit vehicle file
        int transitEmitInterval = getTransitEmitInterval();
        int transitDepEarliest = getTransitDepEarliest();
        int transitDepLatest = getTransitDepLatest();
        TransitWriter transitWriter = new TransitWriter(outputDir, transitAdapter.getTransits(),
                transitDepEarliest, transitDepLatest, transitEmitInterval);
        transitWriter.writeTransitSchedule();
        transitWriter.writeTransitVehicles();

        // write the network
        new NetworkWriter(scenario.getNetwork()).write(outputDir + File.separator + "network.xml");

        // generate the population
        int personDepHomeEarliest = getPopDepHomeEarliest();
        int personDepHomeLatest = getPopDepHomeLatest();
        int personDepWorkEarliest = getPopDepWorkEarliest();
        int personDepWorkLatest = getPopDepWorkLatest();
        PersonGenerator generator = new RandomWithinBoundaryPersonGenerator(scenario, boundary,
                Integer.valueOf(populationGenerationNumberOfAgents.getText()),
                        personDepHomeEarliest, personDepHomeLatest, personDepWorkEarliest, personDepWorkLatest);

        for (Person person : generator.getPersons()) {
            scenario.getPopulation().addPerson(person);
        }

        // write the population
        new PopulationWriter(scenario.getPopulation(), scenario.getNetwork())
                .write(outputDir + File.separator + "population.xml");
    }

    private void showWarningDialog(String warning) {
        warningDialog.setTitle("Warning");
        warningDialog.setContentText(warning);
        warningDialog.showAndWait();
    }

    private int getTransitEmitInterval() {
        return Integer.valueOf(publicTransportationEmitInterval.getText()) * 60;
    }

    private double getTransitSpeed() {
        return Double.valueOf(publicTransportationSpeed.getText());
    }

    private int getTransitDepEarliest() {
        return Integer.valueOf(publicTransportationStartHour.getValue().toString()) * 3600 +
                Integer.valueOf(publicTransportationStartMinute.getValue().toString()) * 60;
    }

    private int getTransitDepLatest() {
        return Integer.valueOf(publicTransportationEndHour.getValue().toString()) * 3600 +
                Integer.valueOf(publicTransportationEndMinute.getValue().toString()) * 60;
    }

    private int getPopDepHomeEarliest() {
        return Integer.valueOf(populationGenerationLeaveHomeStartHour.getValue().toString()) * 3600 +
                Integer.valueOf(populationGenerationLeaveHomeStartMinute.getValue().toString()) * 60;
    }

    private int getPopDepHomeLatest() {
        return Integer.valueOf(populationGenerationLeaveHomeEndHour.getValue().toString()) * 3600 +
                Integer.valueOf(populationGenerationLeaveHomeEndMinute.getValue().toString()) * 60;
    }

    private int getPopDepWorkEarliest() {
        return Integer.valueOf(populationGenerationLeaveWorkStartHour.getValue().toString()) * 3600 +
                Integer.valueOf(populationGenerationLeaveWorkStartMinute.getValue().toString()) * 60;
    }

    private int getPopDepWorkLatest() {
        return Integer.valueOf(populationGenerationLeaveWorkEndHour.getValue().toString()) * 3600 +
                Integer.valueOf(populationGenerationLeaveWorkEndMinute.getValue().toString()) * 60;
    }
}
