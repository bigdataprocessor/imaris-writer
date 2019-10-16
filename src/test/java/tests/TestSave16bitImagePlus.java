package tests;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.util.BdvFunctions;
import de.embl.cba.imaris.ImarisWriter;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import org.junit.Test;

import java.io.IOException;

public class TestSave16bitImagePlus
{
	@Test
	public void saveAsImaris( ) throws SpimDataException, IOException
	{
		final net.imagej.ImageJ ij = new net.imagej.ImageJ();

		ImagePlus imp = IJ.openImage( TestConstants.TEST_FOLDER + "test-data/mri-stack-16bit.tif" );
		imp.setTitle( "mri-stack-16bit" );

		ImarisWriter writer = new ImarisWriter( imp, TestConstants.TEST_FOLDER + "test-output" );

		writer.setLogService( ij.log() );
		writer.write();

		final SpimDataMinimal spimDataMinimal = Imaris.openIms( TestConstants.TEST_FOLDER + "test-output/mri-stack-16bit-header.ims" );

		if ( TestConstants.interactive )
			BdvFunctions.show( spimDataMinimal );
	}


	public static void main( String[] args ) throws SpimDataException, IOException
	{
		TestConstants.interactive = true;
		new TestSave16bitImagePlus().saveAsImaris();
	}
}
