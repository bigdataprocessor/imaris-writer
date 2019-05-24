package de.embl.cba.imaris;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Binner;
import ij.plugin.Duplicator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public abstract class ImarisUtils {

    public final static String IMAGE = "Image";
    public final static String DATA_SET = "DataSet";
    public final static String DATA = "Data";
    public final static String TIME_INFO = "TimeInfo";
    public final static String CHANNEL = "Channel ";
    public final static String TIME_POINT = "TimePoint ";
    public final static String TIME_POINT_ATTRIBUTE = "TimePoint";
    public final static String HISTOGRAM = "Histogram";
    public final static String IMAGE_SIZE = "ImageSize";
    public final static String IMAGE_BLOCK_SIZE = "ImageBlockSize";
    public final static String RESOLUTION_LEVEL = "ResolutionLevel ";
    public final static String[] XYZ = new String[]{"X","Y","Z"};
    public final static String DATA_SET_INFO = "DataSetInfo";
    public final static String CHANNEL_COLOR = "Color";
    public final static String CHANNEL_NAME = "Name";
    public final static String DEFAULT_COLOR = "1.000 1.000 1.000";
    public final static String RESOLUTION_LEVELS_ATTRIBUTE = "ResolutionLevels";

    public final static int DIRECTORY = 0;
    public final static int FILENAME = 1;
    public final static int GROUP = 2;
    public final static long MIN_VOXELS = 1024 * 1024;


    public static ArrayList< File > getImarisFiles( String directory )
    {

        File dir = new File(directory);
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean accept =
                        ( name.endsWith( ".ims" )
                        && (!name.contains( "meta" )) );
                return accept;
            }
        });

        ArrayList < File > masterFiles = new ArrayList<>();
        for (File imsMasterFile : files)
        {
            masterFiles.add ( imsMasterFile );
        }

        return ( masterFiles );
    }

    public static void createImarisMetaFile( String directory )
    {
        // create imaris meta file
        ArrayList < File > imarisFiles = ImarisUtils.getImarisFiles( directory );
        if ( imarisFiles.size() > 1 )
        {
            //ImarisWriter.writeCombinedHeaderFile( imarisFiles, "meta.ims" );
        }
    }

    public static ImagePlus bin(
            ImagePlus imp,
            int[] binning,
            String binningTitle )
    {

        String title = imp.getTitle();
        Binner binner = new Binner();

        Calibration saveCalibration =
                imp.getCalibration().copy(); // this is due to a bug in the binner

        // TODO: maybe this could be done faster?
        ImagePlus impBinned = binner.shrink(
                imp,
                binning[0], binning[1], binning[2],
                binner.AVERAGE );

        impBinned.setTitle(binningTitle + "_" + title);

        // reset calibration of input image
        // necessary due to a bug in the binner
        imp.setCalibration( saveCalibration );

        return ( impBinned );
    }

    public static int[] delimitedStringToIntegerArray(String s, String delimiter) {

        String[] sA = s.split(delimiter);
        int[] nums = new int[sA.length];
        for (int i = 0; i < nums.length; i++)
        {
            nums[i] = Integer.parseInt(sA[i].trim());
        }

        return nums;
    }

    public static ImagePlus getDataCube( ImagePlus image, int c, int t, int[] binning )
    {
        ImagePlus dataCube = new Duplicator().run( image, c + 1, c + 1, 1, image.getNSlices(), t + 1, t + 1 );

        if ( binning[ 0 ] > 1 || binning[ 1 ] > 1 || binning[ 2 ] > 1 ){
            Binner binner = new Binner();
            dataCube = binner.shrink( dataCube, binning[0], binning[1], binning[2], binner.AVERAGE );
        }
        return dataCube;
    }



}
