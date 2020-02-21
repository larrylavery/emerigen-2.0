package samples;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SensorReading {
	@JsonProperty("_id")
	private String id;
	@JsonProperty("_rev")
	private String revision;
	final private String sensorID;
	final private Integer locationID;
	final private Long timestamp;
	final private Double lowerBound;
	final private Double upperBound;
	final private Double percentZebra;

	public SensorReading(String id, String revision, String sensorID, int locationID,
			long timestamp, double lowerBound, double upperBound, double percentZebra) {
		this.id = id;
		this.sensorID = sensorID;
		this.revision = revision;
		this.locationID = locationID;
		this.timestamp = timestamp;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.percentZebra = percentZebra;
	}

	String getKey() {
		final String result = getSensorID() + '-' + getLocationID() + '-'
				+ getTimestamp().toString();
		System.out.println("Key string = " + result);
		return result;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRevision() {
		return revision;
	}

	public String getSensorID() {
		return sensorID;
	}

	public String getId() {
		return id;
	}

	public Integer getLocationID() {
		return locationID;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public Double getUpperBound() {
		return upperBound;
	}

	public Double getPercentZebra() {
		return percentZebra;
	}

}
