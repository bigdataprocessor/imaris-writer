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
	public void saveShortImagePlusAsImaris( )
	{
		final net.imagej.ImageJ ij = new net.imagej.ImageJ();

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
		writer.setLogService( ij.log() );
		writer.write();
	}


	public static void main( String[] args )
	{
		new TestSaveShortImagePlus().saveShortImagePlusAsImaris();
	}


}
