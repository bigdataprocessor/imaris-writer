/*-
 * #%L
 * imaris-writer
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.LUT;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;

import java.awt.*;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.ArrayList;

import static ij.CompositeImage.GRAYSCALE;

public class ImarisDataSet {

    private ArrayList < long[] > dimensions;
    private ArrayList < int[] > relativeBinnings;
    private ArrayList < long[] > chunks;
    private ArrayList < String > channelColors;
    private ArrayList < String > channelRanges;
    private ArrayList < String > channelNames;
    private RealInterval interval;
    private CTRDataSets ctrDataSets;
    private ArrayList < String > timePoints;

    // Trying to make blocks of about 8000 voxels in size (8-bit)
    // Because I read somewhere that the OS reads often anyway in blocks of around 8000 bytes...
    private static int CHUNKING_XYZ = 64;
    private static int CHUNKING_XY_HIGHEST_RESOLUTION = CHUNKING_XYZ;
    private static int CHUNKING_Z_HIGHEST_RESOLUTION = CHUNKING_XYZ;

    public ImarisDataSet( File file )
    {
        initFromImarisFile( file.getParent(), file.getName() );
    }

    public ImarisDataSet( String directory, String filename )
    {
        initFromImarisFile( directory, filename );
    }


    public ImarisDataSet( ImagePlus imp,
                          int[] binning,
                          String directory,
                          String filenameStump )
    {
        setDimensionsBinningsChunks( imp, binning );
        setTimePoints( imp );
        setChannels( imp );
        setInterval( imp );

        ctrDataSets = new CTRDataSets();

        for ( int c = 0; c < channelColors.size(); ++c )
        {
            for ( int t = 0; t < timePoints.size(); ++t )
            {
                for ( int r = 0; r < dimensions.size(); ++r )
                {
                    ctrDataSets.addExternal( c, t, r, directory, filenameStump );
                }
            }
        }
    }

    private void initFromImarisFile( String directory, String filename )
    {
        ImarisReader reader = new ImarisReader( directory, filename );

        channelColors = reader.readChannelColors();
        channelNames = reader.readChannelNames();
        channelRanges =  reader.readChannelRanges();
        timePoints = reader.readTimePoints();
        dimensions = reader.readDimensions();
        interval = reader.readCalibratedInterval();

        ctrDataSets = new CTRDataSets();

        for ( int c = 0; c < channelColors.size(); ++c )
        {
            for ( int t = 0; t < timePoints.size(); ++t )
            {
                for ( int r = 0; r < dimensions.size(); ++r )
                {
                    ctrDataSets.addImaris( c, c, t, r, directory, filename );
                }
            }
        }

        reader.closeFile();
    }

    public ArrayList< String > getChannelNames()
    {
        return channelNames;
    }

    public String getDataSetDirectory( int c, int t, int r)
    {
        return ctrDataSets.get( c, t, r ).directory;
    }

    public String getDataSetFilename( int c, int t, int r )
    {
        return ( ctrDataSets.get( c, t, r ).filename );
    }

    public String getDataSetGroupName( int c, int t, int r)
    {
        return ( ctrDataSets.get( c, t, r ).h5Group );
    }

    public ArrayList< int[] > getRelativeBinnings()
    {
        return relativeBinnings;
    }

    public RealInterval getInterval()
    {
        return interval;
    }

    public ArrayList< String > getChannelColors()
    {
        return channelColors;
    }

    public int getNumChannels()
    {
        return channelNames.size();
    }

    public ArrayList< String > getTimePoints()
    {
        return timePoints;
    }

    public ArrayList< long[] > getDimensions()
    {
        return dimensions;
    }

    public ArrayList< long[] > getChunks()
    {
        return chunks;
    }

    private long[] getImageSize(ImagePlus imp, int[] primaryBinning )
    {
        long[] size = new long[3];

        if ( primaryBinning[0] > 1 || primaryBinning[1] > 1 || primaryBinning[2] > 1 )
        {
            size[0] = imp.getWidth();
            size[1] = imp.getHeight();
            size[2] = imp.getNSlices();

            for ( int d = 0; d < 3; ++d )
            {
                size[d] /= primaryBinning[d];
            }

        }
        else
        {
            size[0] = imp.getWidth();
            size[1] = imp.getHeight();
            size[2] = imp.getNSlices();
        }

        return ( size );

    }

    private void setDimensionsBinningsChunks( ImagePlus imp, int[] primaryBinning )
    {
        dimensions = new ArrayList<>();
        relativeBinnings = new ArrayList<>();
        chunks = new ArrayList<>();

        long[] initialChunks = new long[]{
                CHUNKING_XY_HIGHEST_RESOLUTION,
                CHUNKING_XY_HIGHEST_RESOLUTION,
                CHUNKING_Z_HIGHEST_RESOLUTION };
        int[] initialBinning = new int[]{ 1, 1, 1 };

        for ( int iResolution = 0; ; ++iResolution )
        {
            long currentVolume;
            long[] currentChunks;
            int[] currentRelativeBinning = new int[3];
            long[] currentDimensions = new long[3];

            if ( iResolution == 0 )
            {
                currentDimensions = getImageSize( imp, primaryBinning );

                currentChunks = initialChunks;

                ensureChunkSizesNotExceedingCurrentImageDimensions(
                        currentDimensions, currentChunks );

                currentRelativeBinning = initialBinning;
            }
            else
            {
                long[] lastDimensions = dimensions.get( iResolution - 1 );
                long lastVolume = lastDimensions[ 0 ]
                        * lastDimensions[ 1 ] * lastDimensions[ 2 ];

                setDimensionsAndBinningsForThisResolutionLayer(
                        currentDimensions,
                        currentRelativeBinning,
                        lastDimensions,
                        lastVolume );

                currentChunks = getChunksForThisResolutionLayer( currentDimensions );

            }

            currentVolume = currentDimensions[ 0 ]
                    * currentDimensions[ 1 ] * currentDimensions[ 2 ];

            adaptZChunkingToAccomodateJavaIndexingLimitations(
                    currentVolume,
                    currentChunks );

            dimensions.add( currentDimensions );

            chunks.add( currentChunks );

            relativeBinnings.add( currentRelativeBinning );

            if ( currentVolume < ImarisUtils.MIN_VOXELS )
            {
                break;
            }
        }
    }

    private void setDimensionsAndBinningsForThisResolutionLayer(
            long[] currentDimensions,
            int[] currentRelativeBinning,
            long[] lastDimensions,
            long lastVolume )
    {
        for ( int d = 0; d < 3; d++ )
        {
            long lastSizeThisDimensionSquared =
                    lastDimensions[ d ] * lastDimensions[ d ];

            long lastPerpendicularPlaneSize = lastVolume / lastDimensions[ d ];

            if ( 100 * lastSizeThisDimensionSquared > lastPerpendicularPlaneSize )
            {
                currentDimensions[ d ] = lastDimensions[ d ] / 2;
                currentRelativeBinning[ d ] = 2;
            }
            else
            {
                currentDimensions[ d ] = lastDimensions[ d ];
                currentRelativeBinning[ d ] = 1;
            }
            currentDimensions[ d ] = Math.max( 1, currentDimensions[ d ] );
        }
    }

    private long[] getChunksForThisResolutionLayer( long[] currentDimensions )
    {
        long[] currentChunks;
        currentChunks = new long[]{ CHUNKING_XYZ, CHUNKING_XYZ, CHUNKING_XYZ };

        ensureChunkSizesNotExceedingCurrentImageDimensions(
                currentDimensions, currentChunks );

        return currentChunks;
    }

    private void ensureChunkSizesNotExceedingCurrentImageDimensions(
            long[] currentDimensions,
            long[] currentChunks )
    {
        for ( int d = 0; d < 3; d++ )
            if ( currentChunks[ d ] > currentDimensions[ d ] )
                currentChunks[ d ] = currentDimensions[ d ];
    }

    private void adaptZChunkingToAccomodateJavaIndexingLimitations(
            long currentVolume,
            long[] currentChunks )
    {
        if ( currentVolume > ImarisUtils.getMaximumArrayIndex() )
        {
            currentChunks[ 2 ] = 1;
            IJ.log( "Data volume is larger than maximum Java indexing. \n" +
                    "Thus, setting z-chucking at this resolution level to 1." );
        }
    }

    private void setTimePoints( ImagePlus imp )
    {
        timePoints = new ArrayList<>();

        for ( int t = 0; t < imp.getNFrames(); ++t )
        {
            // TODO: extract real information from imp?
            timePoints.add("2000-01-01 00:00:0" + t);
        }
    }

    private void setChannels( ImagePlus imp )
    {
        channelColors = new ArrayList<>();
        channelNames = new ArrayList<>();
        channelRanges = new ArrayList<>();

        for ( int c = 0; c < imp.getNChannels(); ++c )
        {
            if ( imp instanceof CompositeImage )
            {
                CompositeImage compositeImage = ( CompositeImage ) imp;
                LUT channelLut = compositeImage.getChannelLut( c + 1 );
				int mode = compositeImage.getMode();
				if ( channelLut == null || mode == GRAYSCALE )
				{
					channelColors.add( ImarisUtils.DEFAULT_COLOR );
				}
				else
				{
					IndexColorModel cm = channelLut.getColorModel();
					if ( cm == null )
					{
						channelColors.add( ImarisUtils.DEFAULT_COLOR );
					}
					else
					{
						int i = cm.getMapSize() - 1;
						String color = "" + cm.getRed( i ) / 255.0 + " " + cm.getGreen( i ) / 255.0 + " " + cm.getBlue( i ) / 255.0;
						channelColors.add( color );
					}

				}

				compositeImage.setC( c + 1 );
                channelRanges.add( "" + compositeImage.getDisplayRangeMin() + " " +  compositeImage.getDisplayRangeMax() );
            }
            else
            {
                channelColors.add( ImarisUtils.DEFAULT_COLOR );
                channelRanges.add( "" + imp.getDisplayRangeMin() + " " +  imp.getDisplayRangeMax() );
            }
            channelNames.add( "channel_" + c );
        }
    }

    public void setChannelNames( ArrayList< String > channelNames )
    {
        this.channelNames = channelNames;
    }

    private void setInterval( ImagePlus imp )
    {
        double[] min = new double[3];
        double[] max = new double[3];

        Calibration calibration = imp.getCalibration();

        double conversionToMicrometer = 1.0;

        if ( calibration.getUnit().equals( "nm" )
                || calibration.getUnit().equals( "nanometer" )
                || calibration.getUnit().equals( "nanometre" ) )
        {
            conversionToMicrometer = 1.0 / 1000.0;
        }

        max[ 0 ] = imp.getWidth() * calibration.pixelWidth * conversionToMicrometer;
        max[ 1 ] = imp.getHeight() * calibration.pixelHeight * conversionToMicrometer;
        max[ 2 ] = imp.getNSlices() * calibration.pixelDepth * conversionToMicrometer;

        interval = new FinalRealInterval( min, max );
    }

    public void addChannelsFromImaris( File file )
    {
        addChannelsFromImaris( file.getParent(), file.getName() );
    }

    public void addChannelsFromImaris( String directory, String filename )
    {
        ImarisReader reader = new ImarisReader( directory, filename );

        int nc = reader.readChannelColors().size();
        int nt = reader.readTimePoints().size();
        int nr = reader.readDimensions().size();

        int currentNumChannelsInMetaFile = channelColors.size();

        for ( int c = 0; c < nc; ++c )
        {
            channelColors.add( reader.readChannelColors().get( c ) );
            channelNames.add( reader.readChannelNames().get( c ) );
            channelRanges.add( reader.readChannelRanges().get( c ) );

            for ( int t = 0; t < nt; ++t )
            {
                for ( int r = 0; r < nr; ++r )
                {
                    ctrDataSets.addImaris( c + currentNumChannelsInMetaFile, c, t, r, directory, filename);
                }
            }
        }
    }

    public ArrayList< String > getChannelRanges()
    {
        return channelRanges;
    }
}
