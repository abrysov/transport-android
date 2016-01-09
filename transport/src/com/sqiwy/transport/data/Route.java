/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Route {
    public static final String DIRECT = "direct";
    public static final String REVERSE = "reverse";

    
	private long id;
	private transient long duration;
	private transient Date start;
	private transient Date end;
	private String direction;

    @SerializedName("geo_route")
    private List<Point> points;

    @SerializedName("points")
    private List<Point> busStops;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("duration")
    private String durationTime;

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
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}
	/**
	 * @return the start
	 */
	public Date getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Date start) {
		this.start = start;
	}
	/**
	 * @return the end
	 */
	public Date getEnd() {
		return end;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(Date end) {
		this.end = end;
	}
	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}
	/**
	 * @param direction the direction to set
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}
	/**
	 * @return the points
	 */
	public List<Point> getPoints() {
		return points;
	}

    public List<Point> getBusStops() {
        return busStops;
    }

    public void setBusStops(List<Point> busStops) {
        this.busStops = busStops;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(String durationTime) {
        this.durationTime = durationTime;
    }

    /**
	 * @param points the points to set
	 */
	public void setPoints(List<Point> points) {
		this.points = points;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + (int) (duration ^ (duration >>> 32));
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((points == null) ? 0 : points.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Route other = (Route) obj;
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction))
			return false;
		if (duration != other.duration)
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (id != other.id)
			return false;
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Route [id=" + id + ", duration=" + duration + ", start=" + start + ", end=" + end + ", direction="
				+ direction + ", points=" + points + "]";
	}

}
