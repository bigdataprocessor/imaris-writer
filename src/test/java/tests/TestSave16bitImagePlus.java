package tests;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.util.BdvFunctions;
import de.embl.cba.imaris.ImarisWriter;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import org.junit.Test;

public class TestSave16bitImagePlus
{

	@Test
	public void saveAsImaris( ) throws SpimDataException
	{
		final net.imagej.ImageJ ij = new net.imagej.ImageJ();
		ij.ui().showUI();

		ImagePlus imp = IJ.openImage( TestPathConstants.TEST_FOLDER + "test_data/mri-stack-16bit.tif" );
		imp.setTitle( "mri-stack-16bit" );

		ImarisWriter writer = new ImarisWriter( imp, TestPathConstants.TEST_FOLDER + "test-output" );

		writer.setLogService( ij.log() );
		writer.write();

		final SpimDataMinimal spimDataMinimal = new XmlIoSpimDataMinimal().load( TestPathConstants.TEST_FOLDER + "test-output/mri-stack-16bit-header.ims" );

		BdvFunctions.show( spimDataMinimal );
	}


	public static void main( String[] args ) throws SpimDataException
	{
		new TestSave16bitImagePlus().saveAsImaris();
	}




}
