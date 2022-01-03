package de.tgx03.packlink.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A class representing a basic address containing the country and zip code.
 * Doesn't hold an actual street address.
 */
public class Address {

	/**
	 * The string to indicate the following string specifies the source country to the API.
	 */
	private static final String FROM_COUNTRY = URLEncoder.encode("from[country]", StandardCharsets.UTF_8);
	/**
	 * The string to indicate the following string specifies the source zip to the API.
	 */
	private static final String FROM_ZIP = URLEncoder.encode("from[zip]", StandardCharsets.UTF_8);
	/**
	 * The string to indicate the following string specifies the target country to the API.
	 */
	private static final String TO_COUNTRY = URLEncoder.encode("to[country]", StandardCharsets.UTF_8);
	/**
	 * The string to indicate the following string specifies the target zip to the API.
	 */
	private static final String TO_ZIP = URLEncoder.encode("to[zip]", StandardCharsets.UTF_8);

	/**
	 * The country of this address.
	 */
	public final Country country;
	/**
	 * The zip of this address.
	 */
	public final String zip;

	/**
	 * Create a new address with a given country and zip code.
	 *
	 * @param country
	 * @param zip
	 */
	public Address(Country country, String zip) {
		this.country = country;
		this.zip = zip;
	}

	/**
	 * Converts this address to a string which in a web address gets used as the source address.
	 *
	 * @return The API-fitting string for the source address.
	 */
	protected String toSourceAddress() {
		return FROM_COUNTRY + "=" + country.iso + "&" + FROM_ZIP + "=" + URLEncoder.encode(zip, StandardCharsets.UTF_8);
	}

	/**
	 * Converts this address to a string which in a web address gets used as the target address.
	 *
	 * @return The API-fitting string for the target address.
	 */
	protected String toDestinationAddress() {
		return TO_COUNTRY + "=" + country.iso + "&" + TO_ZIP + "=" + URLEncoder.encode(zip, StandardCharsets.UTF_8);
	}

	/**
	 * Determines whether the postal code of this address in the country of this address.
	 *
	 * @return Whether this is a known address.
	 */
	public boolean isValidAddress() {
		return country.validPostalCode(zip);
	}

	@Override
	public String toString() {
		return country + "-" + zip;
	}

	@Override
	public int hashCode() {
		return Objects.hash(country, zip);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Address a) {
			return a.country == this.country && this.zip.equals(a.zip);
		}
		return false;
	}
}
