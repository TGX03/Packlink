package de.tgx03.packlink;

import de.tgx03.packlink.api.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * A class that calculates the prices for all locations for given parcels.
 */
public final class TableCreator {

	/**
	 * The random generator for selecting a zip by random.
	 */
	private static final Random RANDOM = new Random();
	/**
	 * The tax rate to apply.
	 */
	private static final double tax = 0.2;

	/**
	 * All the parcels to calculate the prices for.
	 */
	private final Parcel[] parcels;
	/**
	 * The output file.
	 */
	private final File out;
	/**
	 * The source of the parcels.
	 */
	private final Address sourceAddress;

	/**
	 * Creates a new table creator.
	 *
	 * @param fileOut       The output file.
	 * @param sourceAddress The source of the parcels.
	 * @param parcels       All the parcels to ship.
	 */
	public TableCreator(String fileOut, Address sourceAddress, Parcel... parcels) {
		this.parcels = parcels;
		this.out = new File(fileOut);
		this.sourceAddress = sourceAddress;
	}

	/**
	 * @param args 1 - API Key
	 *             2 - target File
	 *             3 - Source address country
	 *             4 - Source address zip
	 *             all following - packages written as weight-length-width-height
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		API.setApiKey(args[0]);
		API.initializeCountries();
		API.initializePostalCodes();
		Service.setTax(tax);
		Parcel[] parcels = new Parcel[args.length - 4];
		for (int i = 0; i < parcels.length; i++) {
			parcels[i] = convertParcel(args[i + 4]);
		}
		Address sourceAddress = new Address(Country.getCountry(args[2]), args[3]);
		TableCreator creator = new TableCreator(args[1], sourceAddress, parcels);
		creator.run();
	}

	/**
	 * Generates a parcel from its string.
	 *
	 * @param parcelDescriptor The string representing the parcel.
	 * @return The generated parcel object.
	 */
	private static Parcel convertParcel(String parcelDescriptor) {
		String[] split = parcelDescriptor.split("-");
		if (split.length != 4) throw new IllegalArgumentException("A parcel has exactly 4 arguments");
		short length = Short.parseShort(split[1]);
		short width = Short.parseShort(split[2]);
		short height = Short.parseShort(split[3]);
		return new Parcel(split[0], height, length, width);
	}

	/**
	 * Start the creator
	 *
	 * @throws InterruptedException I dunno.
	 * @throws IOException          Probably when an error during transmission or writing occurs.
	 */
	public void run() throws InterruptedException, IOException {
		Workbook workbook = new XSSFWorkbook();
		if (parcels.length == 1) {
			Sheet sheet = workbook.createSheet();
			parcelToSheet(parcels[0], sheet);
		} else {
			final CountDownLatch latch = new CountDownLatch(parcels.length);
			for (Parcel parcel : parcels) {
				Sheet sheet = workbook.createSheet(parcel.toString());
				new Thread(() -> {
					try {
						parcelToSheet(parcel, sheet);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						latch.countDown();
					}
				}).start();
			}
			latch.await();
		}
		FileOutputStream output = new FileOutputStream(out);
		workbook.write(output);
		output.close();
	}

	/**
	 * Query a parcel and write the result to the given sheet.
	 *
	 * @param parcel The parcel to query.
	 * @param sheet  The sheet to write to.
	 * @throws IOException I dunno.
	 */
	private void parcelToSheet(Parcel parcel, Sheet sheet) throws IOException {
		Collection<Country> countries = Country.getAllCountries();
		Row firstRow = sheet.createRow(0);

		firstRow.createCell(0).setCellValue("Country");
		firstRow.createCell(1).setCellValue("Standard service");
		firstRow.createCell(2).setCellValue("Standard price");
		firstRow.createCell(3).setCellValue("Express Service");
		firstRow.createCell(4).setCellValue("Express price");

		int currentRow = 0;
		for (Country country : countries) {
			String[] zips = country.getPostalCodes().toArray(new String[0]);
			if (zips.length > 0) {
				int position = RANDOM.nextInt(zips.length);
				String zip = zips[position];
				Address target = new Address(country, zip);
				Service[] services = API.queryServices(sourceAddress, target, parcel);
				if (services.length > 0) {
					currentRow++;
					Row row = sheet.createRow(currentRow);
					row.createCell(0).setCellValue(country.name);
					Service cheapest = getCheapest(services);
					Service express = getExpress(services);
					if (cheapest != null && cheapest != express) {
						row.createCell(1).setCellValue(cheapest.toString());
						row.createCell(2).setCellValue(cheapest.getPriceWithTax());
					}
					if (express != null) {
						row.createCell(3).setCellValue(express.toString());
						row.createCell(4).setCellValue(express.getPriceWithTax());
					}
				}
			}
		}
	}

	@Nullable
	private Service getExpress(@NotNull Service[] services) throws IllegalArgumentException {
		for (Service service : services) {
			if (service.serviceType == Service.Type.EXPRESS && !service.deliveryToParcelshop && !service.serviceName.contains("Dokumente")) return service;
		}
		return null;
	}

	@Nullable
	private Service getCheapest(@NotNull Service[] services) throws IllegalArgumentException {
		for (Service service : services) {
			if (!service.deliveryToParcelshop && !service.serviceName.contains("Dokumente")) return service;
		}
		return null;
	}
}