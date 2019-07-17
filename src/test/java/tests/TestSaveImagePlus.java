package tests;

import de.embl.cba.imaris.ImarisWriter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ShortProcessor;
import org.junit.Test;

public class TestSaveImagePlus
{
	@Test
	public void saveShortImagePlusAsImaris( )
	{
		final net.imagej.ImageJ ij = new net.imagej.ImageJ();

		ij.ui().showUI();

		ImarisWriter writer = new ImarisWriter( createShortImagePlus(), "/Users/tischer/Desktop/tmp" );

		writer.setLogService( ij.log() );

		writer.write();
	}

	private static ImagePlus createShortImagePlus()
	{
		final int sizeZ = 20; // set to 300 to test java indexing issues
		final int sizeXY = 2048;
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

		return imp;
	}

	public static void main( String[] args )
	{
		new TestSaveImagePlus().saveShortImagePlusAsImaris();
	}




}
