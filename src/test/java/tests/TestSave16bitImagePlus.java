package tests;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.util.BdvFunctions;
import de.embl.cba.imaris.ImarisWriter;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestSave16bitImagePlus
{
	@Test
	public void saveAsImaris( ) throws SpimDataException, IOException
	{
		final net.imagej.ImageJ ij = new net.imagej.ImageJ();

		final String fileName = "mri-stack-16bit";

		ImagePlus imp = IJ.openImage( TestConstants.TEST_FOLDER + "test-data" + File.separator + fileName + ".tif" );
		imp.setTitle( "mri-stack-16bit" );

		final String outputFolder = "test-output-little-endian";

		ImarisWriter writer = new ImarisWriter( imp, TestConstants.TEST_FOLDER + outputFolder );
		writer.setLogService( ij.log() );
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
