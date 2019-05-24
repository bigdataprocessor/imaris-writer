package de.embl.cba.imaris;

import ij.ImagePlus;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

import static de.embl.cba.imaris.ImarisUtils.delimitedStringToIntegerArray;


@Plugin( type = Command.class, menuPath = "Plugins>BigDataTools>Imaris Writer" )
public class ImarisWriterCommand implements Command
{
	@Parameter
	public LogService logService;

	@Parameter( visibility = ItemVisibility.MESSAGE, persist = false )
	private String message = "";

	@Parameter
	public ImagePlus imagePlus;

	@Parameter( label = "Output directory", style = "directory" )
	public File directory;

//	@Parameter( label = "Binning [ x, y, z ]")
	public String binningString = "1,1,1";

	// TODO: multi-threaded writing, using multiple IJ instances
	// TODO: multi-threaded writing, using the cluster

	@Override
	public void run()
	{
		ImarisWriter writer = new ImarisWriter( imagePlus, directory.getAbsolutePath() );

		writer.setLogService( logService );

		writer.setBinning( delimitedStringToIntegerArray( binningString, "," ) );

		writer.write();
	}

}