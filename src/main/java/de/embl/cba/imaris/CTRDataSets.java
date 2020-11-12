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

import java.util.HashMap;
import java.util.Map;

public class CTRDataSets {

    Map< String, DataSet > ctrDataSets = new HashMap<>();

    public void add( int c, int t, int r, DataSet dataSetReference )
    {
        ctrDataSets.put( getKey(c,t,r), dataSetReference );
    }

    public DataSet get( int c, int t, int r )
    {

        return ( ctrDataSets.get( getKey(c,t,r) ) );
    }

    private String getKey( int c, int t, int r )
    {
        String ctr = "c"+c+"t"+t+"r"+r;
        return ( ctr );
    }

    public void addImaris(  int cIntern, int cExtern, int t, int r,
                            String directory,
                            String filename )
    {
        DataSet dataSet = new DataSet(
                directory,
                filename,
                ImarisUtils.DATA_SET
                        + "/" + ImarisUtils.RESOLUTION_LEVEL + r
                        + "/" + ImarisUtils.TIME_POINT + t
                        + "/" + ImarisUtils.CHANNEL + cExtern);

        add( cIntern, t, r, dataSet );

    }

    public void addExternal( int c, int t, int r,
                             String directory,
                             String filename )
    {
        DataSet dataSet = new DataSet(
                directory,
                filename + getChannelTimeString( c, t ) + ".h5",
                 ImarisUtils.RESOLUTION_LEVEL + r );

        add( c, t, r, dataSet );

    }


    public static String getChannelTimeString( int c, int t )
    {
        String s = String.format("--C%02d--T%05d", c, t);
        return ( s );
    }



}
