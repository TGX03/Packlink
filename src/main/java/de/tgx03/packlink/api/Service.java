package de.tgx03.packlink.api;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * A class representing a service for a specified shipment.
 */
public class Service implements Serializable {

	/**
	 * How the name of the carrier gets specified in JSON.
	 */
	private static final String CARRIER_NAME = "carrier_name";
	/**
	 * How the name of the service gets specified in JSON.
	 */
	private static final String SERVICE_NAME = "name";
	/**
	 * How the price of the service gets specified in JSON.
	 */
	private static final String PRICE = "base_price";
	/**
	 * How the type of the Service gets specified in JSON.
	 */
	private static final String TYPE = "category";
	/**
	 * How it's specified whether this is a shipment that gets picked up or needs to be dropped off.
	 */
	private static final String DROP_OFF = "dropoff";
	/**
	 * How the duration of the shipment gets specified.
	 */
	private static final String DURATION = "transit_time";
	/**
	 * How it's specified whether this is a shipment that gets shipped to the receiver or needs to be picked up.
	 */
	private static final String DELIVERY_TO_PARCELSHOP = "delivery_to_parcelshop";

	/**
	 * The tax rate to apply when requesting the price with tax.
	 */
	private static double tax = 0;

	/**
	 * The name of the carrier of this shipment.
	 */
	public final String carrier;
	/**
	 * The name of this service.
	 */
	public final String serviceName;
	/**
	 * The type of this service.
	 */
	public final Type serviceType;
	/**
	 * The price of this shipment specified in cents, because fuck dealing with decimals for currency.
	 */
	public final int priceInCents;
	/**
	 * How long in days this shipment is going to take.
	 */
	public final byte duration;
	/**
	 * Whether this is a shipment that gets picked up or needs to be dropped off.
	 */
	public final boolean pickup;
	/**
	 * Whether the service is shipped to a full address or needs to be picked up at a parcel shop.
	 */
	public final boolean deliveryToParcelshop;

	/**
	 * Create a new service from a given JSON string.
	 *
	 * @param queryResult The string containing the information about the service.
	 */
	protected Service(JSONObject queryResult) {
		carrier = queryResult.getString(CARRIER_NAME).intern();
		serviceName = queryResult.getString(SERVICE_NAME).intern();
		priceInCents = Integer.parseInt(queryResult.getString(PRICE).replace(".", ""));
		serviceType = Type.parseString(queryResult.getString(TYPE));
		pickup = !queryResult.getBoolean(DROP_OFF);
		duration = Byte.parseByte(queryResult.getString(DURATION).replace(" DAYS", ""));
		deliveryToParcelshop = queryResult.getBoolean(DELIVERY_TO_PARCELSHOP);
	}

	/**
	 * Create a new service from already known data.
	 *
	 * @param carrier      The name of the carrier.
	 * @param serviceName  The name of the service.
	 * @param priceInCents The price of the service in cents without tax.
	 * @param serviceType  The type of the service.
	 * @param duration     The duration of the shipment.
	 * @param pickup       Whether the shipment will be picked up or needs to be dropped off.
	 */
	protected Service(String carrier, String serviceName, int priceInCents, Type serviceType, byte duration, boolean pickup, boolean parcelshop) {
		this.carrier = carrier;
		this.serviceName = serviceName;
		this.priceInCents = priceInCents;
		this.serviceType = serviceType;
		this.duration = duration;
		this.pickup = pickup;
		this.deliveryToParcelshop = parcelshop;
	}

	/**
	 * Set the tax rate to apply when requesting a price with tax.
	 *
	 * @param tax The tax rate to apply.
	 */
	public static void setTax(double tax) {
		Service.tax = tax;
	}

	/**
	 * Calculates the price of this service with the given static tax rate.
	 *
	 * @return The price with tax.
	 */
	public int getPriceWithTax() {
		double tax = priceInCents * Service.tax;
		double result = priceInCents + tax;
		return (int) Math.round(result);
	}

	@Override
	public String toString() {
		return carrier + " " + serviceName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(carrier, serviceName, priceInCents, serviceType);
	}

	/**
	 * The categories of shipments.
	 */
	public enum Type {
		/**
		 * An express shipment.
		 */
		EXPRESS,
		/**
		 * A standard shipment.
		 */
		STANDARD;

		/**
		 * Convert a given string to the corresponding enum.
		 * Sometimes the API provides an empty string for this, so then null gets returned.
		 *
		 * @param string The string to parse.
		 * @return The corresponding enum.
		 */
		@Nullable
		public static Type parseString(String string) {
			switch (string) {
				case "standard" -> {
					return STANDARD;
				}
				case "express" -> {
					return EXPRESS;
				}
				default -> {
					return null;
				}
			}
		}
	}
}
