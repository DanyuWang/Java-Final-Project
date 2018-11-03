
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import javafx.collections.*;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

@SuppressWarnings("unchecked")
/**
 * Description about Main class
 * @author Wang Danyu
 *
 */
public class Main extends Application {
    /**
     *These are data structure to store all earthquakes, search result, different magnitude, different regions<br>
     *     and plate.
     */
    private static ObservableList<EQ> eq;
    private static ObservableList<EQ> result;
    private static HashMap<Double,Integer> magCount = new HashMap<>();
    private static ArrayList<String> regionList = new ArrayList<>();
    private static HashMap<String,String> plate = new HashMap<>();
    /**
     * These are controllers to show data or has some events.
     */
    private static ChoiceBox<String> choiceBox1;
    private static ChoiceBox<String> choiceBox2;
    private static Label label1 = new Label("Plate1");
    private static Label label2 = new Label("Plate2");
    private static Label regionLabel = new Label("Region");
    private static DatePicker start;
    private static DatePicker end;
    private static ComboBox<String> regionBox = new ComboBox<>();
    private static CheckBox checkBox = new CheckBox("Choose using area to search");
    private static Button button = new Button("Search");
    private static Button update = new Button("Update");
    private static Slider mag = new Slider(1,9,1);
    private static TableView<EQ> tableView;
    private static Text text = new Text();
    /**
     * These are charts segments to form bar charts and pie chart
     */
    private static PieChart pie = new PieChart();
    private static CategoryAxis xAxis = new CategoryAxis();
    private static NumberAxis yAxis = new NumberAxis();
    private static BarChart<String,Number> chart =
            new BarChart<>(xAxis, yAxis);
    private static XYChart.Series<String,Number> seriesChart1 =
            new XYChart.Series<>();
    private static String [] xAxisContent = new String[]{" ","1-2","2-3","3-4","4-5","5-6",">=6"};
    private static String [] pieCate = new String[]{"North Hemisphere","South Hemisphere"};
    private static int[] distribution = new int[2];
    private static ObservableList<XYChart.Data<String,Number>> data = FXCollections.observableArrayList();
    private static ObservableList<PieChart.Data> dataOfDis = FXCollections.observableArrayList();
    /**
     * Some segments to form world map using mercator projection
     */
    private static int picWidth;
    private static int picHeight;
    private static WorldMap worldMap;
    private static StackPane Mercator = new StackPane();

    /**
     *
     * @param args command line
     * @throws SQLException some problem in sql
     * @throws ClassNotFoundException can not find class
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        launch(args);
    }

    @Override
    /**
     * @param primaryStage
     * @throws Exception
     * @version 2.0
     */
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Earthquake Information");
        tableView = setTabView();
        tableView.setItems(getEarthquake());//默认填充表中全部内容
        plate = makePair();

        chart.setTitle("Count on Magnitude");
        chart.setPadding(new Insets(30,150,30,150));
        xAxis.setLabel("Magnitude");
        yAxis.setLabel("Count");
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        seriesChart1.setName("Total count");
        chart.getData().add(seriesChart1);
        for ( int i = 1; i < xAxisContent.length; i ++){
            data.add(i-1,new XYChart.Data<>(xAxisContent[i],magCount.get(i*1.0)));
        }
        seriesChart1.setData(data);

        pie.setTitle("Distribution");
        pie.setPadding(new Insets(30,150,30,150));
        for ( int i = 0; i < 2; i ++) {
            dataOfDis.add(i, new PieChart.Data(pieCate[i], distribution[i]));
        }
        pie.setData(dataOfDis);

        VBox inTab = new VBox();
        TabPane tabPane = new TabPane();
        tabPane.setId("pane");
        //tab1
        Tab tab1 = new Tab();
        tab1.setText("All information");
        tab1.setContent(tableView);
        tab1.setClosable(false);
        tabPane.getTabs().add(tab1);
        //tab2
        Tab tab2 = new Tab();
        tab2.setText("BarChart");
        tab2.setContent(chart);
        tab2.setClosable(false);
        tabPane.getTabs().add(tab2);
        //tab3
        Tab tab3 = new Tab();
        tab3.setText("PieChart");
        tab3.setClosable(false);
        tab3.setContent(pie);
        tabPane.getTabs().add(tab3);
        //tab4
        Tab tab4 = new Tab();
        tab4.setText("WordMap");
        tab4.setClosable(false);

