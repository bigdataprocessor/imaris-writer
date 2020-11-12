/*-
 * #%L
 * imaris-writer
 * %%
 * Copyright (C) 2018 - 2020 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
    public final static String HISTOGRAM = "Histogram";
    public final static String IMAGE_SIZE = "ImageSize";
    public final static String IMAGE_BLOCK_SIZE = "ImageBlockSize";
    public final static String RESOLUTION_LEVEL = "ResolutionLevel ";
    public final static String[] XYZ = new String[]{"X","Y","Z"};
    public final static String DATA_SET_INFO = "DataSetInfo";
    public final static String CHANNEL_COLOR = "Color";
    public final static String CHANNEL_COLOR_RANGE = "ColorRange";

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
            ImarisWriter.writeCombinedHeaderFile( imarisFiles, "meta.ims" );
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

    public static ImagePlus getVolume( ImagePlus image, int c, int t, int[] binning )
    {
        ImagePlus volume = new Duplicator().run(
                image, c + 1, c + 1, 1,
                image.getNSlices(), t + 1, t + 1 );

        if ( binning[ 0 ] > 1 || binning[ 1 ] > 1 || binning[ 2 ] > 1 ){
            Binner binner = new Binner();
            volume = binner.shrink(
                    volume,
                    binning[0],
                    binning[1],
                    binning[2],
                    binner.AVERAGE );
        }

        return volume;
    }


    public static int getMaximumArrayIndex( )
    {
        return Integer.MAX_VALUE - 10;
    }
}
