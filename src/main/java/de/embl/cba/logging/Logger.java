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
package de.embl.cba.logging;

import java.util.ArrayList;

public interface Logger {

    /**
     * whether or not to show debug messages
     *
     * @param message
     */
    void setShowDebug( boolean showDebug );


    /**
     * whether or not to show debug messages
     *
     * @param message
     */
    boolean isShowDebug();

    /**
     * prints messages that are merely for information, such as progress of computations
     *
     * @param message
     */
    void info( String message );


    /**
     * prints messages that are merely for information, such as progress of computations
     *
     * @param message
     */
    void progress( String message, String progress );


    /**
     * prints messages that are merely for information, such as progress of computations
     *
     * @param message
     */
    void progress( String header,
				   ArrayList< String > messages,
				   long startTime,
				   long counter, long counterMax );

    /**
     * shows important messages that should not be overlooked by the user
     *
     * @param message
     */
    void error( String message );

    /**
     * shows messages that contain warnings
     *
     * @param message
     */
    void warning( String message );

    /**
     * shows messages that contain information for debugging
     *
     * @param message
     */
    void debug( String message );

    /**
     * displays a progress wheel
     *
     * @param message
     */
    void progressWheel( String message );

}
