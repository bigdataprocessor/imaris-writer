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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageStatistics;
import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

import static de.embl.cba.imaris.H5Utils.writeDoubleAttribute;
import static de.embl.cba.imaris.H5Utils.writeStringAttribute;
import static de.embl.cba.imaris.ImarisUtils.*;

public class H5DataCubeWriter
{
    private long file_id;
    private long memory_type;
    private long file_type;

    public void writeImarisCompatibleResolutionPyramid(
            ImagePlus imp,
            ImarisDataSet idp,
            int c,
            int t ) throws HDF5LibraryException
    {

        file_id = createFile(
                idp.getDataSetDirectory( c, t, 0 ),
                idp.getDataSetFilename( c, t, 0 ) );

        setMemoryTypeAndFileType( imp );

        ImagePlus impResolutionLevel = imp;

        for ( int resolution = 0; resolution < idp.getDimensions().size(); resolution++ )
        {
            if ( resolution > 0 )
            {
                // bin further
                impResolutionLevel = ImarisUtils.bin(
                        impResolutionLevel,
                        idp.getRelativeBinnings().get( resolution ),
                        "binned" );
            }

            writeDataCubeAndAttributes(
                    impResolutionLevel,
                    ImarisUtils.RESOLUTION_LEVEL + resolution,
                    idp.getDimensions().get( resolution ),
                    idp.getChunks().get( resolution ) );

            writeHistogramAndAttributes(
                    impResolutionLevel,
                    ImarisUtils.RESOLUTION_LEVEL + resolution );
        }

        H5.H5Fclose( file_id );
    }

    private void setMemoryTypeAndFileType( ImagePlus imp )
    {
        if ( imp.getBitDepth() == 8 )
        {
            memory_type = HDF5Constants.H5T_NATIVE_UCHAR;
            file_type = HDF5Constants.H5T_STD_U8LE;
        }
        else if ( imp.getBitDepth() == 16 )
        {
            memory_type = HDF5Constants.H5T_NATIVE_USHORT;
//            file_type = HDF5Constants.H5T_STD_U16BE; // BigEndian does not work
            file_type = HDF5Constants.H5T_STD_U16LE;
        }
        else if ( imp.getBitDepth() == 32 )
        {
            memory_type = HDF5Constants.H5T_NATIVE_FLOAT;
            file_type = HDF5Constants.H5T_IEEE_F32LE;
        }
        else
        {
            IJ.showMessage( "Image data type is not supported, " +
                    "only 8-bit, 16-bit and 32-bit floating point are possible." );
        }
    }