        URL url = this.getClass()
                .getClassLoader()
                .getResource("Mercator.jpg");
        if ( url != null){
            Image image = new Image(url.toString());
            picWidth = (int) image.getWidth();
            picHeight = (int) image.getHeight();
            worldMap = new WorldMap(image,180);
            ImageView iv = new ImageView(image);
            Mercator.getChildren().add(iv);
        }
        refreshMap(eq);
        inTab.getChildren().add(Mercator);
        tab4.setContent(inTab);
        tabPane.getTabs().add(tab4);


        VBox big = new VBox();
        button.setOnAction(event -> {
            if (!checkBox.isSelected()) {
                result = search();
                text.setText("There are total " + String.valueOf(result.size()) + " earthquakes found.");
                tableView.setItems(result);
                refreshChart(result);
                refreshMap(result);
            }else{
                result = resultFromPlate();
                text.setText("There are total " + String.valueOf(result.size()) + " earthquakes found.");
                tableView.setItems(result);
                refreshChart(result);
                refreshMap(result);
            }
        });

        update.setOnAction(event -> {
            try {
                int n = Crawler.webcrawler();
                eq = getEarthquake();
                tableView.setItems(eq);
                text.setText("There are total "+ String.valueOf(eq.size()) +" earthquakes information.");
                refreshChart(eq);
                refreshMap(eq);
                display(n);

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        });
        HBox date = datePicker();
        big.getChildren().addAll(date,mag(),region(), showButton(), tabPane);

        Scene scene = new Scene(big);
        primaryStage.setScene(scene);

        scene.getStylesheets().add(getClass().getResource("styleSheet.css").toExternalForm());
        primaryStage.show();

    }

    /**
     *Form a table view.
     * @return return TableView which is filled with EQ objects
     */
    private static TableView<EQ>  setTabView(){
        //id Column
        TableColumn<EQ,String> idColumn = new TableColumn<>("ID");
        idColumn.setMinWidth(80);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        //date Column
        TableColumn<EQ,String> dateColumn = new TableColumn<>("UTC_Date");
        dateColumn.setMinWidth(200);
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        //latitude Column
        TableColumn<EQ,String> latitudeColumn = new TableColumn<>("Latitude");
        latitudeColumn.setMinWidth(50);
        latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        //longitude Column
        TableColumn<EQ,String> longitudeColumn = new TableColumn<>("Longitude");
        longitudeColumn.setMinWidth(50);
        longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        //depth Column
        TableColumn<EQ,String> depthColumn = new TableColumn<>("Depth");
        depthColumn.setMinWidth(30);
        depthColumn.setCellValueFactory(new PropertyValueFactory<>("depth"));
        //magnitude Column
        TableColumn<EQ,String> magnitudeColumn = new TableColumn<>("Magnitude");
        magnitudeColumn.setMinWidth(20);
        magnitudeColumn.setCellValueFactory(new PropertyValueFactory<>("magnitude"));
        //region Column
        TableColumn<EQ,String> regionColumn = new TableColumn<>("Region");
        regionColumn.setMinWidth(300);
        regionColumn.setCellValueFactory(new PropertyValueFactory<>("region"));

        TableView<EQ> tableView = new TableView<>();
        tableView.getColumns().addAll(idColumn,dateColumn,latitudeColumn,longitudeColumn,
                depthColumn,magnitudeColumn,regionColumn);
        return tableView;

    }

