# imaris-writer

Java code for saving 3D multi-channel movies into a multi-resolution Imaris compatible hdf5 format. 

## Imaris file format

See here for Imaris file format specifications: 
http://open.bitplane.com/Default.aspx?tabid=268

## Input formats

Supported input image formats are:

- `ImagePlus` objects, as they are used in ImageJ.

## File format

An example output, for the filename `image` looks like this: 

- image-header.ims
    - an hdf5 file, linking into below volumes
- image--C00--T00000.h5
    - hdf5 file containing multiple resolution levels
- image--C01--T00000.h5
    - hdf5 file containing multiple resolution levels
- image--C00--T00001.h5
    - hdf5 file containing multiple resolution levels
- image--C01--T00001.h5
    - hdf5 file containing multiple resolution levels
    

## High-level reading options

### Imaris

The `*.ims` header file be opened using Imaris.

### BigDataViewer
  
The `*.ims` header file can be opened within Fiji:

- [ Plugins > BigDataViewer > Open Imaris (experimental) ]

Example java code that achieves the same:

```
import bdv.spimdata.XmlIoSpimDataMinimal;
SpimDataMinimal image = new XmlIoSpimDataMinimal().load( file.getAbsolutePath() );
BdvFunctions.show( image );
```

### BigDataConverter

Individual resolution levels can be lazy-loaded using:

- [ Plugins > BigDataTools > BigDataConverter ]

which will open them as a 5D `VirtualStack`, i.e. a lazy-loading data structure feeding pixel values into an `ImagePlus`.

### BigDataConverter2

Individual resolution levels can be opened using

- [ Plugins > BigDataTools > BigDataConverter2 ]

which will open them as a 5D `CachedCellImage`, i.e. a lazy-loading data structure feeding pixel values in a `RandomAccessibleInterval`.

## Technical details

### Hdf5 library


```
<dependency>
    <groupId>cisd</groupId>
    <artifactId>jhdf5</artifactId>
</dependency>
```

`import ncsa.hdf.hdf5lib.*`

As far as we know, this is the most core hdf5 library and should contain all functionality. We also explored the higher level `ch.systemsx.cisd.hdf5` library, but certain functionality, such as `filters` did not seem to be available.

## Future plans

- Create per channel sub-folders


