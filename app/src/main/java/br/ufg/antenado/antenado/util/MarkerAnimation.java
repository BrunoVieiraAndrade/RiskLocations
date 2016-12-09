package br.ufg.antenado.antenado.util;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

class MarkerAnimation {
	private final static Interpolator sInterpolator = new AccelerateDecelerateInterpolator();

	private final Marker mMarker;
	private final LatLngInterpolator mLatLngInterpolator;
	private final LatLng mStartPosition;
	private final LatLng mFinalPosition;
	private final long mDuration;
	private final long mStartTime;


	public MarkerAnimation(Marker marker, LatLng finalPosition, LatLngInterpolator latLngInterpolator,
						   long duration) {
		mMarker = marker;
		mLatLngInterpolator = latLngInterpolator;
		mStartPosition = marker.getPosition();
		mFinalPosition = finalPosition;
		mDuration = duration;
		mStartTime = AnimationUtils.currentAnimationTimeMillis();
	}

	public boolean animate() {
		// Calculate progress using interpolator
		long elapsed = AnimationUtils.currentAnimationTimeMillis() - mStartTime;
		float t = elapsed / (float)mDuration;
		float v = sInterpolator.getInterpolation(t);
		mMarker.setPosition(mLatLngInterpolator.interpolate(v, mStartPosition, mFinalPosition));

		// Repeat till progress is complete.
		return (t < 1);
	}

	@Override
	public int hashCode() {
		// So we only get one animation for the same marker in our HashSet
		return mMarker.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Marker){
			return mMarker.equals(o);
		}
		return super.equals(o);
	}
}