package de.tgx03.packlink.api;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A class representing a country with its name, ISO code and valid postal codes.
 */
public class Country {

	/**
	 * How the name of a country is stored in JSON.
	 */
	private static final String COUNTRY_NAME = "name";
	/**
	 * How the ISO code of a country is stored in JSON.
	 */
	private static final String ISO_CODE = "isoCode";
	/**
	 * The value in JSON determining whether this country actually has some sort of postal code system.
	 */
	private static final String zips = "hasPostalCodes";
	/**
	 * A HashMap holding all registered countries and maps them to their ISO code.
	 */
	private static final HashMap<String, Country> countries = new HashMap<>();

	/**
	 * The full name of this country.
	 */
	public final String name;
	/**
	 * The ISO code of this country.
	 */
	public final String iso;
	/**
	 * Whether this country uses postal codes.
	 */
	public final boolean hasPostalCodes;
	/**
	 * A set of all the postal codes known for this country.
	 * If the country doesn't use postal codes. It holds valid places.
	 */
	private final HashSet<String> validCodes = new HashSet<>();

	/**
	 * Create a new country from its JSON object.
	 *
	 * @param query The JSON string holding the information for this country.
	 */
	private Country(@NotNull JSONObject query) {
		this.name = query.getString(COUNTRY_NAME).intern();
		this.iso = query.getString(ISO_CODE).intern();
		this.hasPostalCodes = query.getBoolean(zips);
	}

	/**
	 * Creates a new country with the information prefilled.
	 *
	 * @param name The full name of the country.
	 * @param iso  The ISO code of this country.
	 * @param zips Whether this country has a ZIP system.
	 */
	private Country(@NotNull String name, @NotNull String iso, boolean zips) {
		this.name = name.intern();
		this.iso = iso.intern();
		this.hasPostalCodes = zips;
	}

	/**
	 * Get a country by its JSON representation.
	 * If an object with the same ISO code already exists, it is returned.
	 * Otherwise a new country gets created and stored in the database.
	 * Keep in mind only the ISO code gets used for checking whether the country already exists, meaning if one ISO code gets used for multiple countries,
	 * for example for special regions in countries, they are treated as the same country.
	 *
	 * @param query The JSON string representing the country.
	 * @return The found or created country object.
	 */
	@NotNull
	public static Country getCountry(@NotNull JSONObject query) {
		String iso = query.getString(ISO_CODE);
		if (countries.containsKey(iso)) return countries.get(iso);
		else {
			Country country = new Country(query);
			countries.put(iso, country);
			return country;
		}
	}

	/**
	 * Tries to find a country by its ISO code. If such a country doesn't exist, an error is thrown.
	 *
	 * @param iso
	 * @return
	 */
	@NotNull
	public static Country getCountry(@NotNull String iso) {
		if (countries.containsKey(iso)) return countries.get(iso);
		else throw new IllegalArgumentException("No country with this code exists");
	}

	/**
	 * Gets all countries currently registered.
	 *
	 * @return A collection containing all the countries.
	 */
	@NotNull
	public static Collection<Country> getAllCountries() {
		return countries.values();
	}

	/**
	 * Add a new postal code as valid for this country.
	 *
	 * @param code The code to add.
	 */
	public void addPostalCode(@NotNull String code) {
		if (!"".equals(code))validCodes.add(code);
	}

	/**
	 * Get all postal codes currently registered with this country.
	 *
	 * @return The postal codes.
	 */
	public Set<String> getPostalCodes() {
		return validCodes;
	}

	/**
	 * Verifies whether a given postal code is valid for this country.
	 *
	 * @param code The code to check.
	 * @return Whether it's valid for this country.
	 */
	public boolean validPostalCode(String code) {
		return validCodes.contains(code);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Country c) {
			return this.iso == c.iso;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return iso.hashCode();
	}
}
