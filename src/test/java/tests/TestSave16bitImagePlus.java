package tests;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.util.BdvFunctions;
import de.embl.cba.imaris.ImarisWriter;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import mpicbg.spim.data.SpimDataException;
import org.junit.Test;
import org.scijava.log.LogService;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TestSave16bitImagePlus
{
	@Test
	public void saveAsImaris() throws SpimDataException, IOException
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

		if ( TestConstants.interactive )
			BdvFunctions.show( spimDataMinimal );
	}


	public static void main( String[] args ) throws SpimDataException, IOException
	{
		TestConstants.interactive = true;
		new TestSave16bitImagePlus().saveAsImaris();
	}
}
