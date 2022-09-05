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
package tests;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import de.embl.cba.imaris.ImarisWriter;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.process.LUT;
import mpicbg.spim.data.SpimDataException;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TestSave16bitImagePlus
{
	@Test
	public void run() throws IOException
	{
		final String fileName = "mri-stack-16bit";

		ImagePlus imp = IJ.openImage( TestConstants.TEST_FOLDER + "test-data" + File.separator + fileName + ".tif" );
		imp.setTitle( "mri-stack-16bit" );

		CompositeImage compositeImage = new CompositeImage( imp );
		compositeImage.setChannelLut( LUT.createLutFromColor( new Color( 255, 0,0 ) ) );

		final String outputFolder = "test-output-little-endian";

		ImarisWriter writer = new ImarisWriter( compositeImage, TestConstants.TEST_FOLDER + outputFolder );
		writer.write();

		final SpimDataMinimal spimDataMinimal = Imaris.openIms( TestConstants.TEST_FOLDER + outputFolder + File.separator + fileName + "-header.ims");
	}

	public static void main( String[] args ) throws SpimDataException, IOException
	{
		TestConstants.interactive = true;
		new TestSave16bitImagePlus().run();
	}
}
