package de.tgx03.packlink.api;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * The class used for communicating with the Packlink API.
 */
public final class API {

	/**
	 * The basic URL of the API.
	 */
	private static final String API = "https://api.packlink.com/v1";
	/**
	 * The URL extension when querying services.
	 */
	private static final String CARRIERS = "/services?";
	/**
	 * The URL extension when requesting all the valid countries.
	 */
	private static final String COUNTRIES = "/locations/postalzones/origins";
	/**
	 * The URL extension when requesting all the postal codes for a specific country.
	 */
	private static final String POSTAL_CODES = "/locations/postalcodes/country/";
	/**
	 * The requested language for any names.
	 */
	private static final String LANGUAGE = "?language=en_US";

	/**
	 * The flag specifying a zip code in JSON.
	 */
	private static final String ZIP_CODE = "zipcode";

	/**
	 * The API-key to be used for requests.
	 */
	private static String API_KEY;

	/**
	 * Makes no sense to instantiate this class.
	 *
	 * @throws IllegalAccessException No!
	 */
	private API() throws IllegalAccessException {
		throw new IllegalAccessException("Fuck off");
	}

	/**
	 * Set the API key to be used for requests.
	 *
	 * @param key Your API key.
	 */
	public static void setApiKey(@NotNull String key) {
		API_KEY = key;
	}

	/**
	 * Get all available service for a parcel to be shipped.
	 *
	 * @param source  The source address of the parcel.
	 * @param target  The target address of the parcel.
	 * @param parcels All the parcels to be included in this shipment.
	 * @return All the available services for this shipment.
	 * @throws IOException Probably one of your arguments was invalid, but maybe something else went wrong, I dunno.
	 */
	public static Service[] queryServices(Address source, Address target, Parcel... parcels) throws IOException {
		JSONArray arr = new JSONArray(queryURL(createServicesURL(source, target, parcels)));
		Service[] result = new Service[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			result[i] = new Service(arr.getJSONObject(i));
		}
		return result;
	}

	/**
	 * Creates the URL for a service request.
	 *
	 * @param source  The source address.
	 * @param target  The target address.
	 * @param parcels All the parcels to be included.
	 * @return The URL string to be used.
	 */
	private static String createServicesURL(Address source, Address target, Parcel... parcels) {
		if (parcels.length == 0) throw new IllegalArgumentException("At least one parcel required");
		StringBuilder builder = new StringBuilder(250);
		builder.append(API);
		builder.append(CARRIERS);
		builder.append(source.toSourceAddress());
		builder.append("&").append(target.toDestinationAddress());
		for (int i = 0; i < parcels.length; i++) {
			builder.append("&").append(parcels[i].toHTTPRequest(i));
		}
		return builder.toString();
	}

	/**
	 * Get all countries from the API and put them in the database.
	 *
	 * @throws IOException Something went wrong during communication with the API. Read the error I guess.
	 */
	public static void initializeCountries() throws IOException {
		JSONArray arr = new JSONArray(queryURL(API + COUNTRIES + LANGUAGE));
		for (int i = 0; i < arr.length(); i++) {
			Country.getCountry(arr.getJSONObject(i));
		}
	}

	/**
	 * Get all the valid postal countries for all the known countries.
	 *
	 * @throws IOException I dunno, look at the message. Multiple such exceptions may have occurred, however only the first one gets thrown.
	 */
	public static void initializePostalCodes() throws IOException {
		ExceptionHolder exception = new ExceptionHolder();  // To store any exception that may occur in any of the threads. Only the first one is saved.
		Collection<Country> countries = Country.getAllCountries();
		CountDownLatch latch = new CountDownLatch(countries.size());
		for (Country country : countries) {

			// Gets done in multiple threads as that speeds it up massively.
			new Thread(() -> {
				String url = API + POSTAL_CODES + country.iso + LANGUAGE + "&q=";   // Don't ask me why the q is required.
				try {
					JSONArray arr = new JSONArray(queryURL(url));
					for (int i = 0; i < arr.length(); i++) {
						country.addPostalCode(arr.getJSONObject(i).getString(ZIP_CODE));
					}
				} catch (IOException e) {
					if (exception.exception == null) exception.exception = e;
				} finally {
					latch.countDown();
				}
			}).start();


			// Wait for the threads to finish
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (exception.exception != null) throw exception.exception; // Throw the first exception that occurred.
		}
	}

	/**
	 * Queries a specified URL and gives back the result as a string.
	 *
	 * @param url The URL to send to the API.
	 * @return The string of the result.
	 * @throws IOException Look at the message, no idea when this gets thrown.
	 */
	@NotNull
	private static String queryURL(@NotNull String url) throws IOException {
		StringBuilder builder = new StringBuilder(25000);
		HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("Authorization", API_KEY);
		connection.setRequestMethod("GET");
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String next = reader.readLine();
		do {
			builder.append(next);
		} while ((next = reader.readLine()) != null);
		return builder.toString();
	}

	/**
	 * A class only used for transporting an exception from another thread.
	 */
	private static class ExceptionHolder {

		/**
		 * The hopefully not existing exception.
		 */
		private IOException exception;

	}
}
