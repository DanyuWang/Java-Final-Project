import javafx.scene.image.Image;
@SuppressWarnings("unchecked")
public class WorldMap {
    private double middleLongitude;
    private double pixelWidth;
    private double pixelHeight;
    private double xCoef;
    private double yCoef;

    /**
     * @param background the map picture
     * @param middleLongitude reference longitude
     */
    public WorldMap(Image background, double middleLongitude){
        this.pixelWidth = background.getWidth();
        this.pixelHeight = background.getHeight();
        this.middleLongitude = middleLongitude;
        xCoef = 1.0;
        yCoef = 1.0;
    }

    /**
     *
     * @param lambda  longitude in formula
     * @param phi latitude in formula
     * @return double array to store new position
     */
    private double[] getMercatorXY(double lambda, double phi) {
        double[] coordinates = new double[2];
        coordinates[0] = (pixelWidth / 2.0 / Math.PI) * lambda;
        coordinates[1] = (pixelWidth / 2.0 / Math.PI) * Math.log(Math.tan(Math.PI / 4.0 + (phi / 2.0)));
        return coordinates;
    }

    /**
     *
     * @param latitude relative latitude
     * @param longitude relative longitude
     * @return double array to store relative position
     */
    private double[] getXY(double latitude,
                           double longitude) {
        double new_longitude;

        new_longitude = (longitude - middleLongitude);
        if (new_longitude < -180) {
            new_longitude += 360;
        }
        if (new_longitude > 180) {
            new_longitude -= 360;
        }
        return getMercatorXY(new_longitude * Math.PI / 180.0, latitude * Math.PI / 180.0);
    }

    /**
     *
     * @param latitude  normal latitude
     * @param longitude normal longitude
     * @return int array to store position in map picture
     */
    public int[] imgxy(double latitude,
                       double longitude) {
        double[] xy = getXY(latitude, longitude);
        if (xy == null) {
            return null;
        }
        int[]    imgcoord = new int[2];
        imgcoord[0] = (int)(Math.round(xy[0] * xCoef) + Math.round(pixelWidth/2));
        imgcoord[1] = (int)(Math.round(pixelHeight/2) - Math.round(xy[1] * yCoef));

        return imgcoord;
    }

}
