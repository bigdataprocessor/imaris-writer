import de.embl.cba.imaris.ImarisWriterCommand;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

public class RunImarisWriterCommand
{

	public static void main( String... args )
	{
		final net.imagej.ImageJ ij = new net.imagej.ImageJ();
		ij.ui().showUI();

		createAndShowImage();

		ij.command().run( ImarisWriterCommand.class, true );
	}

	private static void createAndShowImage()
	{
		final ImageStack imageStack = new ImageStack( 100, 100, 80 );

		for ( int i = 1; i <= 80; i++ )
		{
			imageStack.setProcessor( new ByteProcessor( 100, 100  ), i );
		}

		ImagePlus imp = new ImagePlus( "image", imageStack );
		imp.setDimensions( 2, 20, 2 ); // 2 * 20 * 2 = 80;
		imp.show();
	}


}
