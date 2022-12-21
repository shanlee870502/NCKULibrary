package edu.ncku.application.model;

import java.io.Serializable;

public class News implements Serializable {

	private String title;
	private String unit;
	private int pubTime;
	private int endTime;
	private String contents;

	public News(String title, String unit, int pubTime, int endTime, String contents) {
		super();
		this.title = title;
		this.unit = unit;
		this.pubTime = pubTime;
		this.endTime = endTime;
		this.contents = contents;
	}

	public String getTitle() {
		return title;
	}

	public String getUnit() {
		return unit;
	}

	public int getPubTime() {
		return pubTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public String getContents() {
		return contents;
	}

	public boolean equals(Object o) {
		return (o instanceof News) && (((News) o).getTitle()).equals(this.getTitle());
	}

	public int hashCode() {
		return title.hashCode() + pubTime;
	}
	
}
