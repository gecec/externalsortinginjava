package com.google.code.externalsorting.csv;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.After;
import org.junit.Test;


public class CsvExternalSortTest {
	private static final String FILE_CSV = "externalSorting.csv";
	private static final String FILE_UNICODE_CSV = "nonLatinSorting.csv";

	File outputfile;

	@Test
	public void testMultiLineFile() throws IOException, ClassNotFoundException {
		String path = this.getClass().getClassLoader().getResource(FILE_CSV).getPath();
		
		File file = new File(path);
		
		outputfile = new File("outputSort1.csv");
		
		Comparator<CSVRecord> comparator = (op1, op2) -> op1.get(0)
				.compareTo(op2.get(0));

		List<File> sortInBatch = CsvExternalSort.sortInBatch(file, comparator, CsvExternalSort.DEFAULTMAXTEMPFILES, Charset.defaultCharset(), null, false, 1);
		
		assertEquals(sortInBatch.size(), 1);
		
		int mergeSortedFiles = CsvExternalSort.mergeSortedFiles(sortInBatch, outputfile, comparator, Charset.defaultCharset(), false, true);
		
		assertEquals(mergeSortedFiles, 4);
		
		BufferedReader reader = new BufferedReader(new FileReader(outputfile));
		String readLine = reader.readLine();

		assertEquals(readLine, "6,this wont work in other systems,3");
		reader.close();
	}
	
	@Test
	public void testNonLatin() throws Exception {
		Field cs = Charset.class.getDeclaredField("defaultCharset");
		cs.setAccessible(true);
		cs.set(null, Charset.forName("windows-1251"));

		String path = this.getClass().getClassLoader().getResource(FILE_UNICODE_CSV).getPath();

		File file = new File(path);

		outputfile = new File("unicode_output.csv");

		Comparator<CSVRecord> comparator = (op1, op2) -> op1.get(0)
				.compareTo(op2.get(0));

		List<File> sortInBatch = CsvExternalSort.sortInBatch(file, comparator, CsvExternalSort.DEFAULTMAXTEMPFILES, StandardCharsets.UTF_8, null, false, 1);

		assertEquals(sortInBatch.size(), 1);

		int mergeSortedFiles = CsvExternalSort.mergeSortedFiles(sortInBatch, outputfile, comparator, StandardCharsets.UTF_8, false, true);

		assertEquals(mergeSortedFiles, 5);

		List<String> lines = Files.readAllLines(Paths.get(outputfile.getPath()), StandardCharsets.UTF_8);

		assertEquals(lines.get(0), "2,זה רק טקסט אחי לקריאה קשה,8");
		assertEquals(lines.get(1), "5,هذا هو النص إخوانه فقط من الصعب القراءة,3");
		assertEquals(lines.get(2), "6,это не будет работать в других системах,3");
	}

	@After
	public void onTearDown() {
		if(outputfile.exists()) {
			outputfile.delete();
		}
	}

}