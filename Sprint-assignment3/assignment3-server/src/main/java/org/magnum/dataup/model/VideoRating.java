package org.magnum.dataup.model;

public class VideoRating {
	private int rating = 0;
	private int nums = 0;
	
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public int getNums() {
		return nums;
	}
	public void setNums(int nums) {
		this.nums = nums;
	}
	
	public String toString() {
		return "VideoRating: (" + rating + ") based on " + nums + " votes";
	}

}