    /**
     *Form the first line in window.
     * @return HBox, which contains labels and dates picker which has ability to control the end date before <br>
     *     start date unable to be choose
     */
    private HBox datePicker(){
        HBox hBox = new HBox();
        hBox.setSpacing(30);//控件间距离
        start = new DatePicker();
        end = new DatePicker();
        start.setValue(LocalDate.now());
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item.isBefore(
                                        start.getValue().plusDays(1))
                                        ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };
                    }
                };
        end.setDayCellFactory(dayCellFactory);
        end.setValue(start.getValue().plusDays(1));
        Label label1 = new Label("Start Date:");
        Label label2 = new Label("End Date:");
        hBox.getChildren().addAll(label1,start,label2,end);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(20,20,10,20));

        return hBox;

    }

    /**
     *Form second line
     * @return HBox which contains a slider that measures magnitude and a checkBox to choose a way to search quakes
     */
    private HBox mag(){
        HBox hBox = new HBox();

        HBox bigger = new HBox();
        bigger.setSpacing(60);
        hBox.setSpacing(30);
        Label label = new Label("Magnitude  >= ");
        checkBox = new CheckBox("Choose only using area to search");
        checkBox.setOnAction(event -> {
            if ( !checkBox.isSelected()){
                label1.setVisible(false);
                label2.setVisible(false);
                choiceBox1.setVisible(false);
                choiceBox2.setVisible(false);
                regionBox.setVisible(true);
                regionLabel.setVisible(true);
            }else{
                label1.setVisible(true);
                label2.setVisible(true);
                choiceBox1.setVisible(true);
                choiceBox2.setVisible(true);
                regionBox.setVisible(false);
                regionLabel.setVisible(false);

            }
        });

        mag.setValue(1); // Select all
        mag.setShowTickLabels(true);
        mag.setShowTickMarks(true);
        mag.setMajorTickUnit(1);
        mag.setMinorTickCount(1);
        mag.setBlockIncrement(0.5f);
        hBox.getChildren().addAll(label,mag);
        bigger.getChildren().addAll(hBox,checkBox);
        hBox.setAlignment(Pos.TOP_LEFT);
        bigger.setAlignment(Pos.TOP_LEFT);
        bigger.setPadding(new Insets(20,20,10,20));

        return bigger;
    }

    /**
     *
     * @return HBox which contains a comboBox to choose region<br>
     *     Users can type on it and it can automatically list what you want according to what you type
     */
    private HBox region(){
        HBox hBox = new HBox();
        HBox hBox1 = new HBox();
        hBox.setSpacing(30);

        for ( String re: regionList){
            regionBox.getItems().add(re);
        }
        regionBox.setEditable(true);
        regionBox.setMaxWidth(220);
        regionBox.getSelectionModel().select("");
        regionBox.setPromptText("All the World");
        new AutoCompleteComboBoxListener<>(regionBox);

        choiceBox1 = new ChoiceBox();
        choiceBox1.getItems().addAll("African","Antarctic","Arabian","Australian","Cocos", "Caribbean",
                "Eurasian", "Filipino", "Indian","Juan de Fuca","Nazca","North-American","Pacific","Scotia","South-American");

        choiceBox2 = new ChoiceBox<>();
        choiceBox2.getItems().addAll("African","Antarctic","Arabian","Australian","Cocos", "Caribbean",
                "Eurasian", "Filipino", "Indian","Juan de Fuca","Nazca","North-American","Pacific","Scotia","South-American");
        choiceBox2.setVisible(false);
        choiceBox1.setVisible(false);
        label1 = new Label("Plate1");
        label2 = new Label("Plate2");
        label1.setVisible(false);
        label2.setVisible(false);
        hBox1.setAlignment(Pos.CENTER);
        hBox1.setSpacing(20);
        hBox1.getChildren().addAll(label1,choiceBox1,label2,choiceBox2);
        hBox.setSpacing(30);
        hBox.getChildren().addAll(regionLabel, regionBox,hBox1);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(20,20,10,20));

        return hBox;
    }

    /**
     *
     * @return HBox which contains a button to search quakes <br>
     *     a update button to add new information in the sqlite file<br>
     *     and a text to show the number of quakes
     */
    private HBox showButton(){
        HBox hBox = new HBox();
        hBox.setSpacing(30);
        hBox.getChildren().addAll(button,update,text);
        button.setId("search_button");
        update.setId("update_button");
        text.setText("There are total "+ String.valueOf(eq.size()) +" earthquakes information.");
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(20,20,10,20));

        return hBox;
    }


    /**
     *
     * @return ObservableList that is filtered by requirement above using "if" clause
     */
    private ObservableList<EQ> search(){
        ObservableList<EQ> result = FXCollections.observableArrayList();
        String date;
        String beginTime = start.getValue().toString();
        String endTime = end.getValue().toString();
        for ( EQ eachEQ: eq){
            date = eachEQ.getDate().substring(0,10);
            if ( beginTime.compareTo(date) <= 0 && date.compareTo(endTime) <= 0){
                if ( eachEQ.getMagnitude() >= mag.getValue()) {
                    if ( !regionBox.getValue().equals("")) {
                        if ( regionBox.getValue().equals(eachEQ.getRegion())) {
                            result.add(eachEQ);
                        }
                    }else{
                        result.add(eachEQ);
                    }
                }
            }

        }
//        System.out.println(result.size());
        return result;
    }

    /**
     * Update information in both charts.
     * @param eq This the result of getting data from database
     */
    private void refreshChart(ObservableList<EQ> eq){
        HashMap<Double,Integer> resultMag = new HashMap<>();
        int[] resultDis = new int[2];
        int count;
        for ( EQ eq2 : eq) {
            Double value = Math.floor(eq2.getMagnitude());
            if (resultMag.containsKey(value)) {
                count = resultMag.get(value) + 1;
            } else {
                count = 1;
            }
            resultMag.put(value, count);
            if ( eq2.getLatitude() > 0){
                resultDis[0] ++;
            }else{
                resultDis[1] ++;
            }
        }
        for ( int i = 1; i < xAxisContent.length; i ++){
            if ( resultMag.get(i*1.0) != null) {
                data.set(i - 1, new XYChart.Data<>(xAxisContent[i], resultMag.get(i * 1.0)));
            }else data.set(i - 1, new XYChart.Data<>(xAxisContent[i], 0));
        }
        for ( int i = 0; i < 2; i ++) {
            dataOfDis.set(i, new PieChart.Data(pieCate[i], resultDis[i]));
        }
        seriesChart1.setData(data);
        pie.setData(dataOfDis);
    }

    /**
     *
     * @param earthquakes
     * To change content of map
     */
    private void refreshMap(ObservableList<EQ> earthquakes){
        int diameter;
        int[] xy;
        ObservableList<Node> paneChild;

        paneChild = Mercator.getChildren();
        //remove used canvas
        if (paneChild.size() > 1) {
            paneChild.remove(1, paneChild.size());
        }
        Canvas canvas = new Canvas(picWidth, picHeight);
        GraphicsContext context2D = canvas.getGraphicsContext2D();
        context2D.setStroke(Color.RED);
        context2D.setLineWidth(2);
        for( EQ q: earthquakes){
            diameter = (int)(4 * q.getMagnitude());
            xy = worldMap.imgxy(q.getLatitude(), q.getLongitude());
            context2D.strokeOval(xy[0] - diameter / 2, xy[1] - diameter / 2,
                    diameter, diameter);
        }
        Mercator.getChildren().add(canvas);

    }

    /**
     *
     * @return ObservableList that contains all information in the sqlite file
     */
    private ObservableList<EQ> getEarthquake() {
        try {
            String fileName = filePos();

            eq = FXCollections.observableArrayList();
            SqlCon sc = new SqlCon(fileName);
            sc.connectDB();
            ResultSet rs = sc.readFromDB();

            int count;
            try {
                while (rs.next()) {
                    IntegerProperty id = new SimpleIntegerProperty(this,"id",Integer.valueOf(rs.getString(1)));
                    //  System.out.println(id.intValue());
                    StringProperty date = new SimpleStringProperty(this,"date",rs.getString(2));
                    //    System.out.println(date);
                    DoubleProperty latitude = new SimpleDoubleProperty(this,"latitude",Double.valueOf(rs.getString(3)));
                    //  System.out.println(latitude);
                    DoubleProperty longitude = new SimpleDoubleProperty(this,"longitude",Double.valueOf(rs.getString(4)));
                    //     System.out.println(id.intValue());
                    IntegerProperty depth = new SimpleIntegerProperty(this,"depth",Integer.valueOf(rs.getString(5)));
                    DoubleProperty magnitude = new SimpleDoubleProperty(this,"magnitude",Double.valueOf(rs.getString(6)));
                    Double value = Math.floor(Double.parseDouble(rs.getString(6)));

                    if (magCount.containsKey(value)) {
                        count = magCount.get(value) + 1;
                    } else {
                        count = 1;
                    }
                    magCount.put(value, count);
                    if (Double.valueOf(rs.getString(3)) <= 0.0) {
                        distribution[1]++;
                    } else {
                        distribution[0]++;
                    }

                    String eqRegion = rs.getString(7);
                    StringProperty region = new SimpleStringProperty(this, "region", rs.getString(7));
                    if (!regionList.contains(eqRegion)) {
                        regionList.add(eqRegion);
                    }

                    StringProperty area_id = new SimpleStringProperty(this,"area_id",rs.getString(8));
                    EQ e1 = new EQ(id, date, latitude, longitude, depth, magnitude, region,area_id);

                    eq.add(e1);
    //System.out.println(e1.getId()+" "+e1.getDate()+" "+e1.getLatitude()+" "+e1.getLongitude()+" "+e1.getDepth()+" "+e1.getMagnitude()+e1.getRegion());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Collections.sort(regionList);
            return eq;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }

    }

    /**
     *First, find the abbreviation of plate and find the area_id, then according area_id we find quakes.
     * @return A ArrayList that store the number of two plate intersection id helping to search quakes
     * @throws SQLException some problems in sql
     */
    private static ArrayList<String> query() throws SQLException {
        String plate1 = plate.get(choiceBox1.getValue());
        String plate2 = plate.get(choiceBox2.getValue());
        ArrayList<String> temp = new ArrayList<>();
    SqlCon sc = new SqlCon("/Users/wangholly/IdeaProjects/Pro1/src/earthquakes-1.sqlite");
    sc.connectDB();
    PreparedStatement stmt;
    ResultSet rs;
    stmt = sc.con.prepareStatement("SELECT id FROM plate_areas WHERE plate1='"+plate1+"'AND plate2='"+plate2+"'");
    rs = stmt.executeQuery();
    while(rs.next()){
        temp.add(rs.getString(1));
    }
    return temp;
}

    /**
     *when query from second choice.
     * @return ObservableList whose content is searched by two plates
     */
    private static ObservableList<EQ> resultFromPlate() {
        try {
            ArrayList<String> idList = query();
            //System.out.println(idList.size());
            result = FXCollections.observableArrayList();
                for( EQ e: eq){
                   // System.out.println(e.getArea_id());
                    if( idList.contains(e.getArea_id())){
                        result.add(e);
                    }
                }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *Get sqlite file position from properties file.
     * @return The properties filename
     */
    public static String filePos(){
        Properties a = new Properties();
        Properties b = new Properties(a);
        try (BufferedReader conf = new BufferedReader(new FileReader("src/quakeInfo.properties"))) {
            b.load(conf);
            return b.getProperty("file_position");
        } catch (IOException e) {
            System.err.println("Warning: using default preferences");
            System.out.println(b.getProperty("stop_words_dir"));
            return null;
        }
    }

    /**
     *
     * @param n This is the number of new information.
     *          show the update information
     */
    private static void display(int n){
        Stage smallWindow = new Stage();
        smallWindow.initModality(Modality.APPLICATION_MODAL);
        smallWindow.setTitle("Update Information");
        smallWindow.setMinWidth(250);
        VBox vBox = new VBox();
        Text text = new Text("Database has been Updated!");
        Text text1 = new Text(n+" information added!");
        vBox.getChildren().addAll(text,text1);
        vBox.setPadding(new Insets(20,20,20,20));
        vBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vBox);
        smallWindow.setScene(scene);
        smallWindow.showAndWait();
    }

    /**
     * @return To connect complete area name(key) and abbreviation(value) together
     */
    private static HashMap<String, String> makePair(){
        plate.put("Scotia","SC");
        plate.put("Antarctic","AT");
        plate.put("African","AF");
        plate.put("Arabian","AR");
        plate.put("Cocos","CO");
        plate.put("Eurasian","EA");
        plate.put("Indian","IN");
        plate.put("Australian","AU");
        plate.put("Juan de Fuca","JF");
        plate.put("Caribbean","CA");
        plate.put("North-American","NA");
        plate.put("Nazca","NZ");
        plate.put("Pacific","PA");
        plate.put("Filipino","FI");
        plate.put("South-American","SA");

        return plate;
    }
}