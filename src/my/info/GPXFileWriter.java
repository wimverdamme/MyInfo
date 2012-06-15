package my.info;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GPXFileWriter {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
    
    /**
     * GPX opening tag
     */
    private static final String TAG_GPX = "<gpx"
            + " xmlns=\"http://www.topografix.com/GPX/1/1\""
            + " version=\"1.1\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd \">";
    
    /**
     * Date format for a point timestamp.
     */
    private static final SimpleDateFormat POINT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    /**
     * Writes the GPX file
     * @param trackName Name of the GPX track (metadata)
     * @param cTrackPoints Cursor to track points.
     * @param target Target GPX file
     * @throws IOException 
     */
    public static void writeGpxFile(String trackName, ArrayList<RunningActivity.Point> points, File target) throws IOException {
            FileWriter fw = new FileWriter(target);
            
            fw.write(XML_HEADER + "\n");
            fw.write(TAG_GPX + "\n");
            
            writeTrackPoints(trackName, fw, points);
            
            fw.write("</gpx>");
            
            fw.close();
    }
    
    /**
     * Iterates on track points and write them.
     * @param trackName Name of the track (metadata).
     * @param fw Writer to the target file.
     * @param c Cursor to track points.
     * @throws IOException
     */
    public static void writeTrackPoints(String trackName, FileWriter fw, ArrayList<RunningActivity.Point> points) throws IOException {
            fw.write("\t" + "<trk>");
            fw.write("\t\t" + "<name>" + trackName + "</name>" + "\n");
            
            fw.write("\t\t" + "<trkseg>" + "\n");
            
            for (RunningActivity.Point point : points)
            {
            	StringBuffer out = new StringBuffer();
            	out.append("\t\t\t" + "<trkpt lat=\"" 
                        + point.LatitudeE6/(double)1000000  + "\" "
                        + "lon=\"" + point.LongitudeE6/(double)1000000 + "\">");
            	out.append("<ele>" + point.Altitude + "</ele>");
            	out.append("<time>" + POINT_DATE_FORMATTER.format(new Date(point.Time)) + "</time>");
            	out.append("<speed>" + point.Speed + "</speed>");
            	out.append("</trkpt>" + "\n");
                fw.write(out.toString());
            }
               
            fw.write("\t\t" + "</trkseg>" + "\n");
            fw.write("\t" + "</trk>" + "\n");
    }
    

}

