package inchi;



import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

/**
 * class implements running the official InChi executable on a string to generate an InChi key.
 * This is a very slow process, the rate limiting step in the indexing,
 *  and should be replaced by JNI or something that is faster.
 *  
 *  This should be considered a reference implementation.
 * 
 * @author clarkm
 * @date October 2, 2014
 * @copyright 2014 Matthw clark
 *
 */
public class Inchi implements Serializable {
	
	/*
	 * serial ID
	 */
	private static final long serialVersionUID = -2314793220236681231L;
	/*
	 * keep statistics of number of inchi's generated
	 */
	private static long keysGenerated = 0;
	/*
	 * keep statistics of total time required
	 */
	private static long totalTime = 0;
	

	/**
	 * main for demonstration and testing.
	 * 
	 * @param args ignored for thiss test.
	 */
	public static void main(String[] args) {
		
		final File file = new File("clopidogrel.sdf");
		final String result = new Inchi().getInchi(file);
		final String[] inchiAndKey = result.split("\n");
		final int timeInMilliseconds = Inchi.msPerInChI();
		
		System.out.println(String.format("InChI string: %s\nInChI key   : %s\n\nGenerated in %d ms",
				inchiAndKey[0], inchiAndKey[1], timeInMilliseconds));
		
	}

	/**
	 * generate the InChi from a file with any molecular structure format readable by
	 * the inchi program and return the result file as a string.
	 * 
	 * @param mol File with a molecule structure readable by the inchi program
	 * @return outputfile from the inchi program.
	 */
	public  String getInchi(final File inputFile) {
		
		String result = null;
		File outputFile = null;
		final String tempDir = System.getProperty("java.io.tmpdir") + File.separator;
		final String uniqueFname = UUID.randomUUID().toString();
		long time = System.currentTimeMillis();
		
		keysGenerated++;

		try {
			/*
			 * this is way faster than the File.getTempFile, which becomes very slow
			 * if there are a lot of files in the temp directory since it checks for
			 * uniqueness. we don't care that much here because the random GUID is pretty
			 * unique.
			 */
			outputFile = new File(tempDir + "inchi" + uniqueFname + "_temp.out");
			
			/*
			 * Java doesn't have a system-dependent null file build in so we have to 
			 * set it ourselves.
			 */
			String nullFile ="/dev/null";
			String optionChar = "-";
			
			/*
			 * change prefix and null for windoes
			 */
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				nullFile = "NUL";
				optionChar = "/";
			}
			
			String input = inputFile.getCanonicalPath();
			String output = outputFile.getCanonicalPath();
			String log = nullFile;
			String problem = nullFile;
			
			/*
			 * command line for inchi, with arguments
			 */
			final String[] args = {
					"inchi",
					input, 
					output, 
					log,
					problem,
					// Put your list of options here, each as an array element
					// sadly inchi uses -option for linux and /option for windows
					// so you have to detect which to use
					optionChar + "Key",
					optionChar + "AuxNone",
					optionChar + "NoLabels"};
			
			// run JNI program
			InChIJNI.runInchi(args);
		
			result = readFile(outputFile);
			outputFile.delete(); // clean up
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {

			totalTime += System.currentTimeMillis() - time;
		}

		return result;
	}
	
	
	/**
	 * instrumentation - return the milliseconds per invocation
	 * @return average milliseconds per inchi generation
	 */
	public static int msPerInChI() {
		return (int) (totalTime/keysGenerated);
	}
	
	
	/**
	 * Read a small file into a string. Reads directly into a byte buffer, so
	 * this limits to files that could be held in memory.
	 * 
	 * @param file  file to read
	 * @return contents of file in a string
	 * @throws IOException if file does not exist, or cannot be read
	 */
	private String readFile(final File file) throws IOException {
		
		FileInputStream fileStream = null;
		
		try {
			
			String result = null;
			fileStream = new FileInputStream(file);
			final byte[] byteBuffer = new byte[(int) file.length()];
			int bytesRead = fileStream.read(byteBuffer);

			assert (bytesRead == file.length()); // should have read all bytes

			result = new String(byteBuffer).trim();
			fileStream.close();
			return result;
			
		} finally { // try to close even if error is thrown during open/read
			fileStream.close();
		}
	}
}
