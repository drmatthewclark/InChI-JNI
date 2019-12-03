package inchi;

/**
 * class to load the JNI version of InChI use it to compute InChI strings.
 * 
 * @author Matthew Clark
 * @date October 6, 2014
 *
 * 
 */
public class InChIJNI  {
	
	/*
	 * load the operating system specific library. This is  .dll for windoes and .so for linux.
	 * This uses the IUPAC code for the inchi program, which could have memory leaks, bugs and
	 * other bad behaviour that will disturb the operation of your JVM.
	 * 
	 * This dll or so has to be on your library path, which can be set by
	 * java -Djava.library.path=<the path>  or LD_LIBRARY_PATH
	 * 
	 * If you move this to a different package you have to remake the .h and .c files with javah and
	 * recompile/relink the library because the package name goes into the dll file.
	 * 
	 * The only change to the standard inchimain.c file is to add the line
	 *     inchi_ios_flush(pout);
	 * at like 870, before the return 0 statement
	 * 
	 *  because otherwise it doesn't flush the output file before exiting.
	 *  
	 */
	static {
		try {
			System.loadLibrary("inchi");
		} catch (Exception e) {
			System.err.println("error loading inchi library: " + e);
		}
	}

	/**
	 * given a molecule structure in a format that the InChI program can understand, return the
	 * InChI key for the molecule.
	 * 
	 * for example, call with  " InChiJNI.runInchi(args);".  You write the input file, or pass the file name
	 * of an existing input in the argument array.  The output, and log files are written by the inchi program
	 * as if you ran it on the command line.  You can then open them and read the output in your java program.
	 * 
	 * @param String[] array of arguments to the inchi program.
	 */
	public static native void runInchi(final String[] args);
	
}