    private void writeDataCubeAndAttributes(
            ImagePlus imp,
            String group,
            long[] dimensionXYZ,
            long[] chunkXYZ ) throws HDF5Exception
    {

        // change dimension order to fit hdf5

        long[] dimension = new long[]{
                dimensionXYZ[ 2 ],
                dimensionXYZ[ 1 ],
                dimensionXYZ[ 0 ] };

        long[] chunk = new long[]{
                chunkXYZ[ 2 ],
                chunkXYZ[ 1 ],
                chunkXYZ[ 0 ] };

        long group_id = H5Utils.createGroup( file_id, group );

        long dataspace_id = H5.H5Screate_simple( dimension.length, dimension, null );

        // create "dataset creation property list" (dcpl)
        long dcpl_id = H5.H5Pcreate( HDF5Constants.H5P_DATASET_CREATE );

        // chunks
        H5.H5Pset_chunk( dcpl_id, chunk.length, chunk );

        // compression
        H5.H5Pset_deflate( dcpl_id, 2);

        // create dataset
        long dataset_id = -1;
        try
        {
            dataset_id = H5.H5Dcreate(
                    group_id,
                    DATA,
                    file_type,
                    dataspace_id,
                    HDF5Constants.H5P_DEFAULT,
                    dcpl_id,
                    HDF5Constants.H5P_DEFAULT );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        writeImagePlusData( dataset_id, imp, chunkXYZ );

        // Attributes
        writeSizeAttributes( group_id, dimensionXYZ );
        writeChunkAttributes( group_id, chunkXYZ  );
        writeCalibrationAttribute( dataset_id, imp.getCalibration() );

        H5.H5Sclose( dataspace_id );
        H5.H5Dclose( dataset_id );
        H5.H5Pclose( dcpl_id );
        H5.H5Gclose( group_id );

    }

    private void writeImagePlusData( long dataset_id, ImagePlus imp, long[] chunkXYZ ) throws HDF5Exception
    {
        long dataspace_id;

        long numElements = 1L * imp.getWidth() * imp.getHeight() * imp.getNSlices();
        boolean javaIndexingIssue = numElements > getMaximumArrayIndex();

        if( imp.getBitDepth() == 8 )
        {
            if ( ! javaIndexingIssue )
            {
                byte[] data = getByteData( imp, 0, 0 );

                H5.H5Dwrite( dataset_id,
                        memory_type,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT,
                        data );
            }
            else
            {
                System.out.println( "Very large data set " +
                        "=> saving 8-bit hdf5 plane-wise " +
                        "to circumvent java indexing issues." );

                for ( int z = 0; z < imp.getNSlices(); ++z )
                {
                    // TODO: maybe could make it work also with chunking now.
                    byte[] slice = ( byte [] ) imp.getStack().getProcessor( z + 1 ).getPixels();

                    long[] start = new long[]{ z, 0, 0 };
                    long[] count = new long[]{ 1, imp.getHeight(), imp.getWidth()};

                    dataspace_id = H5.H5Dget_space( dataset_id );

                    // Select hyperslab in file dataspace
                    H5.H5Sselect_hyperslab( dataspace_id,
                            HDF5Constants.H5S_SELECT_SET,
                            start,
                            null,
                            count,
                            null
                    );

                    // Create memspace
                    long memspace = H5.H5Screate_simple( 1,
                            new long[]{ slice.length }, null );

                    // write
                    H5.H5Dwrite( dataset_id,
                            memory_type,
                            memspace,
                            dataspace_id,
                            HDF5Constants.H5P_DEFAULT,
                            slice );
                }
            }
        }
        else if( imp.getBitDepth() == 16 )
        {
            if ( ! javaIndexingIssue )
            {
                short[] data = getShortData( imp, 0, 0 );

                H5.H5Dwrite( dataset_id,
                        memory_type,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT,
                        data );
            }
            else
            {
                System.out.println( "Very large data set " +
                        "=> saving 16-bit hdf5 plane-wise " +
                        "to circumvent java indexing issues." );

                final long dz = 1; //chunkXYZ[ 2 ];

                for ( int z = 0; z < imp.getNSlices(); z += dz )
                {
                    long nz = dz;

                    if ( z + nz >= imp.getNSlices() )
                        nz = ( imp.getNSlices() - z - 1  );

                    // TODO: maybe could make it work also with chunking now.
                    short[] slice = (short [] ) imp.getStack().getProcessor( z + 1 ).getPixels();

                    long[] start = new long[]{ z, 0, 0 };
                    long[] count = new long[]{
                            nz,
                            imp.getHeight(),
                            imp.getWidth()};

                    dataspace_id = H5.H5Dget_space( dataset_id );

                    // Select hyperslab in file dataspace
                    H5.H5Sselect_hyperslab(
                            dataspace_id,
                            HDF5Constants.H5S_SELECT_SET,
                            start,
                            null,
                            count,
                            null
                    );

                    // Create memspace
                    long memspace = H5.H5Screate_simple(
                            1,
                            new long[]{ slice.length * nz },
                            null );

                    // write
                    H5.H5Dwrite( dataset_id,
                            memory_type,
                            memspace,
                            dataspace_id,
                            HDF5Constants.H5P_DEFAULT,
                            slice );
                }
            }
        }
        else if( imp.getBitDepth() == 32 )
        {
            float[][] data = getFloatData( imp, 0, 0 );

            H5.H5Dwrite( dataset_id,
                    memory_type,
                    HDF5Constants.H5S_ALL,
                    HDF5Constants.H5S_ALL,
                    HDF5Constants.H5P_DEFAULT,
                    data );
        }
        else
        {
            IJ.showMessage( "Image data type is not supported, " +
                    "only 8-bit, 16-bit and 32-bit are possible." );
        }

    }

    private void writeSizeAttributes( long group_id, long[] dimension )
    {
        for ( int d = 0; d < 3; ++d )
        {
            writeStringAttribute( group_id,
                    IMAGE_SIZE + XYZ[d],
                    String.valueOf( dimension[d]) );
        }
    }

    private void writeChunkAttributes( long group_id, long[] chunks )
    {
        for ( int d = 0; d < 3; ++d )
        {
            writeStringAttribute( group_id,
                    IMAGE_BLOCK_SIZE + XYZ[d],
                    String.valueOf( chunks[d]) );
        }
    }

    private void writeCalibrationAttribute( long object_id, Calibration calibration )
    {

        double[] calibrationXYZ = new double[]
                {
                        calibration.pixelWidth,
                        calibration.pixelHeight,
                        calibration.pixelDepth
                };

        writeDoubleAttribute( object_id, "element_size_um", calibrationXYZ );

    }

    private void writeHistogramAndAttributes(ImagePlus imp, String group )
    {
        long group_id = H5Utils.createGroup( file_id, group );

        ImageStatistics imageStatistics = imp.getStatistics();

        /*
        imaris expects 64bit unsigned int values:
        - http://open.bitplane.com/Default.aspx?tabid=268
        thus, we are using as memory type: H5T_NATIVE_ULLONG
        and as the corresponding dataset type: H5T_STD_U64LE
        - https://support.hdfgroup.org/HDF5/release/dttable.html
        */
        long[] histogram = new long[ imageStatistics.histogram.length ];
        for ( int i = 0; i < imageStatistics.histogram.length; ++i )
        {
            histogram[i] = imageStatistics.histogram[i];
        }

        long[] histo_dims = { histogram.length };

        long histo_dataspace_id = H5.H5Screate_simple(
                histo_dims.length, histo_dims, null);

        long histo_dataset_id = H5.H5Dcreate( group_id, HISTOGRAM,
                HDF5Constants.H5T_STD_U64LE, histo_dataspace_id,
                HDF5Constants.H5P_DEFAULT,
                HDF5Constants.H5P_DEFAULT,
                HDF5Constants.H5P_DEFAULT);

        H5.H5Dwrite(histo_dataset_id,
                HDF5Constants.H5T_NATIVE_ULLONG,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
                HDF5Constants.H5P_DEFAULT, histogram);

        writeStringAttribute( group_id,
                HISTOGRAM + "Min",
                String.valueOf( imageStatistics.min ) );

        writeStringAttribute( group_id,
                HISTOGRAM + "Max",
                String.valueOf( imageStatistics.max ) );

        H5.H5Dclose( histo_dataset_id );
        H5.H5Sclose( histo_dataspace_id );
        H5.H5Gclose( group_id );

    }

    private long createFile( String directory, String filename )
    {
        return( H5Utils.createFile( directory, filename  ) );
    }

    private byte[] getByteData( ImagePlus imp, int c, int t )
    {
        ImageStack stack = imp.getStack();

        int[] size = new int[]{
                imp.getWidth(),
                imp.getHeight(),
                imp.getNSlices()
        };


        byte[] data = new byte[ size[0] * size[1] * size[2] ];

        // TODO: make copying multi-threaded

        int dataArrayIndex = 0;
        for (int z = 0; z < imp.getNSlices(); z++)
        {
            int n = imp.getStackIndex(c+1, z+1, t+1);

            final byte[] pixels = ( byte[] ) stack.getProcessor( n ).getPixels();
            System.arraycopy( pixels, 0, data, dataArrayIndex, pixels.length );
            dataArrayIndex += pixels.length;
        }

        return ( data );

    }

    private short[] getShortData( ImagePlus imp, int c, int t )
    {
        ImageStack stack = imp.getStack();

        int[] size = new int[]{
                imp.getWidth(),
                imp.getHeight(),
                imp.getNSlices()
        };


        short[] data = new short[ size[0] * size[1] * size[2] ];

        // TODO: make copying multi-threaded

        int dataArrayIndex = 0;
        for (int z = 0; z < imp.getNSlices(); z++)
        {
            int n = imp.getStackIndex(c+1, z+1, t+1);

            final short[] pixels = ( short[] ) stack.getProcessor( n ).getPixels();
            System.arraycopy( pixels, 0, data, dataArrayIndex, pixels.length );
            dataArrayIndex += pixels.length;
        }

        return ( data );

    }

    private float[][] getFloatData(ImagePlus imp, int c, int t)
    {
        ImageStack stack = imp.getStack();

        int[] size = new int[]{
                imp.getWidth(),
                imp.getHeight(),
                imp.getNSlices()
        };

        float[][] data = new float[ size[2] ] [ size[1] * size[0] ];

        for (int z = 0; z < imp.getNSlices(); z++)
        {
            int n = imp.getStackIndex(c+1, z+1, t+1);
            data[z] = (float[]) stack.getProcessor(n).getPixels();
        }
        return ( data );

    }


}
