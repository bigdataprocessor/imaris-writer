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
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

import java.io.File;
import java.util.ArrayList;

public abstract class H5Utils
{
    public static void writeIntegerAttribute(
            int dataset_id,
            String attrName,
            int[] attrValue ) throws HDF5Exception
    {
        long[] attrDims = { attrValue.length };

        // Create the data space for the attribute.
        long dataspace_id = H5.H5Screate_simple(attrDims.length, attrDims, null);

        // Create a dataset attribute.
//        int attribute_id = H5.H5Acreate(dataset_id, attrName,
//                HDF5Constants.H5T_STD_I32BE, dataspace_id,
//                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

        long attribute_id = H5.H5Acreate(dataset_id, attrName,
                HDF5Constants.H5T_STD_I32BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        // Write the attribute data.
        H5.H5Awrite(attribute_id, HDF5Constants.H5T_NATIVE_INT, attrValue);

        // Close the attribute.
        H5.H5Aclose(attribute_id);
    }

    public static void writeDoubleAttribute( long dataset_id, String attrName, double[] attrValue ) throws HDF5Exception
    {

        long[] attrDims = { attrValue.length };

        // Create the data space for the attribute.
        long dataspace_id = H5.H5Screate_simple(attrDims.length, attrDims, null);

        // Create a dataset attribute.
        long attribute_id = H5.H5Acreate(dataset_id, attrName,
                HDF5Constants.H5T_IEEE_F64BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        // Write the attribute data.
        H5.H5Awrite(attribute_id, HDF5Constants.H5T_NATIVE_DOUBLE, attrValue);

        // Close the attribute.
        H5.H5Aclose(attribute_id);
    }

    public static void writeStringAttribute( long dataset_id, String attrName, String attrValue )
    {

        long[] attrDims = { attrValue.getBytes().length };

        // Create the data space for the attribute.
        long dataspace_id = H5.H5Screate_simple(attrDims.length, attrDims, null);

        // Create the data space for the attribute.
        //int dataspace_id = H5.H5Screate( HDF5Constants.H5S_SCALAR );

        // Create attribute type
        //int type_id = H5.H5Tcopy( HDF5Constants.H5T_C_S1 );
        //H5.H5Tset_size(type_id, attrValue.length());

        long type_id = HDF5Constants.H5T_C_S1;

        // Create a dataset attribute.
        long attribute_id = H5.H5Acreate( dataset_id, attrName,
                type_id, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

        // Write the attribute
        H5.H5Awrite(attribute_id, type_id, attrValue.getBytes());

        // Close the attribute.
        H5.H5Aclose(attribute_id);
    }

    public static void writeLongArrayListAs32IntArray( int group_id, ArrayList< long[] > list, String name ) throws
            HDF5Exception
    {

        int[][] data = new int[ list.size() ][ list.get(0).length ];

        for (int i = 0; i < list.size(); i++)
        {
            for (int j = 0; j < list.get(i).length; j++)
            {
                data[i][j] = (int) list.get(i)[j];
            }
        }

        long[] data_dims = { data.length, data[0].length };

        long dataspace_id = H5.H5Screate_simple( data_dims.length, data_dims, null );

        long dataset_id = H5.H5Dcreate(group_id, name,
                HDF5Constants.H5T_STD_I32LE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        H5.H5Dwrite( dataset_id, HDF5Constants.H5T_NATIVE_INT,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data );

        H5.H5Dclose(dataset_id);

        H5.H5Sclose(dataspace_id);

    }

    public static void writeLongArrayListAsDoubleArray( int group_id, ArrayList < long[] > list, String name ) throws HDF5Exception
    {

        double[] data = new double[list.size() * list.get(0).length];

        int p = 0;
        for (int i = 0; i < list.size(); i++)
        {
            for (int j = 0; j < list.get(i).length; j++)
            {
                data[ p++ ] = list.get(i)[j];
            }
        }

        long[] data_dims = { list.size(), list.get(0).length };

        long dataspace_id = H5.H5Screate_simple( data_dims.length, data_dims, null );

        long dataset_id = H5.H5Dcreate( group_id, name,
                HDF5Constants.H5T_IEEE_F64BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        H5.H5Dwrite( dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data );

        H5.H5Dclose(dataset_id);

        H5.H5Sclose(dataspace_id);

    }

    public static void writeIntArrayListAsDoubleArray( int group_id, ArrayList < int[] > list, String name ) throws
            HDF5Exception
    {

        double[] data = new double[list.size() * list.get(0).length];

        int p = 0;
        for (int i = 0; i < list.size(); i++)
        {
            for (int j = 0; j < list.get(i).length; j++)
            {
                data[ p++ ] = list.get(i)[j];
            }
        }

        long[] data_dims = { list.size(), list.get(0).length };

        long dataspace_id = H5.H5Screate_simple( data_dims.length, data_dims, null );

        long dataset_id = H5.H5Dcreate( group_id, name,
                HDF5Constants.H5T_IEEE_F64BE, dataspace_id,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

        H5.H5Dwrite( dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data );

        H5.H5Dclose(dataset_id);

        H5.H5Sclose(dataspace_id);

    }

    public static long createGroup( long file_id, String groupName ) throws HDF5LibraryException
    {
        long group_id;

        try
        {
            group_id = H5.H5Gopen( file_id, groupName, HDF5Constants.H5P_DEFAULT );

        }
        catch ( Exception e )
        {

            // create group (and intermediate groups)
            long gcpl_id = H5.H5Pcreate( HDF5Constants.H5P_LINK_CREATE );
            H5.H5Pset_create_intermediate_group( gcpl_id, true );
            group_id = H5.H5Gcreate(file_id, groupName, gcpl_id, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );
        }

        return ( group_id );

    }

    public static long openGroup( int file_id, String groupName )
    {
        long group_id;

        try
        {
            group_id = H5.H5Gopen( file_id, groupName, HDF5Constants.H5P_DEFAULT );
        }
        catch ( Exception e )
        {
            return ( -1 );
        }

        return ( group_id );
    }


    public static long openFile( String directory, String filename )
    {
        String path = directory + File.separator + filename;
        File file = new File( path );

        long file_id = -1;

        if ( file.exists() )
        {
            try
            {
                file_id = H5.H5Fopen( path, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
            }
            catch ( Exception e )
            {
                file_id = -1;
            }
        }

        return ( file_id );
    }


    public static long createFile( String directory, String filename )
    {
        String path = directory + File.separator + filename;

        File file = new File( path );

        if ( file.exists() )
        {
            file.delete();
        }

        long file_id = H5.H5Fcreate(
                path,
                HDF5Constants.H5F_ACC_TRUNC,
                HDF5Constants.H5P_DEFAULT,
                HDF5Constants.H5P_DEFAULT);

        return( file_id );

    }

    public static long[] getDataDimensions( int object_id, String dataName )
    {
        try
        {
            long dataset_id = H5.H5Dopen( object_id, dataName, HDF5Constants.H5P_DEFAULT );
            long dataspace_id = H5.H5Dget_space( dataset_id );

            int ndims = H5.H5Sget_simple_extent_ndims( dataspace_id );

            long[] dimensions = new long[ ndims ];

            H5.H5Sget_simple_extent_dims( dataspace_id, dimensions, null );

            return ( dimensions );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    public static String readStringAttribute( long object_id,
                                              String objectName,
                                              String attributeName )
    {
        String attributeString = "";

        try
        {
            long attribute_id = H5.H5Aopen_by_name( object_id, objectName, attributeName, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT );

            if ( attribute_id < 0 )
            {
                return null;
            }

            long filetype_id = H5.H5Aget_type( attribute_id );
            long sdim = H5.H5Tget_size( filetype_id );
            sdim++; // Make room for null terminator
            long dataspace_id = H5.H5Aget_space( attribute_id );
            long[] dims = { 4 };
            H5.H5Sget_simple_extent_dims( dataspace_id, dims, null );
            byte[][] dset_data = new byte[ ( int ) dims[ 0 ] ][ ( int ) sdim ];
            StringBuffer[] str_data = new StringBuffer[ ( int ) dims[ 0 ] ];

            // Create the memory datatype.
            long memtype_id = H5.H5Tcopy( HDF5Constants.H5T_C_S1 );
            H5.H5Tset_size( memtype_id, sdim );

            // Read data.
            H5.H5Aread( attribute_id, memtype_id, dset_data );
            byte[] tempbuf = new byte[ ( int ) sdim ];
            for ( int indx = 0; indx < ( int ) dims[ 0 ]; indx++ )
            {
                for ( int jndx = 0; jndx < sdim; jndx++ )
                {
                    tempbuf[ jndx ] = dset_data[ indx ][ jndx ];
                }
                str_data[ indx ] = new StringBuffer( new String( tempbuf ) );
            }

            for ( int i = 0; i < str_data.length; i++ )
            {
                attributeString += str_data[ i ];
            }

            // remove null chars
            attributeString = attributeString.replace( "\u0000", "" );

        }
        catch ( Exception e )
        {
            attributeString = null;
        }


        return ( attributeString );


    }




}
