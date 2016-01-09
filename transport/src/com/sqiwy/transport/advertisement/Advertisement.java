package com.sqiwy.transport.advertisement;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.sqiwy.transport.data.Point;

/**
 * Created by abrysov
 */
@SuppressWarnings("serial")
public class Advertisement implements Serializable {
	
	public static final String TRIGGER_ANY = "ANY";
	public static final String TRIGGER_GEO = "GEO";
//	public static final String TRIGGER_ONGOING = "ONGOING";

//    public static String toTriggers(String string) {
////        return Arrays.asList( (string.split(",")) );
//        return string;
//    }
//
//    public static String fromTriggers(String trig) {
////        return Joiner.on(",").skipNulls().join(trig);
//        return trig;
//    }

    public static enum Type {
		VIDEO,
		TEXT,
		BANNER
	}

	private long id;

//    @SerializedName("advertiser_name")
//    private String advertiserName;
//
//    private String name;

    @SerializedName("end_date")
    private String endDate;

//    @SerializedName("total_shows")
//    private Integer totalShows;

    private int version;

    private List<Point> points;

    private String guid;

    @SerializedName("trigger")
    private String trigger;

	@SerializedName("content_type")
	private String type;

    @SerializedName("max_hour_shows")
    private Integer maxHourShows;

    @SerializedName("duration")
	private long showDuration;

//    @SerializedName("start_date")
//    private String startDate;

    @SerializedName("content")
	private List<AdvertisementResource> resources;

    private Integer shows;


	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the showDuration
	 */
	public long getShowDuration() {
		return showDuration;
	}
	/**
	 * @param showDuration the showDuration to set
	 */
	public void setShowDuration(long showDuration) {
		this.showDuration = showDuration;
	}
	/**
	 * @return the resource
	 */
	public List<AdvertisementResource> getResources() {
		return resources;
	}
	/**
	 * @param resources the resource to set
	 */
	public void setResources(List<AdvertisementResource> resources) {
		this.resources = resources;
	}
	/**
	 * @return the triggers
	 */
	public String getTrigger() {
		return trigger;
	}
	/**
	 * @param trig the trigger to set
	 */
	public void setTriggers(String trig) {
		this.trigger = trig;
	}
	/**
	 * @return the points
	 */
	public List<Point> getPoints() {
		return points;
	}
	/**
	 * @param points the points to set
	 */
	public void setPoints(List<Point> points) {
		this.points = points;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}


//    public String getAdvertiserName() {
//        return advertiserName;
//    }
//
//    public void setAdvertiserName(String advertiserName) {
//        this.advertiserName = advertiserName;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

//    public Integer getTotalShows() {
//        return totalShows;
//    }
//
//    public void setTotalShows(Integer totalShows) {
//        this.totalShows = totalShows;
//    }

    public Integer getMaxHourShows() {
        return maxHourShows;
    }

    public void setMaxHourShows(Integer maxHourShows) {
        this.maxHourShows = maxHourShows;
    }

//    public String getStartDate() {
//        return startDate;
//    }
//
//    public void setStartDate(String startDate) {
//        this.startDate = startDate;
//    }

    public Integer getShows() {
        return shows;
    }

    public void setShows(Integer shows) {
        this.shows = shows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Advertisement that = (Advertisement) o;

        if (id != that.id) return false;
        if (showDuration != that.showDuration) return false;
        if (version != that.version) return false;
        if (!guid.equals(that.guid)) return false;
        if (points != null ? !points.equals(that.points) : that.points != null) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + guid.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (int) (showDuration ^ (showDuration >>> 32));
        result = 31 * result + version;
        return result;
    }
}