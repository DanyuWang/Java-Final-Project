
import javafx.beans.property.*;

/**
 * @author Wang Danyu
 * Each earthquake has 8 attributes
 * Every column in quakes sqlite file is concluded.
 * We use Property in case listening.
 */
public class EQ {
    private IntegerProperty id = new SimpleIntegerProperty(this,"depth",0);
    private StringProperty date = new SimpleStringProperty(this,"date","");
    private DoubleProperty latitude = new SimpleDoubleProperty(this,"latitude",0.0);
    private DoubleProperty longitude = new SimpleDoubleProperty(this,"latitude",0.0);
    private IntegerProperty depth = new SimpleIntegerProperty(this,"depth",0);
    private DoubleProperty magnitude = new SimpleDoubleProperty(this,"latitude",0.0);
    private StringProperty region = new SimpleStringProperty(this,"region","");
    private StringProperty area_id = new SimpleStringProperty(this,"area_id","");

    public EQ(IntegerProperty id, StringProperty date, DoubleProperty latitude, DoubleProperty longitude,
              IntegerProperty depth, DoubleProperty magnitude, StringProperty region, StringProperty area_id) {
        this.id = id;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
        this.magnitude = magnitude;
        this.region = region;
        this.area_id = area_id;
    }
    public EQ(IntegerProperty id, StringProperty date, DoubleProperty latitude, DoubleProperty longitude,
              IntegerProperty depth, DoubleProperty magnitude, StringProperty region) {
        this.id = id;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
        this.magnitude = magnitude;
        this.region = region;
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public double getLatitude() {
        return latitude.get();
    }

    public DoubleProperty latitudeProperty() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude.set(latitude);
    }

    public double getLongitude() {
        return longitude.get();
    }

    public DoubleProperty longitudeProperty() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude.set(longitude);
    }

    public int getDepth() {
        return depth.get();
    }

    public IntegerProperty depthProperty() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth.set(depth);
    }

    public double getMagnitude() {
        return magnitude.get();
    }

    public DoubleProperty magnitudeProperty() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude.set(magnitude);
    }

    public String getRegion() {
        return region.get();
    }

    public StringProperty regionProperty() {
        return region;
    }

    public void setRegion(String region) {
        this.region.set(region);
    }

    public String getArea_id(){
        return area_id.get();
    }

    public StringProperty area_idProperty(){
        return area_id;
    }

    public void setArea_id(String area_id){
        this.area_id.set(area_id);
    }
}
