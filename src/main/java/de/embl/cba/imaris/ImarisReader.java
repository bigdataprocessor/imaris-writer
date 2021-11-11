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

import hdf.hdf5lib.H5;
import net.imglib2.FinalRealInterval;

import java.util.ArrayList;

public class ImarisReader {

    long file_id;

    public ImarisReader( String directory, String filename )
    {
        file_id = H5Utils.openFile( directory, filename );
    }

    public void closeFile()
    {
        H5.H5Fclose( file_id );
    }

    public ArrayList< String > readChannelColors( )
    {
        ArrayList < String > channelColors = new ArrayList<>();

        for ( int c = 0; ; ++c )
        {
            String color = H5Utils.readStringAttribute( file_id,
                    ImarisUtils.DATA_SET_INFO
                            + "/" + ImarisUtils.CHANNEL + c,
                            ImarisUtils.CHANNEL_COLOR );

            if ( color == null ) break;

            channelColors.add( color );

        }

        return ( channelColors ) ;
    }

    public ArrayList< String > readChannelNames( )
    {
        ArrayList < String > channelNames = new ArrayList<>();

        for ( int c = 0; ; ++c )
        {

            String color = H5Utils.readStringAttribute( file_id,
                    ImarisUtils.DATA_SET_INFO
                            + "/" + ImarisUtils.CHANNEL + c,
                            ImarisUtils.CHANNEL_NAME );

            if ( color == null ) break;

            channelNames.add( color );

        }

        return ( channelNames ) ;
    }

    public ArrayList< String > readChannelRanges( )
    {
        ArrayList < String > channelRanges = new ArrayList<>();

        for ( int c = 0; ; ++c )
        {

            String color = H5Utils.readStringAttribute( file_id,
                    ImarisUtils.DATA_SET_INFO
                            + "/" + ImarisUtils.CHANNEL + c,
                            ImarisUtils.CHANNEL_COLOR_RANGE );

            if ( color == null ) break;

            channelRanges.add( color );

        }

        return ( channelRanges ) ;
    }

    public ArrayList< String > readTimePoints( )
    {
        ArrayList < String > timePoints = new ArrayList<>();

        for ( int t = 0; ; ++t )
        {
            String timePoint = H5Utils.readStringAttribute( file_id,
                    ImarisUtils.DATA_SET_INFO
                    + "/" + ImarisUtils.TIME_INFO ,
                    ImarisUtils.TIME_POINT + (t+1) );

            if ( timePoint == null ) break;

            timePoints.add( timePoint );
        }

        return ( timePoints ) ;
    }

    public ArrayList< long[] > readDimensions( )
    {
        ArrayList < long[] > dimensions = new ArrayList<>();

        int numResolutions = Integer.parseInt( H5Utils.readStringAttribute( file_id,
                ImarisUtils.DATA_SET_INFO + "/" + ImarisUtils.IMAGE,
                ImarisUtils.RESOLUTION_LEVELS_ATTRIBUTE ).trim() );

        for ( int resolution = 0; resolution < numResolutions; ++resolution )
        {
            long[] dimension = new long[ 3 ];
            for ( int d = 0; d < 3; ++d )
            {
                // number of pixels at different resolutions
                dimension[ d ] = Integer.parseInt(
                    H5Utils.readStringAttribute( file_id,
                        ImarisUtils.DATA_SET_INFO + "/" + ImarisUtils.IMAGE,
                        ImarisUtils.XYZ[ d ] + resolution ) );
            }
            dimensions.add( dimension );
        }


        /*
        for ( int r = 0; ; ++r )
        {

            String dataSetName = DATA_SET
                    + "/" + RESOLUTION_LEVEL + r
                    + "/" + TIME_POINT + 0
                    + "/" + CHANNEL + 0
                    + "/" + DATA;

            long[] dimension = getDataDimensions( file_id, dataSetName );

            if ( dimension == null ) break;


            dimensions.add( dimension );
        }
        */

        return ( dimensions ) ;
    }

    public FinalRealInterval readCalibratedInterval()
    {

        double[] min = new double[ 3 ];
        double[] max = new double[ 3 ];

        String s;

        for ( int d = 0; d < 3; ++d )
        {
            // physical realInterval
            min[d] = Double.parseDouble( H5Utils.readStringAttribute( file_id, ImarisUtils.DATA_SET_INFO + "/" + ImarisUtils.IMAGE,
                    "ExtMin" + d ).trim() );

            max[d] = Double.parseDouble( H5Utils.readStringAttribute( file_id, ImarisUtils.DATA_SET_INFO + "/" + ImarisUtils.IMAGE,
                    "ExtMax" + d ).trim() );
        }

        FinalRealInterval interval = new FinalRealInterval( min, max );

        return ( interval ) ;
    }
}


