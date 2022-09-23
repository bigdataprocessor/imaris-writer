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
package tests;

import de.embl.cba.imaris.ImarisWriter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ShortProcessor;
import org.junit.Test;

import static tests.TestConstants.TEST_FOLDER;

public class TestSaveShortImagePlus
{
	@Test
	public void run( )
	{
		final int sizeZ = 30; // set to 600 to test java indexing issues
		final int sizeXY = 100; // set to 2048 to test java indexing issues
		final ImageStack imageStack = new ImageStack( sizeXY, sizeXY, sizeZ );

		for ( int i = 1; i <= sizeZ; i++ )
		{
			final ShortProcessor ip = new ShortProcessor( sizeXY, sizeXY );
			imageStack.setProcessor( ip, i );
			if ( ( i - 1 ) % 10 == 0 )
				for ( int x = 0; x < sizeXY; x++ )
					for ( int y = 0; y < sizeXY; y++ )
						ip.set( x, y, x );
		}

		ImagePlus imp = new ImagePlus( "image", imageStack );
		imp.setDimensions( 1, sizeZ, 1 );

		ImarisWriter writer = new ImarisWriter( imp, TEST_FOLDER + "test-data" );
		writer.write();
	}

	public static void main( String[] args )
	{
		new TestSaveShortImagePlus().run();
	}

}
