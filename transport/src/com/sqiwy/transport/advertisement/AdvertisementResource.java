package com.sqiwy.transport.advertisement;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
/**
 * Created by abrysov
 */
public class AdvertisementResource implements Serializable {
	
	public static final int NOT_DOWNLOADED = 0;
	public static final int DOWNLOADED = 1;
	public static final int DOWNLOADING = 2;
	
	private long id;
	private String guid;
	private String adsGuid;
//	private boolean archive;
	@SerializedName("url")
	private String uri;
	private String storageUri;
	private String accessUri;
    @SerializedName("size")
	private long bytes;
    private String sha1;
	private int status;

    private Advertisement ad;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
//	public boolean isArchive() {
//		return archive;
//	}
//	public void setArchive(boolean archive) {
//		this.archive = archive;
//	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getStorageUri() {
		return storageUri;
	}
	public void setStorageUri(String storageUri) {
		this.storageUri = storageUri;
	}
	public String getAccessUri() {
		return accessUri;
	}
	public void setAccessUri(String accessUri) {
		this.accessUri = accessUri;
	}
	public long getBytes() {
		return bytes;
	}
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
    public String getSha1() {
        return sha1;
    }
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
    public String getAdsGuid() {
        return adsGuid;
    }
    public void setAdsGuid(String adsGuid) {
        this.adsGuid = adsGuid;
    }
    public Advertisement getAd() {
        return ad;
    }
    public void setAd(Advertisement ads) {
        this.ad = ads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvertisementResource res = (AdvertisementResource) o;

//        if (archive != res.archive) return false;
        if (bytes != res.bytes) return false;
        if (id != res.id) return false;
        if (status != res.status) return false;
        if (accessUri != null ? !accessUri.equals(res.accessUri) : res.accessUri != null) return false;
        if (adsGuid != null ? !adsGuid.equals(res.adsGuid) : res.adsGuid != null) return false;
        if (!guid.equals(res.guid)) return false;
        if (!sha1.equals(res.sha1)) return false;
        if (storageUri != null ? !storageUri.equals(res.storageUri) : res.storageUri != null) return false;
        if (!uri.equals(res.uri)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + guid.hashCode();
        result = 31 * result + (adsGuid != null ? adsGuid.hashCode() : 0);
//        result = 31 * result + (archive ? 1 : 0);
        result = 31 * result + uri.hashCode();
        result = 31 * result + (storageUri != null ? storageUri.hashCode() : 0);
        result = 31 * result + (accessUri != null ? accessUri.hashCode() : 0);
        result = 31 * result + (int) (bytes ^ (bytes >>> 32));
        result = 31 * result + sha1.hashCode();
        result = 31 * result + status;
        return result;
    }
}
