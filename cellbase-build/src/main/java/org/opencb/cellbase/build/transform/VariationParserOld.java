package org.opencb.cellbase.build.transform;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class VariationParserOld {

	private String readLine = null;
	private File path = null;

	private BufferedReader br = null;
	
	private int LIMITROWS = 1500000;
	private Boolean indexed = true;

	private Map<String, String> seqRegion = new HashMap<String, String>();
	private Map<String, String> source = new HashMap<String, String>();
	private Map<String, String> phenotype = new HashMap<String, String>();
	private Map<String, String> study = new HashMap<String, String>();
	private Map<String, String> alleleCode = new HashMap<String, String>();

	static Connection conndb1 = null;
//	Connection conndb2 = null;

	public VariationParserOld() {


	}

	public void createaSQLiteDatabase(Path variationGzipPath) {
		try {
//			this.path = new File(path);
			Class.forName("org.sqlite.JDBC");
			conndb1 = DriverManager.getConnection("jdbc:sqlite:"+variationGzipPath.toAbsolutePath().toString()+"/variation.db");
			conndb1.setAutoCommit(false);
//			conndb2 = DriverManager.getConnection("jdbc:sqlite:mydb2.db");
//			conndb2.setAutoCommit(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		List<String> filenames = new ArrayList<String>(20);
		filenames.add("allele_code.txt.gz");
		filenames.add("genotype_code.txt.gz");
		filenames.add("motif_feature.txt.gz");
		filenames.add("motif_feature_variation.txt.gz");
		//		filenames.add("population_genotype.txt.gz");
		filenames.add("sample.txt.gz");
		//		filenames.add("variation_annotation.txt.gz");

		//		filenames.add("allele.txt.gz");
		//		filenames.add("transcript_variation.txt.gz");

		filenames.add("phenotype.txt.gz");
		//		filenames.add("variation_feature.txt.gz");
		//		filenames.add("variation_synonym.txt.gz");
		//		filenames.add("variation.txt.gz");
		//		filenames.add("seq_region.txt.gz");
		//		filenames.add("source.txt.gz");
		//		filenames.add("study.txt.gz");

		// this.loadHashSeqRegion();
		// this.loadHashSource();
		// this.loadHashPhenotype();
		// this.loadHashStudy();
		// this.loadHashAlleleCode();
//		this.FindAndLoadFiles(new File(path));
		//this.CoreVariationParser();
		
//		BufferedReader br = null;
		File[] myFiles = variationGzipPath.toFile().listFiles();
		for (File file: myFiles) {

			if (filenames.contains(file.getName())) {
				try {
					System.out.println("Load File: " + variationGzipPath.resolve(file.toPath()).toFile());
					br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(variationGzipPath.resolve(file.toPath()).toFile()))));
//					Paths.get(variationGzipFiles.toString(), file.getName()).toFile()))));

					switch (file.getName()) {
					case "allele_code.txt.gz":
						this.alleleCode();
						break;
					case "allele.txt.gz":
						this.allele();
						break;
					case "genotype_code.txt.gz":
						this.genotypeCode();
						break;
					case "motif_feature.txt.gz":
						this.motifFeature();
						break;
					case "motif_feature_variation.txt.gz":
						this.motifFeatureVariation();
						break;
					case "phenotype.txt.gz":
						this.phenotype();
						break;
					case "population_genotype.txt.gz":
						this.populationGenotype();
						break;
					case "sample.txt.gz":
						this.sample();
						break;
					case "variation_annotation.txt.gz":
						this.variationAnnotation();
						break;
					case "transcript_variation.txt.gz":
						this.transcriptVariation();
						break;
					case "variation_feature.txt.gz":
						this.variationFeature();
						break;
					case "variation_synonym.txt.gz":
						this.variationSynonim();
						break;
					case "seq_region.txt.gz":
						this.seqRegion();
						break;
					case "source.txt.gz":
						this.source();
						break;
					case "study.txt.gz":
						this.study();
						break;

					default:
						break;
					}

					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	private void loadHashAlleleCode() {
		try {

			File alleleCodeFile = Paths.get(path.toString(), "allele_code.txt.gz").toFile();

			if (alleleCodeFile.exists()) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(alleleCodeFile))));

				while ((readLine = br.readLine()) != null) {
					String[] readLineFields = readLine.split("\t");
					alleleCode.put(readLineFields[0], readLineFields[1]);
				}

				System.out.println("loadHashAlleleCode: " + alleleCode.size());

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadHashStudy() {
		try {

			File studyFile = Paths.get(path.toString(), "study.txt.gz").toFile();

			if (studyFile.exists()) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(studyFile))));

				while ((readLine = br.readLine()) != null) {
					String[] readLineFields = readLine.split("\t");
					source.put(readLineFields[0], readLineFields[1] + "," + readLineFields[2] + "," + readLineFields[3]
							+ "," + readLineFields[4] + "," + readLineFields[5] + "," + readLineFields[6]);
				}

				System.out.println("loadHashSource: " + source.size());

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadHashPhenotype() {
		try {

			File phenotypeFile = Paths.get(path.toString(), "phenotype.txt.gz").toFile();

			if (phenotypeFile.exists()) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(phenotypeFile))));

				while ((readLine = br.readLine()) != null) {
					String[] readLineFields = readLine.split("\t");
					phenotype.put(readLineFields[0], readLineFields[2]);
				}

				System.out.println("loadHashSource: " + phenotype.size());

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadHashSource() {
		try {

			File sourceFile = Paths.get(path.toString(), "source.txt.gz").toFile();

			if (sourceFile.exists()) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(sourceFile))));

				while ((readLine = br.readLine()) != null) {
					String[] readLineFields = readLine.split("\t");
					if (readLineFields.length == 7) {
						source.put(readLineFields[0], readLineFields[1] + "," + readLineFields[2] + ","
								+ readLineFields[3] + "," + readLineFields[4] + "," + readLineFields[5] + ","
								+ readLineFields[6]);
					} else {
						source.put(readLineFields[0], readLineFields[1] + "," + readLineFields[2] + ","
								+ readLineFields[3] + "," + readLineFields[4] + "," + readLineFields[5] + ", ");
					}
				}

				System.out.println("loadHashSource: " + source.size());

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadHashSeqRegion() {

		try {

			File seqRegionFile = Paths.get(path.toString(), "seq_region.txt.gz").toFile();

			if (seqRegionFile.exists()) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(seqRegionFile))));

				while ((readLine = br.readLine()) != null) {
					String[] readLineFields = readLine.split("\t");
					seqRegion.put(readLineFields[0], readLineFields[1]);
				}

				System.out.println("loadHashSeqRegion: " + seqRegion.size());

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sample() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists sample(" + "sample_id INT ," // INDEX
					+ "name TEXT, " + "size INT, " + "description TEXT," + "display TEXT," + "freqs_from_gts INT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO sample(" + "sample_id," // INDEX
					+ "name, " + "size, " + "description," + "display," + "freqs_from_gts)" + "values (?,?,?,?,?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // sample_id

				if (readLineFields[1].equals("\\N")) {
					ps.setString(2, "");
				} else {
					ps.setString(2, readLineFields[1]); // name
				}

				if (readLineFields[2].equals("\\N")) {
					ps.setInt(3, -1);
				} else {
					ps.setInt(3, Integer.parseInt(readLineFields[2])); // size

				}

				if (readLineFields[3].equals("\\N")) {
					ps.setString(4, "");
				} else {
					ps.setString(4, readLineFields[3]); // description
				}

				if (readLineFields[4].equals("\\N")) {
					ps.setString(5, "");
				} else {
					ps.setString(5, readLineFields[4]); // display
				}

				if (readLineFields[5].equals("\\N")) {
					ps.setInt(6, -1);
				} else {
					ps.setInt(6, Integer.parseInt(readLineFields[5])); // freqs_from_gts
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));
					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch sample: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables.executeUpdate("CREATE INDEX sample_id_sample on sample(sample_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum sample: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void source() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();
			createTables.executeUpdate("CREATE TABLE if not exists source("
					+ "source_id INT ," // INDEX
					+ "name TEXT, " + "version INT, " + "description TEXT," + "url TEXT," + "type TEXT,"
					+ "somatic_status TEXT)");
			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO source("
					+ "source_id," // INDEX
					+ "name, " + "version, " + "description," + "url," + "type," + "somatic_status)"
					+ "values (?,?,?,?,?,?,?)");
			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // source_id

				if (readLineFields[1].equals("\\N")) {
					ps.setString(2, "");
				} else {
					ps.setString(2, readLineFields[1]); // name
				}

				if (readLineFields[2].equals("\\N")) {
					ps.setInt(3, -1);
				} else {
					ps.setInt(3, Integer.parseInt(readLineFields[2])); // version

				}

				if (readLineFields[3].equals("\\N")) {
					ps.setString(4, "");
				} else {
					ps.setString(4, readLineFields[3]); // description
				}

				if (readLineFields[4].equals("\\N")) {
					ps.setString(5, "");
				} else {
					ps.setString(5, readLineFields[4]); // url
				}

				if (readLineFields[5].equals("\\N")) {
					ps.setString(6, "");
				} else {
					ps.setString(6, readLineFields[5]); // type
				}

				if (readLineFields.length > 6) {
					if (readLineFields[6].equals("\\N")) {
						ps.setString(7, "");
					} else {
						ps.setString(7, readLineFields[6]); // somatic_status
					}
				} else {
					ps.setString(7, "");
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;
			}
			tExecBatch = System.nanoTime();
			ps.executeBatch();
			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch source: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables.executeUpdate("CREATE INDEX source_id_source on source(source_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum source: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void seqRegion() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists seq_region(" + "seq_region_id INT ," // index
					+ "name TEXT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO seq_region(" + "seq_region_id ," // index
					+ "name)" + "values (?,?)");

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // source_id

				ps.setString(2, readLineFields[1]); // name

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch seq_region: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables.executeUpdate("CREATE INDEX seq_region_id_seqregion on seq_region(seq_region_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum seq_region: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void study() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists study("
					+ "source_id INT ," // index
					+ "name INT ," + "description TEXT ," + "url TEXT," + "external_reference TEXT,"
					+ "study_type TEXT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO study(" + "source_id," + "name,"
					+ "description," + "url," + "external_reference," + "study_type)" + "values (?,?,?,?,?,?)");

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");
				System.out.println(readLine);

				ps.setInt(1, Integer.parseInt(readLineFields[1])); // source_id

				if (readLineFields[2].equals("\\N")) {
					ps.setString(2, "");
				} else {
					ps.setString(2, readLineFields[2]); // name
				}

				if (readLineFields[3].equals("\\N")) {
					ps.setString(3, "");
				} else {
					ps.setString(3, readLineFields[3]); // description
				}

				if (readLineFields[4].equals("\\N")) {
					ps.setString(4, "");
				} else {
					ps.setString(4, readLineFields[4]); // url
				}

				if (readLineFields[5].equals("\\N")) {
					ps.setString(5, "");
				} else {
					ps.setString(5, readLineFields[5]); // external_reference
				}

				if (readLineFields[6].equals("\\N")) {
					ps.setString(6, "");
				} else {
					ps.setString(6, readLineFields[6]); // study_type
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch study: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables.executeUpdate("CREATE INDEX source_id_study on study(source_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum study: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void phenotype() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists phenotype(" + "phenotype_id TEXT ," // INDEX
					+ "description TEXT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO phenotype(" + "phenotype_id ,"
					+ "description)" + "values (?,?)");

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // phenotype_id

				if (readLineFields[2].equals("\\N")) {
					ps.setString(2, "");
				} else {
					ps.setString(2, readLineFields[2]); // description
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));
			;
			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch phenotype_id: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables.executeUpdate("CREATE INDEX phenotype_id_phenotype on phenotype(phenotype_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum phenotype: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void alleleCode() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists allele_code(" + "allele_code_id INT ," // INDEX
					+ "allele TEXT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO allele_code(" + "allele_code_id ," + "allele)"
					+ "values (?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // allele_code_id

				if (readLineFields[1].equals("\\N")) {
					ps.setString(2, "");
				} else {
					ps.setString(2, readLineFields[1]); // allele
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch allele_code: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables.executeUpdate("CREATE INDEX allele_code_id_allelecode on allele_code(allele_code_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum allele_code: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void genotypeCode() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists genotype_code(" + "genotype_code_id INT ," // INDEX
					+ "allele_code_id INT, " + "haplotype_id INT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO genotype_code(" + "genotype_code_id," // INDEX
					+ "allele_code_id, " + "haplotype_id)" + "values (?,?,?)");

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // genotype_code_id

				ps.setInt(2, Integer.parseInt(readLineFields[1])); // allele_code_id

				ps.setInt(3, Integer.parseInt(readLineFields[2])); // haplotype_id

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch genotype_code: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX genotype_code_id_genotypecode on genotype_code(genotype_code_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum genotype_code: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void populationGenotype() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists population_genotype("
					+ "variation_id INT ," // INDEX
					+ "subsnp_id INT, " + "genotype_code_id INT, " + "frequency REAL," + "sample_id INT,"
					+ "count INT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO population_genotype("
					+ "variation_id," // INDEX
					+ "subsnp_id, " + "genotype_code_id, " + "frequency," + "sample_id," + "count)"
					+ "values (?,?,?,?,?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				// for (String string : readLineFields) {
				// System.out.println(string);
				// }

				ps.setInt(1, Integer.parseInt(readLineFields[1])); // variation_id

				if (readLineFields[2].equals("\\N")) {
					ps.setInt(2, -1);
				} else {
					ps.setInt(2, Integer.parseInt(readLineFields[2])); // subsnp_id
				}

				if (readLineFields[3].equals("\\N")) {
					ps.setInt(3, -1);
				} else {
					ps.setInt(3, Integer.parseInt(readLineFields[3])); // genotype_code_id

				}

				if (readLineFields[4].equals("\\N")) {
					ps.setFloat(4, -1);
				} else {
					ps.setFloat(4, Float.parseFloat(readLineFields[4])); // frequency
				}

				if (readLineFields[5].equals("\\N")) {
					ps.setFloat(5, -1);
				} else {
					ps.setFloat(5, Float.parseFloat(readLineFields[5])); // sample_id
				}

				if (readLineFields[6].equals("\\N")) {
					ps.setInt(6, -1);
				} else {
					ps.setInt(6, Integer.parseInt(readLineFields[6])); // count
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch population_genotype: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX variation_id_populationgenotype on population_genotype(variation_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum population_genotype: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void motifFeatureVariation() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists motif_feature_variation("
					+ "variation_feature_id INT ," // INDEX
					+ "feature_stable_id TEXT," + "motif_feature_id INT, " + "consequence_types TEXT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO motif_feature_variation("
					+ "variation_feature_id," // INDEX
					+ "feature_stable_id," + "motif_feature_id, " + "consequence_types)" + "values (?,?,?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[1])); // variation_id

				if (readLineFields[2].equals("\\N")) {
					ps.setString(2, "");
				} else {
					ps.setString(2, readLineFields[2]); // associated_gene
				}

				ps.setInt(3, Integer.parseInt(readLineFields[3])); // study_id

				if (readLineFields[6].equals("\\N")) {
					ps.setString(4, "");
				} else {
					ps.setString(4, readLineFields[6]); // associated_gene
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch motif_feature_variation: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX variation_feature_id_motiffeaturevariation on motif_feature_variation(variation_feature_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum motif_feature_variation: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void allele() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {

			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists allele(" 
					+ "variation_id INT ,"
					+ "allele_code_id INT, " 
					+ "sample_id INT, " 
					+ "frequency REAL, " 
					+ "count INT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO allele(" + "variation_id,"
					+ "allele_code_id, " + "sample_id, " + "frequency, " + "count)" + "values (?,?,?,?,?)");

			while ((readLine = br.readLine()) != null) {

				String[] readLineFields = readLine.split("\t");

				if (!readLineFields[4].equals("\\N")) {

					ps.setInt(1, Integer.parseInt(readLineFields[1])); // variation_id
					ps.setInt(2, Integer.parseInt(readLineFields[3])); // allele_code_id
					ps.setInt(3, Integer.parseInt(readLineFields[4])); // sample_id

					if (readLineFields[5].equals("\\N")) {
						ps.setFloat(4, Float.parseFloat("-1"));
					} else {
						ps.setFloat(4, Float.parseFloat(readLineFields[5])); // frequency
					}
					if (readLineFields[6].equals("\\N")) {
						ps.setInt(5, -1);
					} else {
						ps.setInt(5, Integer.parseInt(readLineFields[6])); // count
					}

					if (contador % LIMITROWS == 0 && contador != 0) {
						tExecBatch = System.nanoTime();

						ps.executeBatch();

						tExecBatchFin = System.nanoTime();
						tExecBatchTotal = tExecBatchFin - tExecBatch;
						System.out.println(contador + " ---> Time Batch: "
								+ TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

						acum += tExecBatchTotal;
						conndb1.commit();
					}

					ps.addBatch();
					contador++;

				}
			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();
			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch allele: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables.executeUpdate("CREATE INDEX variation_id_allele on allele(variation_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum allele: " + TimeUnit.NANOSECONDS.toSeconds(acum));
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}


	private void sql_motifFeature(){

	}
	private void motifFeature() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists motif_feature("
					+ "motif_feature_id INT ," // INDEX
					+ "seq_region_id INT ," 
					+ "seq_region_start INT, " 
					+ "seq_region_end INT, "
					+ "seq_region_strand INT, " 
					+ "display_label TEXT, " 
					+ "score REAL)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO motif_feature("
					+ "motif_feature_id," // INDEX
					+ "seq_region_id," + "seq_region_start, " + "seq_region_end, " + "seq_region_strand, "
					+ "display_label, " + "score)" + "values (?,?,?,?,?,?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // motif_feature_id
				ps.setInt(2, Integer.parseInt(readLineFields[2])); // seq_region_id

				ps.setInt(3, Integer.parseInt(readLineFields[3])); // seq_region_start
				ps.setInt(4, Integer.parseInt(readLineFields[4])); // seq_region_end
				ps.setInt(5, Integer.parseInt(readLineFields[5])); // seq_region_strands

				if (readLineFields[6].equals("\\N")) {
					ps.setString(6, "");
				} else {
					ps.setString(6, readLineFields[6]); // associated_gene
				}

				if (readLineFields[7].equals("\\N")) {
					ps.setFloat(7, Float.parseFloat("-1"));
				} else {
					ps.setFloat(7, Float.parseFloat(readLineFields[7])); // associated_gene
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch motif_feature: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX motif_feature_id_motiffeature on motif_feature(motif_feature_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum motif_feature: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void variationSynonim() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists variation_synonim(" 
					+ "variation_id INT ," // INDEX
					+ "source_id INT, " 
					+ "name TEXT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO variation_synonim(" 
					+ "variation_id ,"
					+ "source_id, " 
					+ "name)" 
					+ "values (?,?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[1])); // variation_id
				ps.setInt(2, Integer.parseInt(readLineFields[3])); // study_id

				if (readLineFields[4].equals("\\N")) {
					ps.setString(3, "");
				} else {
					ps.setString(3, readLineFields[4]); // associated_gene
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch variation_synonim: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX variation_id_variationsynonim on variation_synonim(variation_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum variation_synonim: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void variationAnnotation() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists variation_annotation("
					+ "variation_id INT ," // INDEX
					+ "phenotype_id INT, " 
					+ "study_id INT, " 
					+ "associated_gene TEXT, "
					+ "associated_variant_risk_allele TEXT, " 
					+ "variation_names TEXT, "
					+ "risk_allele_freq_in_controls REAL," 
					+ "p_value REAL)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO variation_annotation(" 
					+ "variation_id ,"
					+ "phenotype_id, " 
					+ "study_id, " 
					+ "associated_gene, " 
					+ "associated_variant_risk_allele, "
					+ "variation_names, " 
					+ "risk_allele_freq_in_controls," 
					+ "p_value)" 
					+ "values (?,?,?,?,?,?,?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[1])); // variation_id
				ps.setInt(2, Integer.parseInt(readLineFields[2])); // phenotype_id
				ps.setInt(3, Integer.parseInt(readLineFields[3])); // study_id

				if (readLineFields[4].equals("\\N")) {
					ps.setString(4, "");
				} else {
					ps.setString(4, readLineFields[4]); // associated_gene
				}

				if (readLineFields[5].equals("\\N")) {
					ps.setString(5, "");
				} else {
					ps.setString(5, readLineFields[5]); // associated_variant_risk_allele
				}

				if (readLineFields[6].equals("\\N")) {
					ps.setString(6, "");
				} else {
					ps.setString(6, readLineFields[6]); // variation_names
				}

				if (readLineFields[7].equals("\\N")) {
					ps.setFloat(7, -1);
				} else {
					ps.setFloat(7, Float.parseFloat(readLineFields[7])); // risk_allele_freq_in_controls
				}

				if (readLineFields[8].equals("\\N")) {
					ps.setFloat(8, -1);
				} else {
					ps.setFloat(8, Float.parseFloat(readLineFields[8])); // p_value
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch variation_annotation: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX variation_id_variationannotation on variation_annotation(variation_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum variation_annotation: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void transcriptVariation() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;

		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists transcript_variation("
					+ "variation_feature_id INT ," // INDEX
					+ "feature_stable_id TEXT, " 
					+ "allele_string TEXT, " 
					+ "somatic TEXT, "
					+ "consequence_types TEXT, " 
					+ "cds_start INT, " 
					+ "cds_end INT, " 
					+ "cdna_start INT,"
					+ "cdna_end INT," 
					+ "translation_start INT," 
					+ "translation_end INT,"
					+ "distance_to_transcript INT, " 
					+ "codon_allele_string TEXT," 
					+ "pep_allele_string TEXT, "
					+ "hgvs_genomic TEXT, " 
					+ "hgvs_transcript TEXT, " 
					+ "hgvs_protein TEXT,"
					+ "polyphen_prediction TEXT," 
					+ "polyphen_score REAL," 
					+ "sift_prediction TEXT, "
					+ "sift_score REAL)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO transcript_variation("
					+ "variation_feature_id," + "feature_stable_id," + "allele_string," + "somatic,"
					+ "consequence_types," + "cds_start, " + "cds_end , " + "cdna_start ," + "cdna_end ,"
					+ "translation_start ," + "translation_end ," + "distance_to_transcript , "
					+ "codon_allele_string ," + "pep_allele_string , " + "hgvs_genomic , " + "hgvs_transcript , "
					+ "hgvs_protein ," + "polyphen_prediction ," + "polyphen_score ," + "sift_prediction , "
					+ "sift_score)" + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			//

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[1])); // variation_feature_id

				if (readLineFields[2].equals("\\N")) {
					ps.setString(2, "");
				} else {
					ps.setString(2, readLineFields[2]); // feature_stable_id
				}

				if (readLineFields[3].equals("\\N")) {
					ps.setString(3, "");
				} else {
					ps.setString(3, readLineFields[3]); // allele_string
				}

				if (readLineFields[4].equals("\\N")) {
					ps.setString(4, "");
				} else {
					ps.setString(4, readLineFields[4]); // somatic
				}

				if (readLineFields[5].equals("\\N")) {
					ps.setString(5, "");
				} else {
					ps.setString(5, readLineFields[5]); // consequence_types
				}

				if (readLineFields[6].equals("\\N")) {
					ps.setInt(6, -1);
				} else {
					ps.setInt(6, Integer.parseInt(readLineFields[6])); // cds_start
				}

				if (readLineFields[7].equals("\\N")) {
					ps.setInt(7, -1);
				} else {
					ps.setInt(7, Integer.parseInt(readLineFields[7])); // cds_end

				}

				if (readLineFields[8].equals("\\N")) {
					ps.setInt(8, -1);
				} else {
					ps.setInt(8, Integer.parseInt(readLineFields[8])); // cdna_start
				}

				if (readLineFields[9].equals("\\N")) {
					ps.setInt(9, -1);
				} else {
					ps.setInt(9, Integer.parseInt(readLineFields[9])); // cdna_end
				}

				if (readLineFields[10].equals("\\N")) {
					ps.setInt(10, -1);
				} else {
					ps.setInt(10, Integer.parseInt(readLineFields[10])); // translation_start
				}

				if (readLineFields[11].equals("\\N")) {
					ps.setInt(11, -1);
				} else {
					ps.setInt(11, Integer.parseInt(readLineFields[11])); // translation_end
				}

				if (readLineFields[12].equals("\\N")) {
					ps.setInt(12, -1);
				} else {
					ps.setInt(12, Integer.parseInt(readLineFields[12])); // distance_to_transcript
				}

				if (readLineFields[13].equals("\\N")) {
					ps.setString(13, "");
				} else {
					ps.setString(13, readLineFields[13]); // codon_allele_string
				}

				if (readLineFields[14].equals("\\N")) {
					ps.setString(14, "");
				} else {
					ps.setString(14, readLineFields[14]); // pep_allele_string
				}

				if (readLineFields[15].equals("\\N")) {
					ps.setString(15, "");
				} else {
					ps.setString(15, readLineFields[15]); // hgvs_genomic
				}

				if (readLineFields[16].equals("\\N")) {
					ps.setString(16, "");
				} else {
					ps.setString(16, readLineFields[16]); // hgvs_transcript
				}

				if (readLineFields[17].equals("\\N")) {
					ps.setString(17, "");
				} else {
					ps.setString(17, readLineFields[17]); // hgvs_protein
				}

				if (readLineFields[18].equals("\\N")) {
					ps.setString(18, "");
				} else {
					ps.setString(18, readLineFields[18]); // polyphen_prediction
				}

				if (readLineFields[19].equals("\\N")) {
					ps.setFloat(19, -1);
				} else {
					ps.setFloat(19, Float.parseFloat(readLineFields[19])); // polyphen_score
				}

				ps.setString(20, readLineFields[20]); // sift_prediction

				if (readLineFields[21].equals("\\N")) {
					ps.setFloat(21, -1);
				} else {
					ps.setFloat(21, Float.parseFloat(readLineFields[21])); // sift_score
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch transcript_variation: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX variation_feature_id_transcriptvariation on transcript_variation(variation_feature_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum transcript_variation: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void variationFeature() {

		int contador = 0;
		long acum = 0;
		long start;
		long tExecBatch;
		long tExecBatchFin;
		long tExecBatchTotal;
		long tExecIndex;
		long tExecIndexFin;
		long tExecIndexTotal;
		try {
			Statement createTables = conndb1.createStatement();

			createTables.executeUpdate("CREATE TABLE if not exists variation_feature(" 
					+ "variation_feature_id INT, "
					+ "seq_region_id TEXT, " 
					+ "seq_region_start INT, "
					+ "seq_region_end INT, "
					+ "seq_region_strand INT, "
					+ "variation_id INT, " // INDEX
					+ "allele_string TEXT," 
					+ "variation_name TEXT," 
					+ "map_weight TEXT," 
					+ "validation_status TEXT, "
					+ "consequence_types TEXT," 
					+ "somatic INT, " 
					+ "minor_allele TEXT, " 
					+ "minor_allele_freq REAL, "
					+ "minor_allele_count INT)");

			PreparedStatement ps = conndb1.prepareStatement("INSERT INTO variation_feature(" + "variation_feature_id,"
					+ "seq_region_id," + "seq_region_start," + "seq_region_end," + "seq_region_strand,"
					+ "variation_id," + "allele_string," + "variation_name," + "map_weight," + "validation_status,"
					+ "consequence_types," + "somatic," + "minor_allele," + "minor_allele_freq,"
					+ "minor_allele_count)" + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			start = System.nanoTime();
			while ((readLine = br.readLine()) != null) {
				String[] readLineFields = readLine.split("\t");

				ps.setInt(1, Integer.parseInt(readLineFields[0])); // variation_feature_id
				ps.setString(2, seqRegion.get(readLineFields[1])); // seq_region_id
				ps.setInt(3, Integer.parseInt(readLineFields[2])); // seq_region_start
				ps.setInt(4, Integer.parseInt(readLineFields[3])); // seq_region_end
				ps.setInt(5, Integer.parseInt(readLineFields[4])); // seq_region_strand
				ps.setInt(6, Integer.parseInt(readLineFields[5])); // variation_id
				ps.setString(7, readLineFields[6]); // allele_string
				ps.setString(8, readLineFields[7]); // variation_name
				ps.setString(9, readLineFields[8]); // map_weight

				if (readLineFields[11].equals("\\N")) {
					ps.setString(10, "");
				} else {
					ps.setString(10, readLineFields[11]); // validation_status
				}
				if (readLineFields[12].equals("\\N")) {
					ps.setString(11, "");
				} else {
					ps.setString(11, readLineFields[12]); // consequence_types
				}

				if (readLineFields[15].equals("\\N")) {
					ps.setString(12, "");
				} else {
					ps.setString(12, readLineFields[16]); // somatic
				}

				if (readLineFields[16].equals("\\N")) {
					ps.setString(13, "");
				} else {
					ps.setString(13, readLineFields[16]); // minor_allele
				}

				if (readLineFields[17].equals("\\N")) {
					ps.setFloat(14, Float.parseFloat("-1"));
				} else {
					ps.setFloat(14, Float.parseFloat(readLineFields[17])); // minor_allele_freq
				}
				if (readLineFields[18].equals("\\N")) {
					ps.setInt(15, -1);
				} else {
					ps.setInt(15, Integer.parseInt(readLineFields[18])); // minor_allele_count
				}

				if (contador % LIMITROWS == 0 && contador != 0) {
					tExecBatch = System.nanoTime();

					ps.executeBatch();

					tExecBatchFin = System.nanoTime();
					tExecBatchTotal = tExecBatchFin - tExecBatch;
					System.out
					.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

					acum += tExecBatchTotal;
					conndb1.commit();
				}

				ps.addBatch();
				contador++;

			}

			tExecBatch = System.nanoTime();

			ps.executeBatch();

			tExecBatchFin = System.nanoTime();
			tExecBatchTotal = tExecBatchFin - tExecBatch;
			System.out.println(contador + " ---> Time Batch: " + TimeUnit.NANOSECONDS.toMillis(tExecBatchTotal));

			acum += tExecBatchTotal;
			System.out.println("Time TotalBatch Variation_feature: " + TimeUnit.NANOSECONDS.toSeconds(acum));
			conndb1.commit();
			tExecIndex = System.nanoTime();
			if (indexed)
				createTables
				.executeUpdate("CREATE INDEX variation_id_variationfeature on variation_feature(variation_id)");
			tExecIndexFin = System.nanoTime();
			tExecIndexTotal = tExecIndexFin - tExecIndex;
			acum += tExecIndexTotal;
			System.out.println("Time Index: " + TimeUnit.NANOSECONDS.toSeconds(tExecIndexTotal));
			System.out.println("Time acum Variation_feature: " + TimeUnit.NANOSECONDS.toSeconds(acum));

		} catch (SQLException | NumberFormatException | IOException e) {
			e.printStackTrace();
		}

	}

	private void CoreVariationParser() {
		String variation_id = null;
		String readline = null;
		try {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(Paths.get(
					path.toString(), "variation.txt.gz").toFile()))));

			while ((readline = br.readLine()) != null) {
				variation_id = readline.split("\t")[0];
				System.out.println(variation_id);
				this.sql_variation_feature(variation_id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sql_population_genotype(String variation_id){
		int subsnp_id;
		int genotype_code_id;
		double frequency;
		int sample_id;
		int count;

		try {
			Statement pst = conndb1.createStatement();
			ResultSet rs = pst.executeQuery("select * from population_genotype where variation_id='" + Integer.parseInt(variation_id) + "'");
			while (rs.next()) {
				System.out.println(subsnp_id = rs.getInt("subsnp_id"));
				System.out.println(genotype_code_id = rs.getInt("genotype_code_id"));
				System.out.println(frequency = rs.getDouble("frequency"));
				System.out.println(sample_id = rs.getInt("sample_id"));
				System.out.println(count = rs.getInt("count"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void sql_variation_feature(String variation_id) {
		System.out.println("Consultando variation_feature: " + variation_id);
		int variation_feature_id;
		String seq_region_id;
		int seq_region_start;
		int seq_region_end;
		int seq_region_strand;
		String allele_string;
		String variation_name;
		String map_weight;
		String validation_status;
		String consequence_types;
		int somatic;
		String minor_allele;
		double minor_allele_freq;
		int minor_allele_count;

		try {
			Statement pst = conndb1.createStatement();
			ResultSet rs = pst.executeQuery("select * from variation_feature where variation_id='" + Integer.parseInt(variation_id) + "'");
			while (rs.next()) {
				System.out.println(variation_feature_id = rs.getInt("variation_feature_id"));
				System.out.println(seq_region_id = rs.getString("seq_region_id"));
				System.out.println(seq_region_start = rs.getInt("seq_region_start"));
				System.out.println(seq_region_end = rs.getInt("seq_region_end"));
				System.out.println(seq_region_strand = rs.getInt("seq_region_strand"));
				System.out.println(allele_string = rs.getString("allele_string"));
				System.out.println(variation_name = rs.getString("variation_name"));
				System.out.println(map_weight = rs.getString("map_weight"));
				System.out.println(validation_status = rs.getString("validation_status"));
				System.out.println(consequence_types = rs.getString("consequence_types"));
				System.out.println(somatic = rs.getInt("somatic"));
				System.out.println(minor_allele = rs.getString("minor_allele"));
				System.out.println(minor_allele_freq = rs.getDouble("minor_allele_freq"));
				System.out.println(minor_allele_count = rs.getInt("minor_allele_count"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void sql_genotype_code(String genotype_code_id){
		int allele_code_id;
		int haplotype_id;

		try {
			Statement pst = conndb1.createStatement();
			ResultSet rs = pst.executeQuery("select * from genotype_code where genotype_code_id='" + Integer.parseInt(genotype_code_id) + "'");
			while (rs.next()) {
				System.out.println(allele_code_id = rs.getInt("allele_code_id"));
				System.out.println(haplotype_id = rs.getInt("haplotype_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void sql_allele_code(String allele_code_id){
		int allele;

		try {
			Statement pst = conndb1.createStatement();
			ResultSet rs = pst.executeQuery("select * from allele_code where allele_code_id='" + Integer.parseInt(allele_code_id) + "'");
			while (rs.next()) {
				System.out.println(allele = rs.getInt("allele"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void sql_variation_synonim(String variation_id){
		int source_id; 
		String name;

		try {
			Statement pst = conndb1.createStatement();
			ResultSet rs = pst.executeQuery("select * from variation_synonim where variation_id='" + Integer.parseInt(variation_id) + "'");
			while (rs.next()) {
				System.out.println(source_id = rs.getInt("source_id"));
				System.out.println(name = rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



}
