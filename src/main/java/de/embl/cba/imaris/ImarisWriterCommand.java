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
//	public String binningString = "1,1,1";

	// TODO: multi-threaded writing, using multiple IJ instances
	// TODO: multi-threaded writing, using the cluster

	@Override
	public void run()
	{
		ImarisWriter writer = new ImarisWriter( imagePlus, directory.getAbsolutePath() );

		writer.setLogService( logService );

		// writer.setBinning( delimitedStringToIntegerArray( binningString, "," ) );

		writer.write();
	}

}
