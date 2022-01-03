package de.tgx03.packlink.api;

import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A single parcel to be shipped.
 */
public class Parcel {

	/**
	 * The string specifying which package this is.
	 */
	private static final String PACKAGE = URLEncoder.encode("packages[", StandardCharsets.UTF_8);
	/**
	 * The string specifying the weight of the package.
	 */
	private static final String WEIGHT = URLEncoder.encode("][weight]", StandardCharsets.UTF_8);
	/**
	 * The string specifying the height of the package.
	 */
	private static final String HEIGHT = URLEncoder.encode("][height]", StandardCharsets.UTF_8);
	/**
	 * The string specifying the length of the package.
	 */
	private static final String LENGTH = URLEncoder.encode("][length]", StandardCharsets.UTF_8);
	/**
	 * The string specifying the width of the package.
	 */
	private static final String WIDTH = URLEncoder.encode("][width]", StandardCharsets.UTF_8);

	/**
	 * The height of this package.
	 */
	public final short height;
	/**
	 * The length of this package.
	 */
	public final short length;
	/**
	 * The width of this package.
	 */
	public final short width;
	/**
	 * The weight of this package.
	 */
	public final String weight;

	/**
	 * Creates a new parcel with only the weight set.
	 * Is officially supported, but I dunno.
	 *
	 * @param weight The weight of the package in kilos.
	 */
	public Parcel(@NotNull String weight) {
		this.weight = weight;
		this.height = -1;
		this.length = -1;
		this.width = -1;
	}

	/**
	 * Creates a new parcel with dimensions and weight.
	 *
	 * @param weight The weight of the package in kilos.
	 * @param height The height of the package in cm.
	 * @param length The length of the package in cm.
	 * @param width  The width of the package in cm.
	 */
	public Parcel(@NotNull String weight, short height, short length, short width) {
		this.weight = weight;
		this.length = length;
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a string that can be appended to an URL if this is the only package in the shipment.
	 *
	 * @return The string to be used in an URL.
	 */
	@NotNull
	protected String toHTTPRequest() {
		return toHTTPRequest(0);
	}

	/**
	 * Creates a string that can be appended to an URL which also specifies which package this is.
	 *
	 * @param parcelID The ID of this parcel.
	 * @return The string to be used in an URL.
	 */
	@NotNull
	protected String toHTTPRequest(int parcelID) {
		StringBuilder builder = new StringBuilder(67);
		builder.append(PACKAGE).append(parcelID).append(WEIGHT).append("=").append(weight);
		if (height > 0 && width > 0 && length > 0) {
			builder.append("&").append(PACKAGE).append(parcelID).append(HEIGHT).append("=").append(height);
			builder.append("&").append(PACKAGE).append(parcelID).append(LENGTH).append("=").append(length);
			builder.append("&").append(PACKAGE).append(parcelID).append(WIDTH).append("=").append(width);
		}
		return builder.toString();
	}

	@Override
	@NotNull
	public String toString() {
		String result = weight + "Kg";
		if (height > 0 && width > 0 && length > 0) {
			result = result + " " + length + "*" + width + "*" + height;
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Parcel parcel = (Parcel) o;
		return height == parcel.height && length == parcel.length && width == parcel.width && weight.equals(parcel.weight);
	}

	@Override
	public int hashCode() {
		return Objects.hash(height, length, width, weight);
	}
}
